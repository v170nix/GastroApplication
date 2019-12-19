package net.arwix.gastro.library

import com.google.firebase.firestore.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


fun CollectionReference.toFlow() = callbackFlow<QuerySnapshot> {
    val registration = this@toFlow.addSnapshotListener { snapshot, e ->
        if (e != null) {
            cancel(CancellationException("API Error", e))
            return@addSnapshotListener
        }
        if (snapshot != null) {
            offer(snapshot)
        }
    }
    awaitClose {
        cancel()
        registration.remove()
    }
}

fun DocumentReference.toFlow() = callbackFlow<DocumentSnapshot> {
    val registration = this@toFlow.addSnapshotListener { snapshot, e ->
        if (e != null) {
            cancel(CancellationException("API Error", e))
            return@addSnapshotListener
        }
        if (snapshot != null) {
            offer(snapshot)
        }
    }
    awaitClose {
        cancel()
        registration.remove()
    }
}

fun Query.toFlow() = callbackFlow<QuerySnapshot> {
    val registration = this@toFlow.addSnapshotListener { snapshot, e ->
        if (e != null) {
            cancel(CancellationException("API Error", e))
            return@addSnapshotListener
        }
        if (snapshot != null) {
            offer(snapshot)
        }
    }
    awaitClose {
        cancel()
        registration.remove()
    }
}

//
//orders.addSnapshotListener { snapshot: QuerySnapshot?, e ->
//    if (e != null) {
//        Log.e("FirestoreService", "Listen failed", e)
//        return@addSnapshotListener
//    }
//    if (snapshot != null && !snapshot.isEmpty) {
//        Log.e("Snapshot", snapshot.documents.toString())
//    } else {
//        Log.e("Snapshot", "null")
//    }
//}