package net.arwix.gastro.client.ui.check

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import net.arwix.gastro.client.domain.PrinterUseCase
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.CheckData
import net.arwix.mvi.SimpleIntentViewModel

class CheckDetailViewModel(
    private val firestore: FirebaseFirestore
) :
    SimpleIntentViewModel<CheckDetailViewModel.Action, CheckDetailViewModel.Result, CheckDetailViewModel.State>() {

    override var internalViewState: State = State()

    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData<Result> {
            val lastCheck =
                firestore.collection("checks").orderBy("created", Query.Direction.DESCENDING)
                    .limit(1).get().await()
            val data = lastCheck!!.documents[0].toObject(CheckData::class.java) ?: return@liveData
            emit(Result.LastCheck(data))
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