package net.arwix.gastro.library.order.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import net.arwix.gastro.library.menu.data.MenuGroupName


data class OrderData constructor(
    var waiterId: Int? = null,
    var table: Int? = null,
    var tablePart: Int? = null,
    var bonNumbers: Map<MenuGroupName, Int>? = null,
    var orderItems: OrderItems = mapOf(),
    var fromSplitTable: Int? = null,
    var fromSplitTablePart: Int? = null,
    @ServerTimestamp var created: Timestamp? = null
) {
    fun sum(): Long {
        var sum = 0L
        orderItems.values.forEach {
            it.forEach { order ->
                sum += order.count * order.price
            }
        }
        return sum
    }
}

typealias OrderItems = Map<MenuGroupName, List<OrderItem>>