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
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.data.OrderItem
import net.arwix.mvi.SimpleIntentViewModel

class OrderViewModel :
    SimpleIntentViewModel<OrderViewModel.Action, OrderViewModel.Result, OrderViewModel.State>() {

    override var internalViewState: State = State()

    init {
        val firestore = FirebaseFirestore.getInstance()
        viewModelScope.launch {
            val doc = firestore.collection("menu").get().await()!!
            val menu = doc.documents.map { it.id }
            Log.e("menu", menu.toString())
            notificationFromObserver(Result.AddMenu(menu))
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> = liveData {
        when (action) {
            is Action.AddItem -> {
                emit(Result.AddItem(action.typeItem, action.item))
            }
            is Action.EditItem -> {
                emit(Result.EditItem(action.typeItem, action.item))
            }
            is Action.SubmitOrder -> {
                withContext(Dispatchers.Main) {
                    val orders = action.firebase.collection("orders")
                    val orderData =
                        OrderData(6969, internalViewState.table, internalViewState.orderItems)
                    orders.document().set(orderData).await()
                }
                emit(Result.SubmitOrder)
            }
        }
    }

    fun clear() {
        notificationFromObserver(Result.Clear)
    }

    fun selectTable(number: Int) {
        notificationFromObserver(Result.TableSelect(number))
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.TableSelect -> internalViewState.copy(table = result.table)
            Result.Clear -> internalViewState.copy(
                table = null,
                orderItems = internalViewState.orderItems.apply {
                    this.forEach { (key, _) ->
                        this[key] = listOf()
                    }
                },
                isSubmit = false
            )
            is Result.AddMenu -> {
                internalViewState.copy(
                    isLoadingMenu = false,
                    orderItems = internalViewState.orderItems.apply {
                        result.list.forEach {
                            this.getOrPut(it) { listOf() }
                        }
                    }
                )
            }
            is Result.AddItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
                    val old = this[result.typeItem]
                    if (old == null) {
                        this[result.typeItem] = listOf(result.item)
                    } else {
                        this[result.typeItem] = old + result.item
                    }
                })
            }
            is Result.EditItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
                    val old = this[result.typeItem] ?: return@apply
                    val current = old.map {
                        if (it.name == result.item.name) result.item else it
                    }
                    this[result.typeItem] = current
                })
            }
            is Result.SubmitOrder -> {
                internalViewState.copy(isSubmit = true)
            }
        }
    }

    sealed class Action {
        data class AddItem(val typeItem: String, val item: OrderItem) : Action()
        data class EditItem(val typeItem: String, val item: OrderItem) : Action()
        data class SubmitOrder(val firebase: FirebaseFirestore) : Action()
    }

    sealed class Result {
        object Clear : Result()
        data class TableSelect(val table: Int) : Result()
        data class AddMenu(val list: List<String>) : Result()
        data class AddItem(val typeItem: String, val item: OrderItem) : Result()
        data class EditItem(val typeItem: String, val item: OrderItem) : Result()
        object SubmitOrder : Result()
    }


    data class State(
        val isLoadingMenu: Boolean = true,
        val table: Int? = null,
        val orderItems: MutableMap<String, List<OrderItem>> = mutableMapOf(),
        val isSubmit: Boolean = false
    )

}