package net.arwix.gastro.client.ui.history.check

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.firebase.firestore.Query
import net.arwix.gastro.client.domain.PrinterUseCase
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.CheckData
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.mvi.SimpleIntentViewModel

class HistoryCheckDetailViewModel(
    private val firestoreDbApp: FirestoreDbApp
) :
    SimpleIntentViewModel<HistoryCheckDetailViewModel.Action, HistoryCheckDetailViewModel.Result, HistoryCheckDetailViewModel.State>() {

    override var internalViewState: State = State()

    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData<Result> {
            val lastCheck =
                firestoreDbApp.refs.checks
                    .orderBy("created", Query.Direction.DESCENDING)
                    .limit(1).get().await()
            lastCheck?.run {
                val data =
                    documents.firstOrNull()?.toObject(CheckData::class.java) ?: return@liveData
                if (data.isReturnOrder) return@liveData
                emit(Result.LastCheck(data))
            }
        }
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.LastCheck -> {
                State(checkData = result.checkData)
            }
        }
    }

    suspend fun print(context: Context): Int {
        return internalViewState.checkData?.run {
            PrinterUseCase(context).printCheck(this)
        } ?: -100
    }

    sealed class Action {
        object GetLastCheck : Action()
    }

    sealed class Result {
        data class LastCheck(val checkData: CheckData) : Result()
    }

    data class State(val checkData: CheckData? = null)

}