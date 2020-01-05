package net.arwix.gastro.client.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.flow.map
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.toFlow

class OpenTableRepository(private val openTables: CollectionReference) {

    fun asFlow(tableGroup: TableGroup) =
        openTables.document(tableGroup.toDocId()).toFlow().map {
            it.toObject(OpenTableData::class.java)
        }

    fun deleteTable(transaction: Transaction, tableGroup: TableGroup) {
        transaction.delete(openTables.document(tableGroup.toDocId()))
    }

    fun isLatestData(
        transaction: Transaction,
        tableGroup: TableGroup,
        updatedTime: Timestamp?
    ): Boolean {
        val serverOrderData =
            transaction.get(openTables.document(tableGroup.toDocId())).toObject(OpenTableData::class.java)
                ?: return false
        return serverOrderData.updatedTime == updatedTime
    }

    fun addCheck(
        transaction: Transaction,
        tableGroup: TableGroup,
        checkReference: DocumentReference,
        price: Long,
        isReturnOrder: Boolean = false,
        isSplitOrder: Boolean = false
    ): (transaction: Transaction) -> Unit {
        val doc = transaction.get(openTables.document(tableGroup.toDocId()))
        return {
            if (!doc.exists()) throw IllegalStateException("doc not exits $tableGroup")

            val fields = if (isReturnOrder || isSplitOrder) mapOf(
                "checks" to FieldValue.arrayUnion(checkReference),
                "updatedTime" to FieldValue.serverTimestamp(),
                "summaryOrderPrice" to FieldValue.increment(-price)
            ) else mapOf(
                "checks" to FieldValue.arrayUnion(checkReference),
                "updatedTime" to FieldValue.serverTimestamp(),
                "summaryPayPrice" to FieldValue.increment(price)
            )
            it.update(doc.reference, fields)
        }
    }

    fun addOrder(
        transaction: Transaction,
        tableGroup: TableGroup,
        orderReference: DocumentReference,
        price: Long = 0L
    ): (transaction: Transaction) -> Unit {
        val doc =
            transaction.get(openTables.document(tableGroup.toDocId()))

        return {
            if (doc.exists()) {
                it.update(
                    doc.reference,
                    mapOf(
                        "orders" to FieldValue.arrayUnion(orderReference),
                        "updatedTime" to FieldValue.serverTimestamp(),
                        "summaryOrderPrice" to FieldValue.increment(price)
                    )
                )
            } else {
                it.set(
                    doc.reference,
                    OpenTableData(orders = listOf(orderReference), summaryOrderPrice = price)
                )
            }
        }
    }

}