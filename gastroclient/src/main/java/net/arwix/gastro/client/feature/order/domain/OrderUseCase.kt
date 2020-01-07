package net.arwix.gastro.client.feature.order.domain

import androidx.collection.arrayMapOf
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Transaction
import net.arwix.gastro.client.data.OpenTableRepository
import net.arwix.gastro.client.data.OrderRepository
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.order.data.OrderItem
import net.arwix.gastro.library.order.data.OrderItems

class OrderUseCase(
    private val firestoreDbApp: FirestoreDbApp,
    private val openTableRepository: OpenTableRepository,
    private val orderRepository: OrderRepository
) {

    fun submitWorker(
        transaction: Transaction,
        userId: Int,
        tableGroup: TableGroup,
        orderItems: Map<MenuGroupName, List<OrderItem>>,
        fromTableGroup: TableGroup? = null
    ): Pair<DocumentReference?, (transaction: Transaction) -> Unit> {
        val filteredItems = filterItems(orderItems)
        if (filteredItems.isNullOrEmpty()) return null to { _ -> }

        var orderData = OrderData(
            waiterId = userId,
            table = tableGroup.tableId,
            tablePart = tableGroup.tablePart,
            bonNumbers = null,
            orderItems = filteredItems,
            created = null,
            fromSplitTable = fromTableGroup?.tableId,
            fromSplitTablePart = fromTableGroup?.tablePart
        )

        val prefs = firestoreDbApp.getGlobalPrefs(transaction)
        orderData = orderData.copy(bonNumbers = getBons(prefs.orderBon, orderData.orderItems))
        val (orderRef, orderSubmitWorker) = orderRepository.submitOrder(orderData)
        val openTableWorker =
            openTableRepository.addOrder(transaction, tableGroup, orderRef, orderData.sum())
        return orderRef to { trans: Transaction ->
            firestoreDbApp.setGlobalPrefs(
                trans,
                prefs.copy(orderBon = prefs.orderBon + orderData.bonNumbers!!.size)
            )
            orderSubmitWorker(trans)
            openTableWorker(trans)
        }
    }

    suspend fun submit(
        userId: Int,
        tableGroup: TableGroup,
        orderItems: Map<MenuGroupName, List<OrderItem>>
    ): DocumentReference? {
        return firestoreDbApp.runTransaction {
            val (ref, putWorker) = submitWorker(it, userId, tableGroup, orderItems)
            putWorker(it)
            ref
        }.await()
    }

    companion object {
        fun getBons(initBon: Int, orderItems: OrderItems): Map<String, Int> {
            val bonNumbers = arrayMapOf<String, Int>()
            orderItems.keys.forEachIndexed { index, menu ->
                bonNumbers[menu] = initBon + index + 1
            }
            return bonNumbers
        }

        private fun filterItems(orderItems: Map<MenuGroupName, List<OrderItem>>): OrderItems {
            val result = arrayMapOf<MenuGroupName, List<OrderItem>>()
            orderItems.forEach { (menu, orders) ->
                orders
                    .filter { it.count > 0 }
                    .ifEmpty { return@forEach }
                    .also {
                        result[menu] = it
                    }
            }
            return result
        }
    }
}