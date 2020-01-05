package net.arwix.gastro.client.feature.order.domain

import androidx.collection.arrayMapOf
import com.google.firebase.firestore.DocumentReference
import net.arwix.gastro.client.data.OpenTableRepository
import net.arwix.gastro.client.data.OrderRepository
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.order.data.OrderItem
import net.arwix.gastro.library.order.data.OrderItems

class OrderUseCase(
    private val firestoreDbApp: FirestoreDbApp,
    private val openTableRepository: OpenTableRepository,
    private val orderRepository: OrderRepository
) {

    suspend fun submit(
        userId: Int,
        tableGroup: TableGroup,
        orderItems: Map<MenuGroupData, List<OrderItem>>
    ): DocumentReference? {
        val filteredItems = filterItems(orderItems)
        if (filteredItems.isNullOrEmpty()) return null

        var orderData = OrderData(
            waiterId = userId,
            table = tableGroup.tableId,
            tablePart = tableGroup.tablePart,
            bonNumbers = null,
            orderItems = filteredItems,
            created = null
        )
//        val orderReference = firestoreDbApp.refs.orders.document()

        return firestoreDbApp.runTransaction {
            val prefs = firestoreDbApp.getGlobalPrefs(it)
            orderData = orderData.copy(bonNumbers = getBons(prefs.orderBon, orderData.orderItems))
            val (orderRef, orderSubmitWorker) = orderRepository.submitOrder(orderData)
            val openTableWorker =
                openTableRepository.addOrder(it, tableGroup, orderRef, orderData.sum())
            firestoreDbApp.setGlobalPrefs(
                it,
                prefs.copy(orderBon = prefs.orderBon + orderData.bonNumbers!!.size)
            )
            orderSubmitWorker(it)
            openTableWorker(it)
            orderRef
        }.await()
    }

    private companion object {

        private fun getBons(initBon: Int, orderItems: OrderItems): Map<String, Int> {
            val bonNumbers = arrayMapOf<String, Int>()
            orderItems.keys.forEachIndexed { index, menu ->
                bonNumbers[menu] = initBon + index + 1
            }
            return bonNumbers
        }

        private fun filterItems(orderItems: Map<MenuGroupData, List<OrderItem>>): OrderItems {
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