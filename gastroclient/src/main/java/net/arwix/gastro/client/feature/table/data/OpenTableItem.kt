package net.arwix.gastro.client.feature.table.data

import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderItem

data class OpenTableItem(
    // выбранное число для оплаты
    val payCount: Int = 0,
    // сумма исходного оредера
    val orderItem: OrderItem,
    // колличество уже оплаченых чеков
    val checkCount: Int = 0,
    // колличество удаленных чеков
    val returnCount: Int = 0,
    // колличество переведеных на другой стол чеков
    val splitCount: Int = 0
)

typealias MutableTableItems = MutableMap<MenuGroupName, MutableList<OpenTableItem>>
typealias TableItems = Map<MenuGroupName, List<OpenTableItem>>

fun TableItems.filterPayItems(): TableItems = mapValues { (_, list) ->
    list.filter { it.payCount > 0 }
}.filter { (_, list) ->
    list.isNotEmpty()
}

fun TableItems.changePayCount(
    menuGroupName: MenuGroupName,
    openTableItem: OpenTableItem,
    delta: Int
): TableItems {
    val list = (this[menuGroupName] ?: return this).toMutableList()
    val index = list.indexOf(openTableItem)
    if (index == -1) return this
    list[index] = openTableItem.copy(payCount = openTableItem.payCount + delta)
    return toMutableMap().apply { this[menuGroupName] = list }
}