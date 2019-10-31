package net.arwix.gastro.client.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import net.arwix.gastro.client.data.OrderItem
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
            Result.Clear -> internalViewState.copy(table = null, orderItems = mutableListOf())
            is Result.AddItem -> {
                internalViewState.copy(orderItems =  internalViewState.orderItems.apply { add(result.item) })
            }
        }
    }

    sealed class Action {
        data class AddItem(val item: OrderItem): Action()
    }
    sealed class Result {
        object Clear: Result()
        data class TableSelect(val table: Int): Result()
        data class AddItem(val item: OrderItem): Result()
    }


    data class State(
        val table: Int? = null,
        val orderItems: MutableList<OrderItem> = mutableListOf()
    )

}