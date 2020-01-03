package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import net.arwix.gastro.library.order.data.OrderItem

data class CheckData constructor(
    var waiterId: Int? = null,
    var table: Int? = null,
    var tablePart: Int? = null,
    var checkItems: Map<String, List<OrderItem>> = mapOf(),
    var isReturnOrder: Boolean = false,
    var isSplitOrder: Boolean = false,
    @ServerTimestamp var created: Timestamp? = null
)