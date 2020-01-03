package net.arwix.gastro.library.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import net.arwix.gastro.library.preference.PrefGlobalData

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
            closeTables = firestore.collection(prefix + "close tables"),
            prefs = firestore.collection(prefix + "pref")
        )
    }

    fun getGlobalPrefs(transaction: Transaction): PrefGlobalData {
        return transaction.get(refs.prefs.document("global")).toObject(PrefGlobalData::class.java)!!
    }

    fun setGlobalPrefs(transaction: Transaction, pref: PrefGlobalData) {
        transaction.set(refs.prefs.document("global"), pref)
    }

    data class CollectionRef(
        val menu: CollectionReference,
        val orders: CollectionReference,
        val openTables: CollectionReference,
        val checks: CollectionReference,
        val closeTables: CollectionReference,
        val prefs: CollectionReference
    )


}