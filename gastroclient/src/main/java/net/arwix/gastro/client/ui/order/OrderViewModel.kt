package net.arwix.gastro.client.ui.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import net.arwix.mvi.SimpleIntentViewModel

class OrderViewModel: SimpleIntentViewModel<OrderViewModel.Action, OrderViewModel.Result, OrderViewModel.State>() {


    init {
        Log.e("OrderInit", "1")
    }

    override var internalViewState: State = State()

    override fun dispatchAction(action: Action): LiveData<Result> = liveData<Result> {
        when (action) {
            is Action.SelectTable -> {
                emit(Result.TableSelect)
            }
        }
    }

    fun clear() {
        notificationFromObserver(Result.Clear)
    }

    override fun reduce(result: Result): State {
        return when (result) {
            Result.TableSelect -> internalViewState.copy(isTableSelect = true)
            Result.Clear -> internalViewState.copy(isTableSelect = false)
        }
    }

    sealed class Action {
        data class SelectTable(val number: Number): Action()
    }
    sealed class Result {
        object Clear: Result()
        object TableSelect: Result()
    }


    data class State(
        val isTableSelect: Boolean = false
    )

}