package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderItem

data class CheckData constructor(
    var waiterId: Int? = null,
    var table: Int? = null,
    var tablePart: Int? = null,
    var checkItems: CheckItems = mapOf(),
    var isReturnOrder: Boolean = false,
    var isSplitOrder: Boolean = false,
    var splitToTable: Int? = null,
    var splitToTablePart: Int? = null,
    @ServerTimestamp var created: Timestamp? = null
)

typealias CheckItems = Map<MenuGroupName, List<OrderItem>>
typealias MutableCheckItems = MutableMap<MenuGroupName, List<OrderItem>>