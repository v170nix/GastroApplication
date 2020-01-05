package net.arwix.gastro.client.data

import androidx.collection.arrayMapOf
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.order.data.OrderItem

class OrderRepository(
    private val firestoreDbApp: FirestoreDbApp,
    private val openTableRepository: OpenTableRepository
) {

    suspend fun getOrder(documentPath: String): OrderData {
        return firestoreDbApp.refs.orders.document(documentPath).get().await()?.toObject(OrderData::class.java)!!
    }

    suspend fun getOrders(ordersRef: List<DocumentReference>): List<OrderData> = supervisorScope {
        ordersRef.chunked(10)
            .map {
                async(Dispatchers.IO) {
                    firestoreDbApp.refs.orders.whereIn(
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

    suspend fun submit(
        userId: Int,
        tableGroup: TableGroup,
        orderItems: Map<MenuGroupData, List<OrderItem>>
    ): DocumentReference? {
        var orderData = OrderData(
            waiterId = userId,
            table = tableGroup.tableId,
            tablePart = tableGroup.tablePart,
            bonNumbers = null,
            orderItems = filterItems(orderItems),
            created = null
        )
        val orderReference = firestoreDbApp.refs.orders.document()
        if (orderData.orderItems.isNullOrEmpty()) return null
        firestoreDbApp.firestore.runTransaction {
            val prefs = firestoreDbApp.getGlobalPrefs(it)
            orderData = orderData.copy(bonNumbers = getBons(prefs.orderBon, orderData.orderItems))
            val openTableUpdate =
                openTableRepository.addOrder(it, tableGroup, orderReference, orderData.sum())
            firestoreDbApp.setGlobalPrefs(
                it,
                prefs.copy(orderBon = prefs.orderBon + orderData.bonNumbers!!.size)
            )
            it.set(orderReference, orderData)
            openTableUpdate(it)
        }.await()
        return orderReference
    }

    companion object {

        private fun getBons(
            initBon: Int,
            orderItems: Map<MenuGroupName, List<OrderItem>>
        ): Map<String, Int> {
            val bonNumbers = arrayMapOf<String, Int>()
            orderItems.keys.forEachIndexed { index, menu ->
                bonNumbers[menu] = initBon + index + 1
            }
            return bonNumbers
        }

        private fun filterItems(orderItems: Map<MenuGroupData, List<OrderItem>>): Map<MenuGroupName, List<OrderItem>> {
            val result = arrayMapOf<MenuGroupName, List<OrderItem>>()
            orderItems.forEach { (menu, orders) ->
                orders
                    .filter { it.count > 0 }
                    .ifEmpty { return@forEach }
                    .also {
                        result[menu.name] = it
                    }
            }
            return result
        }
    }
}