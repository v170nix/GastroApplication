package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class CloseTableData constructor(
    var table: Int? = null,
    var tablePart: Int? = null,
    var orders: List<DocumentReference> = listOf(),
    var checks: List<DocumentReference> = listOf(),
    var summaryOrderPrice: Long = 0L,
    var summaryPayPrice: Long = 0L,
    @ServerTimestamp var closedTime: Timestamp? = null
)