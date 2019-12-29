package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class OrderData constructor(
    var waiterId: Int? = null,
    var table: Int? = null,
    var tablePart: Int? = null,
    var bonNumbers: Map<String, Int>? = null,
    var orderItems: Map<String, List<OrderItem>>? = null,
    @ServerTimestamp var created: Timestamp? = null
//    @field:JvmField var isPrinted: Boolean = false
)