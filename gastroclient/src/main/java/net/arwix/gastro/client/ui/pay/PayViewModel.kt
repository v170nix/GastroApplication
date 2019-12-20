package net.arwix.gastro.client.ui.pay

import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.*
import net.arwix.gastro.library.toFlow
import net.arwix.mvi.SimpleIntentViewModel

class PayViewModel(
    private val firestoreDbApp: FirestoreDbApp
) : SimpleIntentViewModel<PayViewModel.Action, PayViewModel.Result, PayViewModel.State>() {

    private var tableUpdateJob: Job? = null
    override var internalViewState: State = State()

    fun setTable(tableGroup: TableGroup) {
        tableUpdateJob?.cancel()
        notificationFromObserver(Result.InitTable(tableGroup))
        tableUpdateJob = viewModelScope.launch {
            firestoreDbApp.refs.openTables.document(tableGroup.toDocId())
                .toFlow()
                .collect {
                    it.toObject(OpenTableData::class.java)?.run {
                        val summaryData = ordersToSum(this)
                        notificationFromObserver(Result.TableData(tableGroup, this, summaryData))
                    }
                }
        }
    }

    override fun dispatchAction(action: Action) = liveData {
        when (action) {
            is Action.ChangePayCount -> {
                emit(
                    Result.ChangePayCount(
                        action.type,
                        action.payOrderItem,
                        action.delta
                    )
                )
            }
            is Action.CheckOut -> {
                withContext(Dispatchers.Main) {
                    val isCloseTable = checkOut(
                        action.waiterId,
                        internalViewState.orders!!,
                        internalViewState.tableGroup!!,
                        internalViewState.summaryData!!
                    )
                    if (isCloseTable) emit(Result.CloseTableGroup)
                }
            }
            Action.AddAllItemsToPay -> emit(Result.AddAllItemsToPay)
        }
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.TableData -> {
                internalViewState.copy(
                    tableGroup = result.tableGroup,
                    orders = result.orders,
                    summaryData = result.summaryData
                )
            }
            is Result.InitTable -> {
                State(tableGroup = result.tableGroup)
            }
            is Result.ChangePayCount -> {
                internalViewState.summaryData?.run {
                    val list = this.getValue(result.type)
                    val index = list.indexOf(result.payOrderItem)
                    list[index] = result.payOrderItem.copy(
                        payCount = result.payOrderItem.payCount + result.delta
                    )
                }
                internalViewState
            }
            is Result.CheckOut -> internalViewState
            Result.CloseTableGroup -> internalViewState.copy(isCloseTableGroup = true)
            Result.AddAllItemsToPay -> {
                internalViewState.summaryData?.run {
                    this.forEach {
                        it.value.forEachIndexed { index, payOrderItem ->
                            it.value[index] =
                                payOrderItem.copy(payCount = payOrderItem.orderItem.count - payOrderItem.checkCount)
                        }
                    }
                }
                internalViewState
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    private fun checkOut(
        waiterId: Int,
        currentOpenTableData: OpenTableData,
        tableGroup: TableGroup,
        summaryData: MutableMap<String, MutableList<PayOrderItem>>
    ): Boolean {
        val checkItems = mutableMapOf<String, List<OrderItem>>()
        var isEmpty = true
        var residualCount = 0
        var summaryPrice = 0L
        summaryData.forEach { (type, payOrderItems) ->
            val payItems = mutableListOf<PayOrderItem>()
            payOrderItems.forEach {
                summaryPrice += it.orderItem.price * it.orderItem.count
                residualCount += it.orderItem.count - it.payCount - it.checkCount
                if (it.payCount > 0) payItems.add(it)
            }
            if (payItems.isNotEmpty()) {
                checkItems[type] = payItems.map { it.orderItem.copy(count = it.payCount) }
                isEmpty = false
            }
        }
        if (isEmpty) return false

        val checkData = CheckData(
            waiterId = waiterId,
            table = tableGroup.tableId,
            tablePart = tableGroup.tablePart,
            checkItems = checkItems
        )
        val openTablesRef = firestoreDbApp.refs.openTables
        val closeTablesRef = firestoreDbApp.refs.closeTables
        val checks = firestoreDbApp.refs.checks
        val checkDoc = checks.document()
        firestoreDbApp.firestore.runTransaction {
            val serverOrderData =
                it.get(openTablesRef.document(tableGroup.toDocId())).toObject(OpenTableData::class.java)!!
            if (serverOrderData.updated != currentOpenTableData.updated) return@runTransaction
            it.set(checkDoc, checkData)
            if (residualCount == 0) {
                // closeTable
                val closeTableData = CloseTableData(
                    table = tableGroup.tableId,
                    tablePart = tableGroup.tablePart,
                    orders = currentOpenTableData.orders,
                    checks = currentOpenTableData.checks?.run {
                        this + checkDoc
                    } ?: run {
                        listOf(checkDoc)
                    },
                    summaryPrice = summaryPrice
                )
                it.set(closeTablesRef.document(), closeTableData)
                it.delete(openTablesRef.document(tableGroup.toDocId()))
            } else {
                it.update(
                    openTablesRef.document(tableGroup.toDocId()),
                    "checks",
                    FieldValue.arrayUnion(checkDoc)
                )
                it.update(
                    openTablesRef.document(tableGroup.toDocId()),
                    "updated",
                    FieldValue.serverTimestamp()
                )
            }
        }
        return residualCount == 0
    }

    private suspend fun ordersToSum(openTableData: OpenTableData): MutableMap<String, MutableList<PayOrderItem>> {
        val summaryOrder = mutableMapOf<String, MutableList<PayOrderItem>>()
        openTableData.orders?.forEach { doc ->
            val orderData = doc.get().await()!!.toObject(OrderData::class.java)!!
            orderData.orderItems?.forEach { (type, orderItems) ->
                val summaryOrderItemsOfCurrentType = summaryOrder[type]
                if (summaryOrderItemsOfCurrentType == null) {
                    summaryOrder[type] =
                        orderItems.map { PayOrderItem(orderItem = it) }.toMutableList()
                } else {
                    orderItems.forEach { orderItem ->
                        val summaryOrderItem = summaryOrderItemsOfCurrentType.find {
                            it.orderItem.name == orderItem.name && it.orderItem.price == orderItem.price
                        }
                        if (summaryOrderItem != null) {
                            val index = summaryOrderItemsOfCurrentType.indexOf(summaryOrderItem)
                            summaryOrderItemsOfCurrentType[index] = summaryOrderItem.copy(
                                orderItem = summaryOrderItem.orderItem.copy(
                                    count = summaryOrderItem.orderItem.count + orderItem.count
                                )
                            )
                        } else {
                            summaryOrderItemsOfCurrentType.add(PayOrderItem(orderItem = orderItem))
                        }
                    }
                }
            }
        }
        openTableData.checks?.forEach { doc ->
            val checkData = doc.get().await()!!.toObject(CheckData::class.java)!!
            checkData.checkItems?.forEach { (type, checkItems) ->
                val summaryOrderItemsOfCurrentType = summaryOrder[type]
                if (summaryOrderItemsOfCurrentType == null) {
                    //ERROR
                    throw IllegalStateException("pay order error sync")
                    //      summaryOrder[type] = orderItems.map { PayOrderItem(orderItem = it) }.toMutableList()
                } else {
                    checkItems.forEach { checkItem ->
                        val summaryOrderItem = summaryOrderItemsOfCurrentType.find {
                            it.orderItem.name == checkItem.name && it.orderItem.price == checkItem.price
                        }
                        if (summaryOrderItem != null) {
                            val index = summaryOrderItemsOfCurrentType.indexOf(summaryOrderItem)
                            summaryOrderItemsOfCurrentType[index] = summaryOrderItem.copy(
                                checkCount = summaryOrderItem.checkCount + checkItem.count
                            )
                        } else {
                            throw IllegalStateException("pay order error sync item")
                        }
                    }
                }
            }
        }

        return summaryOrder
    }

    sealed class Action {
        data class ChangePayCount(
            val type: String,
            val payOrderItem: PayOrderItem,
            val delta: Int
        ) : Action()

        object AddAllItemsToPay : Action()
        data class CheckOut(val waiterId: Int) : Action()
    }

    sealed class Result {
        data class InitTable(val tableGroup: TableGroup) : Result()
        data class TableData(
            val tableGroup: TableGroup,
            val orders: OpenTableData? = null,
            val summaryData: MutableMap<String, MutableList<PayOrderItem>>
        ) : Result()

        data class ChangePayCount(
            val type: String,
            val payOrderItem: PayOrderItem,
            val delta: Int
        ) : Result()

        data class CheckOut(val waiterId: Int) : Result()
        object AddAllItemsToPay : Result()
        object CloseTableGroup : Result()
    }

    data class State(
        val isCloseTableGroup: Boolean = false,
        val isLoadingParts: Boolean = true,
        val tableGroup: TableGroup? = null,
        val orders: OpenTableData? = null,
        val summaryData: MutableMap<String, MutableList<PayOrderItem>>? = null
    )

    data class PayOrderItem(
        val payCount: Int = 0,
        val orderItem: OrderItem,
        val checkCount: Int = 0
    )

}