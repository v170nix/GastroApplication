package net.arwix.gastro.client.data

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Transaction
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.TableGroup

class OpenTableRepository(private val firestoreDbApp: FirestoreDbApp) {

    fun addOrder(
        transaction: Transaction,
        tableGroup: TableGroup,
        orderReference: DocumentReference,
        price: Long = 0L
    ): (transaction: Transaction) -> Unit {
        val doc =
            transaction.get(firestoreDbApp.refs.openTables.document(tableGroup.toDocId()))

        return {
            if (doc.exists()) {
                transaction.update(
                    doc.reference,
                    mapOf(
                        "orders" to FieldValue.arrayUnion(orderReference),
                        "updatedTime" to FieldValue.serverTimestamp(),
                        "summaryOrderPrice" to FieldValue.increment(price)
                    )
                )
            } else {
                transaction.set(
                    doc.reference,
                    OpenTableData(orders = listOf(orderReference), summaryOrderPrice = price)
                )
            }
        }

    }

}