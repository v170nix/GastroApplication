package net.arwix.gastro.client.feature.table.domain

import androidx.collection.arrayMapOf
import com.google.firebase.firestore.Transaction
import net.arwix.gastro.client.data.CheckRepository
import net.arwix.gastro.client.data.OpenTableRepository
import net.arwix.gastro.client.data.OrderRepository
import net.arwix.gastro.client.feature.order.domain.OrderUseCase
import net.arwix.gastro.client.feature.table.data.MutableTableItems
import net.arwix.gastro.client.feature.table.data.OpenTableItem
import net.arwix.gastro.client.feature.table.data.TableItems
import net.arwix.gastro.client.feature.table.data.filterPayItems
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.*
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderItems

class OpenTableUseCase(
    private val firestoreDbApp: FirestoreDbApp,
    private val openTableRepository: OpenTableRepository,
    private val orderRepository: OrderRepository,
    private val checkRepository: CheckRepository,
    private val orderUseCase: OrderUseCase
) {

    suspend fun split(
        waiterId: Int,
        openTableData: OpenTableData,
        fromTableGroup: TableGroup,
        tableItems: TableItems,
        toTableGroup: TableGroup
    ) {
        val filteredItems: Map<MenuGroupName, List<OpenTableItem>> = tableItems.filterPayItems()
        if (filteredItems.isEmpty()) return
        val orderItems: OrderItems = filteredItems.mapValues { (_, list) ->
            list.map { it.orderItem.copy(count = it.payCount) }
        }
        firestoreDbApp.runTransaction {
            val checkoutWorker = checkoutWorker(
                it, waiterId, openTableData, fromTableGroup, tableItems,
                isReturnOrder = false,
                isSplitOrder = true,
                splitToTableGroup = toTableGroup
            )
            val (_, orderWorker) = orderUseCase.submitWorker(
                transaction = it,
                userId = waiterId,
                tableGroup = toTableGroup,
                orderItems = orderItems,
                fromTableGroup = fromTableGroup
            )
            checkoutWorker(it)
            orderWorker(it)
        }.await()
    }

    fun checkoutWorker(
        transaction: Transaction,
        waiterId: Int,
        openTableData: OpenTableData,
        tableGroup: TableGroup,
        tableItems: TableItems,
        isReturnOrder: Boolean = false,
        isSplitOrder: Boolean = false,
        splitToTableGroup: TableGroup? = null
    ): (transaction: Transaction) -> Unit {
        val filteredItems = tableItems.filterPayItems()
        if (filteredItems.isEmpty()) return { _ -> }

        if (!openTableRepository.isLatestData(transaction, tableGroup, openTableData.updatedTime)
        ) return { _ -> }

        val checkItems: CheckItems = filteredItems.mapValues { (_, list) ->
            list.map { it.orderItem.copy(count = it.payCount) }
        }
        val summaryPrice = checkItems.values.sumBy { list ->
            list.sumBy { (it.count * it.price).toInt() }
        }
        val residualCount = tableItems.values.sumBy { list ->
            list.sumBy { it.orderItem.count - it.checkCount - it.payCount }
        }

        val checkData = CheckData(
            waiterId = waiterId,
            table = tableGroup.tableId,
            tablePart = tableGroup.tablePart,
            checkItems = checkItems,
            isReturnOrder = isReturnOrder,
            isSplitOrder = isSplitOrder,
            splitToTable = splitToTableGroup?.tableId,
            splitToTablePart = splitToTableGroup?.tablePart
        )

        val (checkRef, submitCheckWork) = checkRepository.submitCheckWorker(checkData)
        val openTableAddCheckWorker = if (residualCount != 0) openTableRepository.applyCheck(
            transaction,
            tableGroup,
            checkRef,
            summaryPrice.toLong(),
            isReturnOrder,
            isSplitOrder
        ) else null

        return { it: Transaction ->
            submitCheckWork(it)
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
                require(openTableAddCheckWorker != null)
                openTableAddCheckWorker(it)
            }
        }
    }

    suspend fun checkout(
        waiterId: Int,
        openTableData: OpenTableData,
        tableGroup: TableGroup,
        tableItems: TableItems,
        isReturnOrder: Boolean = false,
        isSplitOrder: Boolean = false
    ) {
        firestoreDbApp.runTransaction {
            val worker = checkoutWorker(
                it,
                waiterId,
                openTableData,
                tableGroup,
                tableItems,
                isReturnOrder,
                isSplitOrder
            )
            worker(it)
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