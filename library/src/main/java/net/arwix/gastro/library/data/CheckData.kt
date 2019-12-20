package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class CheckData constructor(
    var waiterId: Int? = null,
    var table: Int? = null,
    var tablePart: Int? = null,
    var checkItems: Map<String, List<OrderItem>>? = null,
    var isReturnOrder: Boolean = false,
    @ServerTimestamp var created: Timestamp? = null
) {
    constructor() : this(null, null, null, null, false, null)
}