package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class OrderData constructor(
    var waiterId: Int? = null,
    var table: Int? = null,
    var orderItems: List<OrderItem>? = null,
    @ServerTimestamp var timestampCreated: Timestamp? = null,
    @field:JvmField var isPrinted: Boolean = false
) {
    constructor() : this(null, null, null, null, false)
}