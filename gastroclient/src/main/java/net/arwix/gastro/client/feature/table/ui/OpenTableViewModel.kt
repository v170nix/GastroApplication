package net.arwix.gastro.client.feature.table.ui

import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.arwix.gastro.client.data.OpenTableRepository
import net.arwix.gastro.client.feature.table.data.MutableTableItems
import net.arwix.gastro.client.feature.table.data.OpenTableItem
import net.arwix.gastro.client.feature.table.data.TableItems
import net.arwix.gastro.client.feature.table.data.changePayCount
import net.arwix.gastro.client.feature.table.domain.OpenTableUseCase
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.menu.data.MenuRepository
import net.arwix.mvi.SimpleIntentViewModel

class OpenTableViewModel(
    private val menuRepository: MenuRepository,
    private val openTableRepository: OpenTableRepository,
    private val openTableUseCase: OpenTableUseCase
) : SimpleIntentViewModel<OpenTableViewModel.Action, OpenTableViewModel.Result, OpenTableViewModel.State>() {

    private var tableUpdateJob: Job? = null
    override var internalViewState: State = State()

    private var menuGroups: List<MenuGroupName> = listOf()

    init {
        viewModelScope.launch {
            menuRepository.getMenusAsFlow().collect { menus ->
                menuGroups = menus.sortedBy { it.metadata.order }.map { it.name }
            }
        }
    }

    fun setTable(tableGroup: TableGroup) {
        tableUpdateJob?.cancel()
        notificationFromObserver(Result.InitTable(tableGroup))
        tableUpdateJob = viewModelScope.launch {
            openTableRepository
                .asFlow(tableGroup)
                .collect { tableData ->
                    if (tableData == null) {
                        notificationFromObserver(Result.CloseTableGroup)
                    } else {
                        notificationFromObserver(
                            Result.TableData(
                                tableGroup,
                                tableData,
                                openTableUseCase.getItems(tableData).toSortedMap(Comparator { o1, o2 ->
                                    menuGroups.indexOf(o1) - menuGroups.indexOf(o2)
                                })
                            )
                        )
                    }
                }
        }
    }

    fun split(userId: Int, toTableGroup: TableGroup) {
        viewModelScope.launch {
            val tableGroup = internalViewState.tableGroup ?: return@launch
            val openTableData = internalViewState.tableData ?: return@launch
            val tableItems = internalViewState.tableItems ?: return@launch
            openTableUseCase.split(
                waiterId = userId,
                fromTableGroup = tableGroup,
                openTableData = openTableData,
                tableItems = tableItems,
                toTableGroup = toTableGroup
            )
        }
    }

    override fun dispatchAction(action: Action) = liveData {
        when (action) {
            is Action.ChangePayCount -> emit(
                Result.ChangePayCount(
                    action.menuGroupName,
                    action.openTableItem,
                    action.delta
                )
            )
            is Action.DeleteCheckout -> checkout(action.waiterId, internalViewState, true)
            is Action.Checkout -> checkout(action.waiterId, internalViewState, false)
            Action.AddAllItemsToPay -> emit(Result.AddAllItemsToPay)
        }
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.TableData -> {
                internalViewState.copy(
                    tableGroup = result.tableGroup,
                    tableData = result.orders,
                    tableItems = result.tableItems
                )
            }
            is Result.InitTable -> State(tableGroup = result.tableGroup)

            is Result.ChangePayCount -> internalViewState.copy(
                tableItems = internalViewState.tableItems?.run {
                    changePayCount(result.menuGroupName, result.openTableItem, result.delta)
                }
            )

            Result.CloseTableGroup -> internalViewState.copy(isCloseTableGroup = true)
            Result.AddAllItemsToPay -> internalViewState.copy(
                tableItems = internalViewState.tableItems?.mapValues { (_, list) ->
                    list.map {
                        it.copy(
                            payCount = it.orderItem.count - it.checkCount
                        )
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    private suspend fun checkout(
        waiterId: Int,
        state: State,
        isReturnOrder: Boolean = false
    ) {
        val tableData = state.tableData ?: return
        val tableGroup = state.tableGroup ?: return
        val tableItems = state.tableItems ?: return
        openTableUseCase.checkout(
            waiterId,
            tableData,
            tableGroup,
            tableItems,
            isReturnOrder
        )
    }

    sealed class Action {
        data class ChangePayCount(
            val menuGroupName: MenuGroupName,
            val openTableItem: OpenTableItem,
            val delta: Int
        ) : Action()

        object AddAllItemsToPay : Action()
        data class Checkout(val waiterId: Int) : Action()
        data class DeleteCheckout(val waiterId: Int) : Action()
    }

    sealed class Result {
        data class InitTable(val tableGroup: TableGroup) : Result()
        data class TableData(
            val tableGroup: TableGroup,
            val orders: OpenTableData? = null,
            val tableItems: MutableTableItems
        ) : Result()

        data class ChangePayCount(
            val menuGroupName: MenuGroupName,
            val openTableItem: OpenTableItem,
            val delta: Int
        ) : Result()

        object AddAllItemsToPay : Result()
        object CloseTableGroup : Result()
    }

    data class State(
        val isCloseTableGroup: Boolean = false,
        val tableGroup: TableGroup? = null,
        val tableData: OpenTableData? = null,
        val tableItems: TableItems? = null
    )

}