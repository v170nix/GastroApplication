package net.arwix.gastro.client.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.data.TableGroup
import net.arwix.mvi.SimpleIntentViewModel

class OrderViewModel(private val firestore: FirebaseFirestore) :
    SimpleIntentViewModel<OrderViewModel.Action, OrderViewModel.Result, OrderViewModel.State>() {

    override var internalViewState: State = State()
    private var menuTypes: List<String>? = null

    init {
        viewModelScope.launch {
            val doc = firestore.collection("menu").orderBy("order").get().await()!!
            val menu = doc.documents.map { it.id }
            menuTypes = menu
            notificationFromObserver(Result.AddMenu(menu))
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> = liveData {
        when (action) {
            is Action.AddItem -> emit(Result.AddItem(action.typeItem, action.item))
            is Action.EditItem -> emit(Result.EditItem(action.typeItem, action.item))
            is Action.ChangeCountItem -> emit(
                Result.ChangeCountItem(
                    action.typeItem,
                    action.item,
                    action.delta
                )
            )

            is Action.SubmitOrder -> {
                withContext(Dispatchers.Main) {
                    val orders = firestore.collection("orders")
                    val orderData =
                        OrderData(
                            action.userId,
                            internalViewState.tableGroup!!.tableId,
                            internalViewState.tableGroup!!.tablePart,
                            internalViewState.orderItems.filter {
                                it.value.isNotEmpty()
                            }.let { filterMap ->
                                mutableMapOf<String, List<OrderItem>>().apply {
                                    filterMap.forEach { (key, list) ->
                                        val filterList = list.filter { it.count > 0 }
                                        if (filterList.isNotEmpty()) {
                                            this[key] = filterList
                                        }
                                    }
                                }
                            }
                        )
                    if (!orderData.orderItems.isNullOrEmpty()) {
                        //   orders.add(orderData).await()!!
                        val orderDoc = orders.document()
                        val tableId = orderData.table.toString().toIntOrNull() ?: return@withContext
                        val tablePart =
                            orderData.tablePart.toString().toIntOrNull() ?: return@withContext
                        val docId = "$tableId-$tablePart"
                        firestore.runTransaction {
                            val openTableDoc =
                                it.get(firestore.collection("open tables").document(docId))
                            it.set(orderDoc, orderData)
                            if (openTableDoc.exists()) {
                                it.update(
                                    openTableDoc.reference,
                                    "orders",
                                    FieldValue.arrayUnion(orderDoc)
                                )
                                it.update(
                                    openTableDoc.reference,
                                    "updated",
                                    FieldValue.serverTimestamp()
                                )
                            } else {
                                val openTableData = openTableDoc.toObject(OpenTableData::class.java)
                                val newOpenTableData = OpenTableData(
                                    orders = openTableData?.orders?.run { this + orderDoc }
                                        ?: listOf(orderDoc)
                                )
                                it.set(openTableDoc.reference, newOpenTableData)
                            }
                        }.await()
                    }
                }
                emit(Result.SubmitOrder)
            }
        }
    }

    fun clear() {
        notificationFromObserver(Result.Clear)
    }

    fun selectTable(tableGroup: TableGroup) {
        notificationFromObserver(Result.InitOrder(tableGroup))
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.InitOrder -> {
                internalViewState.copy(
                    tableGroup = result.tableGroup,
                    orderItems = mutableMapOf<String, List<OrderItem>>().apply {
                        menuTypes?.forEach { menuType ->
                            this[menuType] = listOf()
                        }
                    }
                )
            }

            Result.Clear -> internalViewState.copy(
                orderItems = internalViewState.orderItems.apply {
                    forEach { (menuType, _) -> this[menuType] = listOf() }
                },
                isSubmit = false
            )
            is Result.AddMenu -> {
                internalViewState.copy(
                    isLoadingMenu = false,
                    orderItems = mutableMapOf<String, List<OrderItem>>().apply {
                        result.list.forEach { menuType -> this[menuType] = listOf() }
                    }
                )

            }
            is Result.AddItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
                    val orderItems = this[result.typeItem]
                    if (orderItems == null) this[result.typeItem] = listOf(result.item)
                    else this[result.typeItem] = orderItems + result.item
                })
            }
            is Result.EditItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
                    val orderItems = this[result.typeItem] ?: return@apply
                    val current = orderItems.map {
                        if (it.name == result.item.name) result.item else it
                    }
                    this[result.typeItem] = current
                })
            }
            is Result.SubmitOrder -> {
                internalViewState.copy(isSubmit = true)
            }
            is Result.ChangeCountItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
                    val orderItems = this[result.typeItem] ?: return@apply
                    this[result.typeItem] = orderItems.map {
                        if (it.name == result.item.name) it.copy(count = it.count + result.delta) else it
                    }
                })
            }
        }
    }

    sealed class Action {
        data class AddItem(val typeItem: String, val item: OrderItem) : Action()
        data class EditItem(val typeItem: String, val item: OrderItem) :
            Action()

        data class ChangeCountItem(
            val typeItem: String,
            val item: OrderItem,
            val delta: Int
        ) : Action()

        data class SubmitOrder(val userId: Int) : Action()
    }

    sealed class Result {
        object Clear : Result()
        data class InitOrder(val tableGroup: TableGroup) : Result()
        data class AddMenu(val list: List<String>) : Result()
        data class AddItem(val typeItem: String, val item: OrderItem) : Result()
        data class EditItem(val typeItem: String, val item: OrderItem) :
            Result()

        data class ChangeCountItem(
            val typeItem: String,
            val item: OrderItem,
            val delta: Int
        ) : Result()

        object SubmitOrder : Result()
    }


    data class State(
        val isLoadingMenu: Boolean = true,
        val tableGroup: TableGroup? = null,
        val orderItems: MutableMap<String, List<OrderItem>> = mutableMapOf(),
        val isSubmit: Boolean = false
    )

    companion object {
        const val BUNDLE_ID_ITEM_TYPE = "gastro.order.type"
    }

}