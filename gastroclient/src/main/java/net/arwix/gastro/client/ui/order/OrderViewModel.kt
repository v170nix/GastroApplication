package net.arwix.gastro.client.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.data.OrderItem
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
            Log.e("menu", menu.toString())
            notificationFromObserver(Result.AddMenu(menu))
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> = liveData {
        when (action) {
            is Action.AddItem -> {
                emit(Result.AddItem(action.partIndex, action.typeItem, action.item))
            }
            is Action.EditItem -> {
                emit(Result.EditItem(action.partIndex, action.typeItem, action.item))
            }
            is Action.ChangeCountItem -> {
                emit(
                    Result.ChangeCountItem(
                        action.partIndex,
                        action.typeItem,
                        action.item,
                        action.delta
                    )
                )
            }
            is Action.SubmitOrder -> {
                withContext(Dispatchers.Main) {
                    val orders = firestore.collection("orders")
                    val orderData =
                        OrderData(
                            action.userId,
                            internalViewState.orderParts[0].table!!,
                            internalViewState.orderParts[0].orderItems.filter {
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
                        val doc = orders.document()
                        val table = orderData.table!!
                        firestore.runTransaction {
                            val openTableDoc =
                                it.get(firestore.collection("open tables").document(table.toString()))
                            val openTableData = openTableDoc.toObject(OpenTableData::class.java)
                            val newOpenTableData = OpenTableData(
                                parts = openTableData?.parts?.run { this + doc } ?: listOf(doc)
                            )
                            it.set(doc, orderData)
                            it.set(openTableDoc.reference, newOpenTableData)
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

    fun selectTable(number: Int) {
        notificationFromObserver(Result.InitOrder(number))
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.InitOrder -> {
                internalViewState.copy(
                    orderParts = mutableListOf(
                        OrderPart(
                            table = result.table,
                            orderItems = mutableMapOf<String, List<OrderItem>>().apply {
                                menuTypes?.forEach { menuType ->
                                    this[menuType] = listOf()
                                }
                            }
                        ))
                )
            }
            Result.Clear -> internalViewState.copy(
                orderParts = internalViewState.orderParts.apply {
                    this.forEach { orderPart: OrderPart ->
                        orderPart.orderItems.forEach { (key, _) ->
                            orderPart.orderItems[key] = listOf()
                        }
                    }
                },
                isSubmit = false
            )
            is Result.AddMenu -> {
                internalViewState.copy(
                    isLoadingMenu = false,
                    orderParts = mutableListOf(
                        OrderPart(
                            table = kotlin.run {
                                internalViewState.orderParts.firstOrNull()?.table
                            },
                            orderItems = mutableMapOf<String, List<OrderItem>>().apply {
                                result.list.forEach { menuType ->
                                    this[menuType] = listOf()
                                }
                            }
                        ))
                )
            }
            is Result.AddItem -> {
                internalViewState.copy(orderParts = internalViewState.orderParts.apply {
                    val part = internalViewState.orderParts[result.partIndex]
                    val old = part.orderItems[result.typeItem]
                    if (old == null) {
                        part.orderItems[result.typeItem] = listOf(result.item)
                    } else {
                        part.orderItems[result.typeItem] = old + result.item
                    }
                })
            }
            is Result.EditItem -> {
                internalViewState.copy(orderParts = internalViewState.orderParts.apply {
                    val part = internalViewState.orderParts[result.partIndex]
                    val old = part.orderItems[result.typeItem] ?: return@apply
                    val current = old.map {
                        if (it.name == result.item.name) result.item else it
                    }
                    part.orderItems[result.typeItem] = current
                })
            }
            is Result.SubmitOrder -> {
                internalViewState.copy(isSubmit = true)
            }
            is Result.ChangeCountItem -> {
                internalViewState.copy(orderParts = internalViewState.orderParts.apply {
                    val part = internalViewState.orderParts[result.partIndex]
                    val orderList = part.orderItems[result.typeItem] ?: return@apply
                    part.orderItems[result.typeItem] = orderList.map {
                        if (it.name == result.item.name) it.copy(count = it.count + result.delta) else it
                    }
                })
            }
        }
    }

    sealed class Action {
        data class AddItem(val partIndex: Int, val typeItem: String, val item: OrderItem) : Action()
        data class EditItem(val partIndex: Int, val typeItem: String, val item: OrderItem) :
            Action()

        data class ChangeCountItem(
            val partIndex: Int,
            val typeItem: String,
            val item: OrderItem,
            val delta: Int
        ) : Action()

        data class SubmitOrder(val userId: Int) : Action()
    }

    sealed class Result {
        object Clear : Result()
        data class InitOrder(val table: Int) : Result()
        data class AddMenu(val list: List<String>) : Result()
        data class AddItem(val partIndex: Int, val typeItem: String, val item: OrderItem) : Result()
        data class EditItem(val partIndex: Int, val typeItem: String, val item: OrderItem) :
            Result()

        data class ChangeCountItem(
            val partIndex: Int,
            val typeItem: String,
            val item: OrderItem,
            val delta: Int
        ) : Result()

        object SubmitOrder : Result()
    }


    data class State(
        val isLoadingMenu: Boolean = true,
        val orderParts: MutableList<OrderPart> = mutableListOf(),
        val isSubmit: Boolean = false
    )

    data class OrderPart(
        val table: Int? = null,
        val orderItems: MutableMap<String, List<OrderItem>> = mutableMapOf()
    )

    companion object {
        const val BUNDLE_ID_ITEM_TYPE = "gastro.order.type"
        const val BUNGLE_ID_ORDER_PART_ID = "gastro.order.part.id"
    }

}