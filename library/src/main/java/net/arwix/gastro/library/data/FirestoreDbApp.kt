package net.arwix.gastro.library.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreDbApp(
    prefix: String = "",
    val firestore: FirebaseFirestore
) {

    val refs: CollectionRef

    init {
        refs = CollectionRef(
            menu = firestore.collection(prefix + "menu"),
            orders = firestore.collection(prefix + "orders"),
            openTables = firestore.collection(prefix + "open tables"),
            checks = firestore.collection(prefix + "checks"),
            closeTables = firestore.collection(prefix + "close tables")
        )

//        GlobalScope.launch {
//            val oldMenuRef = firestore.collection("menu")
//            oldMenuRef.get().await()?.forEach {
//                refs.menu.document(it.id).set(it.data).await()
//            }
//        }

    }

    data class CollectionRef(
        val menu: CollectionReference,
        val orders: CollectionReference,
        val openTables: CollectionReference,
        val checks: CollectionReference,
        val closeTables: CollectionReference
    )


}