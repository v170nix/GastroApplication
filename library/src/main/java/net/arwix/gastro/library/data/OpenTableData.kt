package net.arwix.gastro.library.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp

data class OpenTableData constructor(
    var parts: List<DocumentReference>? = null,
    @ServerTimestamp var updated: Timestamp? = null
) {

}