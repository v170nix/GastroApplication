package net.arwix.gastro.client.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class OrderData(
    val waiterId: Int? = null,
    val table: Int? = null,
    val orderItems: List<OrderItem>? = null,
    @ServerTimestamp
    val timestampCreated: Timestamp? = null
)