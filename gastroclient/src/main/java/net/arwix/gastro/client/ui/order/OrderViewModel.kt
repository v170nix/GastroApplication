package net.arwix.gastro.client.ui.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import net.arwix.mvi.SimpleIntentViewModel

class OrderViewModel: SimpleIntentViewModel<OrderViewModel.Action, OrderViewModel.Result, OrderViewModel.State>() {
    override var internalViewState: State = State()

    override fun dispatchAction(action: Action): LiveData<Result> = liveData<Result> {
        when (action) {
            is Action.Table -> {
                emit(Result.TableSelect)
            }
        }
    }

    override fun reduce(result: Result): State {
        return State()
    }

    sealed class Action {
        data class Table(val number: Number): Action()
    }
    sealed class Result {
        object TableSelect: Result()
    }


    data class State(
        val isTableSelect: Boolean = false
    )

}