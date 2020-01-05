package net.arwix.gastro.client.feature.table.domain

import androidx.collection.arrayMapOf
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Transaction
import net.arwix.gastro.client.data.CheckRepository
import net.arwix.gastro.client.data.OpenTableRepository
import net.arwix.gastro.client.data.OrderRepository
import net.arwix.gastro.client.feature.table.data.MutableTableItems
import net.arwix.gastro.client.feature.table.data.OpenTableItem
import net.arwix.gastro.client.feature.table.data.TableItems
import net.arwix.gastro.client.feature.table.data.filterPayItems
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.*
import net.arwix.gastro.library.menu.data.MenuGroupName

class OpenTableUseCase(
    private val firestoreDbApp: FirestoreDbApp,
    private val openTableRepository: OpenTableRepository,
    private val orderRepository: OrderRepository,
    private val checkRepository: CheckRepository
) {

    suspend fun checkout(
        waiterId: Int,
        openTableData: OpenTableData,
        tableGroup: TableGroup,
        tableItems: TableItems,
        isReturnOrder: Boolean = false,
        isSplitOrder: Boolean = false
    ) {
        val filterItems = tableItems.filterPayItems()
        if (filterItems.isEmpty()) return

        val checkItems: CheckItems = filterItems.mapValues { (_, list) ->
            list.map { it.orderItem.copy(count = it.payCount) }
        }
        val summaryPrice = checkItems.values.sumBy { list ->
            list.sumBy { (it.count * it.price).toInt() }
        }
        val residualCount = tableItems.values.sumBy { list ->
            list.sumBy { it.orderItem.count - it.payCount - it.checkCount - it.splitCount }
        }

        val checkData = CheckData(
            waiterId = waiterId,
            table = tableGroup.tableId,
            tablePart = tableGroup.tablePart,
            checkItems = checkItems,
            isReturnOrder = isReturnOrder,
            isSplitOrder = isSplitOrder
        )

        firestoreDbApp.runTransaction {
            if (!openTableRepository.isLatestData(
                    it,
                    tableGroup,
                    openTableData.updatedTime
                )
            ) return@runTransaction
            val (checkRef: DocumentReference, checkSubmitWork: (transaction: Transaction) -> Unit) =
                checkRepository.submitCheck(checkData)

            if (residualCount == 0) {
                // close table
                val closeTableData = CloseTableData(
                    table = tableGroup.tableId,
                    tablePart = tableGroup.tablePart,
                    orders = openTableData.orders,
                    checks = openTableData.checks.run { this + checkRef },
                    summaryOrderPrice = openTableData.summaryOrderPrice -
                            if (isReturnOrder || isSplitOrder) summaryPrice else 0,
                    summaryPayPrice = openTableData.summaryPayPrice +
                            if (isReturnOrder || isSplitOrder) 0 else summaryPrice
                )
                it.set(firestoreDbApp.refs.closeTables.document(), closeTableData)
                openTableRepository.deleteTable(it, tableGroup)
            } else {
                val openTableAddCheckWork: (transaction: Transaction) -> Unit =
                    openTableRepository.addCheck(
                        it,
                        tableGroup,
                        checkRef,
                        summaryPrice.toLong(),
                        isReturnOrder,
                        isSplitOrder
                    )
                openTableAddCheckWork(it)
            }
            checkSubmitWork(it)
        }.await()
    }

    suspend fun getItems(openTableData: OpenTableData) =
        applyChecks(openTableData, getSummaryOrder(openTableData))


    private suspend fun applyChecks(
        openTableData: OpenTableData,
        tableItems: MutableTableItems
    ): MutableTableItems = checkRepository.getChecks(openTableData.checks)
        .fold(tableItems) { acc, checkData ->
            checkData.checkItems.forEach { (menu, checks) ->
                val accList = acc.getOrPut(menu) { mutableListOf() }
                checks.forEach { check ->
                    accList.merge(
                        predicate = { it.orderItem.name == check.name && it.orderItem.price == check.price }
                    ) { listItem ->
                        listItem?.copy(
                            checkCount = listItem.checkCount + check.count,
                            returnCount = listItem.returnCount + if (checkData.isReturnOrder) check.count else 0,
                            splitCount = listItem.splitCount + if (checkData.isSplitOrder) check.count else 0
                        ) ?: throw IllegalStateException("pay order error sync item")
                    }
                }
            }
            acc
        }


    private suspend fun getSummaryOrder(openTableData: OpenTableData): MutableTableItems =
        orderRepository.getOrders(openTableData.orders)
            .fold(arrayMapOf<MenuGroupName, MutableList<OpenTableItem>>()) { acc, orderData ->
                orderData.orderItems.forEach { (menu, orders) ->
                    val accList = acc.getOrPut(menu) { mutableListOf() }
                    orders.forEach { order ->
                        accList.merge(
                            predicate = { it.orderItem.name == order.name && it.orderItem.price == order.price }
                        ) { listItem ->
                            listItem?.copy(
                                orderItem = order.copy(count = listItem.orderItem.count + order.count)
                            ) ?: OpenTableItem(
                                orderItem = order
                            )
                        }
                    }
                }
                acc
            }
}

inline fun <T> MutableList<T>.merge(predicate: (T) -> Boolean, block: (listItem: T?) -> T) {
    find(predicate)?.let {
        this[indexOf(it)] = block(it)
    } ?: run {
        this.add(block(null))
    }
}