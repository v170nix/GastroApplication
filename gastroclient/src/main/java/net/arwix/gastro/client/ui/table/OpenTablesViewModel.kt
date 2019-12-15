package net.arwix.gastro.client.ui.table

import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.toFlow
import net.arwix.mvi.SimpleIntentViewModel
import org.threeten.bp.ZonedDateTime

class OpenTablesViewModel(private val firestore: FirebaseFirestore) :
    SimpleIntentViewModel<OpenTablesViewModel.Action, OpenTablesViewModel.Result, OpenTablesViewModel.State>() {

    override var internalViewState = State()

    sealed class Action
    sealed class Result

    data class State(
        val s: Boolean = false
    )

    init {
        val t = Timestamp(ZonedDateTime.now().minusDays(1).toEpochSecond(), 0)
        viewModelScope.launch {
            val query = firestore
                .collection("orders")
                .whereGreaterThan("created", t)
                .orderBy("created", Query.Direction.DESCENDING)
            query.toFlow()
                .collect { snapshot ->
                    snapshot.documents.forEach {
                        val data = it.toObject(OrderData::class.java)
                        Log.e("docs", data.toString())
                    }
                }
        }
    }


    override fun dispatchAction(action: Action) = liveData<Result> {

    }

    override fun reduce(result: Result): State {
        return State()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}