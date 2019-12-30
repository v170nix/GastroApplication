package net.arwix.gastro.library.order.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import net.arwix.gastro.library.menu.data.MenuGroupName


data class OrderData constructor(
    var waiterId: Int? = null,
    var table: Int? = null,
    var tablePart: Int? = null,
    var bonNumbers: Map<MenuGroupName, Int>? = null,
    var orderItems: Map<MenuGroupName, List<OrderItem>> = mapOf(),
    @ServerTimestamp var created: Timestamp? = null
//    @field:JvmField var isPrinted: Boolean = false
)