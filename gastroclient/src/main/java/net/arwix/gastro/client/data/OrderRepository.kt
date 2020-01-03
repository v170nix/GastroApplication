package net.arwix.gastro.client.data

import androidx.collection.arrayMapOf
import com.google.firebase.firestore.DocumentReference
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
            orderItems = filterItems(
                orderItems
            ),
            created = null
        )
        val orderReference = firestoreDbApp.refs.orders.document()
        if (orderData.orderItems.isNullOrEmpty()) return null
        firestoreDbApp.firestore.runTransaction {
            val prefs = firestoreDbApp.getGlobalPrefs(it)
            orderData = orderData.copy(
                bonNumbers = getBons(
                    prefs.orderBon,
                    orderData.orderItems
                )
            )
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

        fun getBons(
            initBon: Int,
            orderItems: Map<MenuGroupName, List<OrderItem>>
        ): Map<String, Int> {
            val bonNumbers = arrayMapOf<String, Int>()
            orderItems.keys.forEachIndexed { index, menu ->
                bonNumbers[menu] = initBon + index + 1
            }
            return bonNumbers
        }

        fun filterItems(orderItems: Map<MenuGroupData, List<OrderItem>>): Map<MenuGroupName, List<OrderItem>> {
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
//            orderItems.filter {
//                it.value.isNotEmpty()
//            }.let { filterMap: Map<MenuGroupData, List<OrderItem>> ->
//                mutableMapOf<String, List<OrderItem>>().apply {
//                    filterMap.forEach { (key, list) ->
//                        val filterList = list.filter { it.count > 0 }
//                        if (filterList.isNotEmpty()) {
//                            this[key.name] = filterList
//                        }
//                    }
//                }
//            }
        }
    }
}