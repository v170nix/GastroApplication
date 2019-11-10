package net.arwix.gastro.library

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
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
        if (snapshot != null && !snapshot.isEmpty) {
            offer(snapshot)
        }
    }
//        invokeOnClose {
//            registration.remove()
//        }
    awaitClose {
        cancel()
        Log.e("snapshotFlow", "cancel")
        registration.remove()
    }
}

fun Query.toFlow() = callbackFlow<QuerySnapshot> {
    val registration = this@toFlow.addSnapshotListener { snapshot, e ->
        if (e != null) {
            cancel(CancellationException("API Error", e))
            return@addSnapshotListener
        }
        if (snapshot != null && !snapshot.isEmpty) {
            offer(snapshot)
        }
    }
//        invokeOnClose {
//            registration.remove()
//        }
    awaitClose {
        cancel()
        Log.e("snapshotFlow", "cancel")
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