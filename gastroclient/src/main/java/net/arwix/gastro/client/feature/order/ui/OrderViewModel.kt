package net.arwix.gastro.client.feature.order.ui

import android.content.Context
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.arwix.gastro.client.feature.order.domain.OrderUseCase
import net.arwix.gastro.client.feature.print.ui.PrintIntentService
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGridItem
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.menu.data.MenuRepository
import net.arwix.gastro.library.order.data.OrderItem
import net.arwix.mvi.SimpleIntentViewModel

class OrderViewModel(
    private val context: Context,
    private val menuRepository: MenuRepository,
    private val orderUseCase: OrderUseCase
//    private val orderRepository: OrderRepository
) :
    SimpleIntentViewModel<OrderViewModel.Action, OrderViewModel.Result, OrderViewModel.State>() {

    var isAnimateBigButton = true

    private var menuGroupData: List<MenuGroupData> = mutableListOf()
    override var internalViewState: State = State()

    init {
        viewModelScope.launch {
            menuRepository.getMenusAsFlow().collect { menus ->
                menus.sortedBy { it.metadata.order }.let {
                    menuGroupData = it
                    notificationFromObserver(Result.SetMenu(menus))
                }
            }
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> = liveData {
        when (action) {
            is Action.AddItem -> emit(
                Result.AddItem(
                    action.typeItem,
                    action.item
                )
            )

            is Action.ChangeCountItem -> emit(
                Result.ChangeCountItem(
                    action.typeItem,
                    action.item,
                    action.delta
                )
            )

            is Action.AddItems -> {
                if (action.items.isEmpty()) return@liveData
                withContext(Dispatchers.Main) {
                    action.items.map {
                        notificationFromObserver(
                            Result.AddItem(
                                it.menu.name,
                                OrderItem(
                                    it.value.name!!,
                                    it.value.price,
                                    1
                                )
                            )
                        )
                    }
                }
            }

            is Action.SubmitOrder -> {
                withContext(Dispatchers.Main) {
                    val reference = orderUseCase.submit(
                        action.userId,
                        internalViewState.tableGroup!!,
                        internalViewState.orderItems.mapKeys { it.key.name }
                    ) ?: return@withContext
                    PrintIntentService.startPrintOrder(context, reference.id)
                }
                emit(Result.SubmitOrder())
            }
        }
    }

    @UiThread
    fun clear() {
        notificationFromObserver(Result.Clear)
    }

    @UiThread
    fun selectTable(tableGroup: TableGroup) {
        notificationFromObserver(Result.InitOrder(tableGroup))
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.InitOrder -> {
                internalViewState.copy(
                    tableGroup = result.tableGroup,
                    orderItems = mutableMapOf<MenuGroupData, List<OrderItem>>().apply {
                        menuGroupData.forEach { this[it] = listOf() }
                    },
                    isSubmit = false
                )
            }

            Result.Clear -> internalViewState.copy(
                orderItems = internalViewState.orderItems.toMutableMap().apply {
                    forEach { (menuType, _) -> this[menuType] = listOf() }
                },
                isSubmit = false
            )

            is Result.SetMenu -> {
                internalViewState.copy(
                    isLoadingMenu = false,
                    orderItems = mutableMapOf<MenuGroupData, List<OrderItem>>().apply {
                        val map: Map<MenuGroupName, List<OrderItem>> =
                            internalViewState.orderItems.mapKeys {
                                it.key.name
                            }
                        result.list.forEach { menuGroup ->
                            this[menuGroup] = map.getOrElse(menuGroup.name) { listOf() }
                        }
                    }
                )

            }

            is Result.AddItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems
                    .toMutableMap().apply {
                        val menuGroupData = keys.find { it.name == result.typeItem } ?: return@apply
                        val orderItems = this[menuGroupData]
                        if (orderItems == null) this[menuGroupData] = listOf(result.item)
                        else {
                            // проверка на одинаковость
                            val listOrderItem = orderItems.find {
                                it.name == result.item.name && it.price == result.item.price
                            }
                            if (listOrderItem == null) {
                                this[menuGroupData] = orderItems + result.item
                            } else {
                                this[menuGroupData] = orderItems.toMutableList().also {
                                    val indexItem = it.indexOf(listOrderItem)
                                    it[indexItem] =
                                        listOrderItem.copy(count = listOrderItem.count + result.item.count)
                                }
                            }
                        }
                    })
            }
            is Result.SubmitOrder -> {
                internalViewState.copy(isSubmit = true)
            }
            is Result.ChangeCountItem -> {
                internalViewState.copy(
                    orderItems = internalViewState.orderItems.changeCountItem(
                        result.menuGroupData,
                        result.item,
                        result.delta
                    )
                )
            }
        }
    }

    sealed class Action {
        data class AddItem(val typeItem: String, val item: OrderItem) : Action()
        data class AddItems(val items: Set<MenuGridItem.Item>) : Action()

        data class ChangeCountItem(
            val typeItem: MenuGroupData,
            val item: OrderItem,
            val delta: Int
        ) : Action()

        data class SubmitOrder(val userId: Int) : Action()
    }

    sealed class Result {
        object Clear : Result()
        data class InitOrder(val tableGroup: TableGroup) : Result()
        data class SetMenu(val list: List<MenuGroupData>) : Result()
        data class AddItem(val typeItem: String, val item: OrderItem) : Result()

        data class ChangeCountItem(
            val menuGroupData: MenuGroupData,
            val item: OrderItem,
            val delta: Int
        ) : Result()

        data class SubmitOrder(val resultPrint: List<Int>? = null) : Result()
    }


    data class State(
        val isLoadingMenu: Boolean = true,
        val tableGroup: TableGroup? = null,
        val orderItems: Map<MenuGroupData, List<OrderItem>> = mapOf(),
        val isSubmit: Boolean = false
    )

    private companion object {

        private fun Map<MenuGroupData, List<OrderItem>>.changeCountItem(
            menuGroupData: MenuGroupData,
            orderItem: OrderItem,
            delta: Int
        ): Map<MenuGroupData, List<OrderItem>> {
            return toMutableMap().apply {
                val orderItems = this[menuGroupData] ?: return@apply
                this[menuGroupData] = orderItems.map {
                    if (it.name == orderItem.name && it.count + delta >= 0) it.copy(count = it.count + delta) else it
                }
            }
        }
    }

}