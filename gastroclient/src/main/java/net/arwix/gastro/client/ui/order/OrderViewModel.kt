package net.arwix.gastro.client.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.data.OrderItem
import net.arwix.mvi.SimpleIntentViewModel

class OrderViewModel: SimpleIntentViewModel<OrderViewModel.Action, OrderViewModel.Result, OrderViewModel.State>() {

    init {
        Log.e("OrderInit", "1")
    }

    override var internalViewState: State = State()

    override fun dispatchAction(action: Action): LiveData<Result> = liveData<Result> {
        when (action) {
            is Action.AddItem -> {
                emit(Result.AddItem(action.item))
            }
            is Action.SubmitOrder -> {
                withContext(Dispatchers.Main) {
                    val orders = action.firebase.collection("orders")
                    val orderData = OrderData(6969, internalViewState.table, internalViewState.orderItems)
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
                orderItems = mutableListOf(),
                isSubmit = false
            )
            is Result.AddItem -> {
                internalViewState.copy(orderItems =  internalViewState.orderItems.apply { add(result.item) })
            }
            is Result.SubmitOrder -> {
                internalViewState.copy(isSubmit = true)
            }
        }
    }

    sealed class Action {
        data class AddItem(val item: OrderItem): Action()
        data class SubmitOrder(val firebase: FirebaseFirestore): Action()
    }
    sealed class Result {
        object Clear: Result()
        data class TableSelect(val table: Int): Result()
        data class AddItem(val item: OrderItem): Result()
        object SubmitOrder : Result()
    }


    data class State(
        val table: Int? = null,
        val orderItems: MutableList<OrderItem> = mutableListOf(),
        val isSubmit: Boolean = false
    )

}