package net.arwix.gastro.client.ui.table

import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.toFlow
import net.arwix.mvi.SimpleIntentViewModel

class OpenTablesViewModel(private val firestore: FirebaseFirestore) :
    SimpleIntentViewModel<OpenTablesViewModel.Action, OpenTablesViewModel.Result, OpenTablesViewModel.State>() {

    override var internalViewState = State()

    init {
        viewModelScope.launch {
            val query = firestore
                .collection("open tables")
                .orderBy("updated", Query.Direction.DESCENDING)
            query.toFlow()
                .collect { snapshot ->
                    val map = LinkedHashMap<Int, OpenTableData>()
                    snapshot.documents.forEach {
                        map[it.id.toInt()] = it.toObject(OpenTableData::class.java)!!
                    }
                    notificationFromObserver(Result.UpdateTablesList(map))
                }
        }
    }

    override fun dispatchAction(action: Action) = liveData<Result> {

    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.UpdateTablesList -> {
                internalViewState.copy(
                    tablesData = result.tablesData
                )

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    sealed class Action
    sealed class Result {
        data class UpdateTablesList(val tablesData: Map<Int, OpenTableData>?) : Result()
    }

    data class State(
        val tablesData: Map<Int, OpenTableData>? = null
    )

    private companion object {
        private fun filterOrderMap(map: Map<String, List<OrderItem>>): Map<String, List<OrderItem>> {
            val outMap = mutableMapOf<String, List<OrderItem>>()
            map.forEach { (type, orderItems) ->
                val items = orderItems.filter { it.count > it.checkout }
                if (items.isNotEmpty()) {
                    outMap[type] = items
                }
            }
            return outMap
        }
    }
}