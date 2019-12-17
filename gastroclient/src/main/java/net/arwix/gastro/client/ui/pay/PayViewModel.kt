package net.arwix.gastro.client.ui.pay

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.toFlow
import net.arwix.mvi.SimpleIntentViewModel

class PayViewModel(
    private val firestore: FirebaseFirestore
) : SimpleIntentViewModel<PayViewModel.Action, PayViewModel.Result, PayViewModel.State>() {

    private var tableUpdateJob: Job? = null
    override var internalViewState: State = State()

    fun setTable(tableGroup: TableGroup) {
        tableUpdateJob?.cancel()
        notificationFromObserver(Result.InitTable(tableGroup))
        tableUpdateJob = viewModelScope.launch {
            firestore.collection("open tables").document(tableGroup.toDocId())
                .toFlow()
                .collect {
                    it.toObject(OpenTableData::class.java)?.run {
                        val summaryData = ordersToSum(this)
                        Log.e("collect", summaryData.toString())
                        notificationFromObserver(Result.TableData(tableGroup, this, summaryData))
                    }
                }
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData<Result> {
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
            }
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
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
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
        return summaryOrder
    }

    sealed class Action {
        data class ChangePayCount(
            val type: String,
            val payOrderItem: PayOrderItem,
            val delta: Int
        ) : Action()
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
    }

    data class State(
        val isLoadingParts: Boolean = true,
        val tableGroup: TableGroup? = null,
        val orders: OpenTableData? = null,
        val summaryData: MutableMap<String, MutableList<PayOrderItem>>? = null
    )

//    it.toObject(OpenTableData::class.java)?.run {
//        val parts = (this.orders ?: return@run).mapNotNull { doc ->
//            val orderData = doc.get().await()?.toObject(OrderData::class.java)
//                ?: return@mapNotNull null
//            val orderItems = orderData.orderItems ?: return@mapNotNull null
//            val payOrderItems =
//                orderItems.mapValues { (_, listOrderItems) ->
//                    listOrderItems.map { orderItem ->
//                        PayOrderItem(orderItem = orderItem)
//                    }.toMutableList()
//                }
//            StatePart(
//                doc,
//                PayOrderData(
//                    orderData, payOrderItems
//                )
//            )
//        }
//        notificationFromObserver(Result.TableData(tableGroup, parts))
//    }

//    data class PayOrderData(
//        val orderData: OrderData,
//        val payOrderItems: Map<String, MutableList<PayOrderItem>>
//    )

    data class PayOrderItem(val payCount: Int = 0, val orderItem: OrderItem)

}