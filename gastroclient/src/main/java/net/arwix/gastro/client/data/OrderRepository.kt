package net.arwix.gastro.client.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import net.arwix.gastro.library.await
import net.arwix.gastro.library.order.data.OrderData

class OrderRepository(private val orderReference: CollectionReference) {

    suspend fun getOrder(documentPath: String): OrderData {
        return orderReference.document(documentPath).get().await()?.toObject(OrderData::class.java)!!
    }

    suspend fun getOrders(ordersRef: List<DocumentReference>): List<OrderData> = supervisorScope {
        ordersRef.chunked(10)
            .map {
                async(Dispatchers.IO) {
                    orderReference.whereIn(
                        FieldPath.documentId(),
                        it
                    ).get().await()
                }
            }
            .awaitAll()
            .filterNotNull()
            .map { it.toObjects(OrderData::class.java) }
            .flatten()
    }

    fun submitOrder(order: OrderData): Pair<DocumentReference, (transaction: Transaction) -> Unit> {
        val orderRef = orderReference.document()
        return orderRef to { transaction ->
            transaction.set(orderRef, order)
        }
    }
}