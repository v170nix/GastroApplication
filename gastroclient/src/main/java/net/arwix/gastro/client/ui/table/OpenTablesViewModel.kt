package net.arwix.gastro.client.ui.table

import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.OrderItem
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.toFlow
import net.arwix.mvi.SimpleIntentViewModel

class OpenTablesViewModel(
    private val firestoreDbApp: FirestoreDbApp
) :
    SimpleIntentViewModel<OpenTablesViewModel.Action, OpenTablesViewModel.Result, OpenTablesViewModel.State>() {

    override var internalViewState = State()

    init {
        viewModelScope.launch {
            val query = firestoreDbApp.refs.openTables
            query
                .orderBy("updated", Query.Direction.DESCENDING)
                .toFlow()
                .collect { snapshot ->
                    val map = LinkedHashMap<TableGroup, OpenTableData>()
                    snapshot.documents.map {
                        map[TableGroup.fromString(it.id)] = it.toObject(OpenTableData::class.java)!!
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
        data class UpdateTablesList(val tablesData: Map<TableGroup, OpenTableData>?) : Result()
    }

    data class State(
        val tablesData: Map<TableGroup, OpenTableData>? = null
    )

    private companion object {
        private fun filterOrderMap(map: Map<String, List<OrderItem>>): Map<String, List<OrderItem>> {
            val outMap = mutableMapOf<String, List<OrderItem>>()
            map.forEach { (type, orderItems) ->
                //  val items = orderItems.filter { it.count > it.checkout }
                if (orderItems.isNotEmpty()) {
                    outMap[type] = orderItems
                }
            }
            return outMap
        }
    }
}