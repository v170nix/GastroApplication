package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class OpenTableData constructor(
    var orders: List<DocumentReference> = listOf(),
    var checks: List<DocumentReference> = listOf(),
    var summaryOrderPrice: Long = 0L,
    var summaryPayPrice: Long = 0L,
    @ServerTimestamp var createdTime: Timestamp? = null,
    @ServerTimestamp var updatedTime: Timestamp? = null
)