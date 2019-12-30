package net.arwix.gastro.client.ui.order

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.epson.epos2.Epos2Exception
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.arwix.gastro.client.domain.PrinterOrderUseCase
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.OpenTableData
import net.arwix.gastro.library.data.TableGroup
import net.arwix.gastro.library.menu.data.MenuGridItem
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupDoc
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.order.data.OrderItem
import net.arwix.gastro.library.toFlow
import net.arwix.mvi.SimpleIntentViewModel

class OrderViewModel(
    private val firestoreDbApp: FirestoreDbApp,
    private val context: Context
) :
    SimpleIntentViewModel<OrderViewModel.Action, OrderViewModel.Result, OrderViewModel.State>() {

    private var menuGroupData: List<MenuGroupData> = mutableListOf()
    override var internalViewState: State = State()

    init {
        viewModelScope.launch {
            firestoreDbApp.refs.menu.orderBy("order").toFlow()
                .collect { docs ->
                    val menus = docs.map { it.toObject(MenuGroupDoc::class.java).toMenuData(it.id) }
                    menuGroupData = menus
                    notificationFromObserver(Result.AddMenu(menus))
                }
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> = liveData {
        when (action) {
            is Action.AddItem -> emit(Result.AddItem(action.typeItem, action.item))
//            is Action.EditItem -> emit(Result.EditItem(action.typeItem, action.item))
            is Action.ChangeCountItem -> emit(
                Result.ChangeCountItem(
                    action.typeItem,
                    action.item,
                    action.delta
                )
            )

            is Action.AddItems -> {
                if (action.items.isEmpty()) return@liveData
                withContext(Dispatchers.Main) {
                    action.items.map {
                        notificationFromObserver(
                            Result.AddItem(
                                it.menu.name,
                                OrderItem(
                                    it.value.name!!,
                                    it.value.price,
                                    1
                                )
                            )
                        )
                    }
                }
            }

            is Action.SubmitOrder -> {
                withContext(Dispatchers.Main) {
                    val orders = firestoreDbApp.refs.orders
                    var orderData =
                        OrderData(
                            action.userId,
                            internalViewState.tableGroup!!.tableId,
                            internalViewState.tableGroup!!.tablePart,
                            null,
                            internalViewState.orderItems.filter {
                                it.value.isNotEmpty()
                            }.let { filterMap: Map<MenuGroupData, List<OrderItem>> ->
                                mutableMapOf<String, List<OrderItem>>().apply {
                                    filterMap.forEach { (key, list) ->
                                        val filterList = list.filter { it.count > 0 }
                                        if (filterList.isNotEmpty()) {
                                            this[key.name] = filterList
                                        }
                                    }
                                }
                            }
                        )
                    if (!orderData.orderItems.isNullOrEmpty()) {
                        //   orders.add(orderData).await()!!
                        val orderDoc = orders.document()
                        val tableId = orderData.table.toString().toIntOrNull() ?: return@withContext
                        val tablePart =
                            orderData.tablePart.toString().toIntOrNull() ?: return@withContext
                        val docId = "$tableId-$tablePart"
                        firestoreDbApp.firestore.runTransaction {
                            // ------- update bans
                            val prefs = firestoreDbApp.getGlobalPrefs(it)!!
                            var initOrderBon = prefs.orderBon
                            val bonNumbers = mutableMapOf<String, Int>()
                            orderData.orderItems!!.keys.forEach { menuGroupName ->
                                initOrderBon++
                                bonNumbers[menuGroupName] = initOrderBon
                            }
                            orderData = orderData.copy(bonNumbers = bonNumbers)
                            // -------------------
                            val openTableDoc =
                                it.get(firestoreDbApp.refs.openTables.document(docId))
                            it.set(orderDoc, orderData)
                            firestoreDbApp.setGlobalPrefs(it, prefs.copy(orderBon = initOrderBon))
                            if (openTableDoc.exists()) {
                                it.update(
                                    openTableDoc.reference,
                                    "orders",
                                    FieldValue.arrayUnion(orderDoc)
                                )
                                it.update(
                                    openTableDoc.reference,
                                    "updated",
                                    FieldValue.serverTimestamp()
                                )
                            } else {
                                val openTableData = openTableDoc.toObject(OpenTableData::class.java)
                                val newOpenTableData = OpenTableData(
                                    orders = openTableData?.orders?.run { this + orderDoc }
                                        ?: listOf(orderDoc)
                                )
                                it.set(openTableDoc.reference, newOpenTableData)
                            }
                        }.await()

                        withContext(Dispatchers.IO) {
                            orderDoc.get().await()
                            val openTableDoc =
                                orderDoc.get().await()!!.toObject(OrderData::class.java)!!
                            val result = print(context, openTableDoc)
                            emit(Result.SubmitOrder(result))
                        }
                        return@withContext
                    }
                }
                emit(Result.SubmitOrder())
            }
        }
    }

    suspend fun print(context: Context, orderData: OrderData): List<Int> {
//        var orderBonNumber = sharedPreferences.getLong("orderBonNumber", 120555)
        val printers = transformMenuToPrinters(menuGroupData)
        val partsOrders = transformOrders(printers, orderData)
        val resultCodes = mutableListOf<Int>()

        partsOrders.forEach { (printerAddress, orderData) ->
            Log.e("printerOld", printerAddress)
            orderData.runCatching {
                PrinterOrderUseCase(context).printOrder(printerAddress, orderData)
            }.onSuccess {
                //                orderBonNumber = it.first
                resultCodes.add(it)
            }.onFailure {
                if (it is Epos2Exception) {
                    resultCodes.add(it.errorStatus)
                }
            }
        }
//        sharedPreferences.edit().putLong("orderBonNumber", orderBonNumber).apply()
        return resultCodes
    }

    fun clear() {
        notificationFromObserver(Result.Clear)
    }

    fun selectTable(tableGroup: TableGroup) {
        notificationFromObserver(Result.InitOrder(tableGroup))
    }

    private fun transformMenuToPrinters(menuGroups: List<MenuGroupData>): MutableMap<String, MutableList<String>> {
        // address list menu
        val printersAddress = mutableMapOf<String, MutableList<String>>()
        menuGroups.forEach {
            val printer = it.printer ?: return@forEach
            val items = printersAddress.getOrPut(printer) {
                mutableListOf()
            }
            items.add(it.name)
        }
        return printersAddress
    }

    private fun transformOrders(
        printerMap: Map<String, List<String>>,
        summaryOrderData: OrderData
    ): Map<String, OrderData> {
        val result = mutableMapOf<String, OrderData>()
        summaryOrderData.orderItems!!.forEach { (menu, listOrders) ->
            var printerAddress: String? = null
            printerMap.forEach printerMap@{ (printer, partMenusInPrinter) ->
                if (partMenusInPrinter.indexOf(menu) > -1) {
                    printerAddress = printer
                    return@printerMap
                }
            }
            if (printerAddress == null) throw IllegalStateException("menu error")
            val orderData = result.getOrPut(printerAddress!!) {
                summaryOrderData.copy(
                    orderItems = mutableMapOf()
                )
            }
            val orderItemsMap = orderData.orderItems as MutableMap
            val orderItemsList = orderItemsMap.getOrPut(menu) {
                mutableListOf()
            } as MutableList
            orderItemsList.addAll(listOrders)
        }
        return result
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.InitOrder -> {
                internalViewState.copy(
                    tableGroup = result.tableGroup,
                    orderItems = mutableMapOf<MenuGroupData, List<OrderItem>>().apply {
                        menuGroupData.forEach {
                            this[it] = listOf()
                        }
//                        menuTypes?.forEach { menuType ->
//                            this[menuType] = listOf()
//                        }
                    }
                )
            }

            Result.Clear -> internalViewState.copy(
                orderItems = internalViewState.orderItems.apply {
                    forEach { (menuType, _) -> this[menuType] = listOf() }
                },
                isSubmit = false
            )
            is Result.AddMenu -> {
                internalViewState.copy(
                    isLoadingMenu = false,
                    orderItems = mutableMapOf<MenuGroupData, List<OrderItem>>().apply {
                        result.list.forEach { menuGroup -> this[menuGroup] = listOf() }
                    }
                )

            }
            is Result.AddItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
                    val menuGroupData = this.keys.find {
                        it.name == result.typeItem
                    } ?: return@apply
                    val orderItems = this[menuGroupData]
                    if (orderItems == null) this[menuGroupData] = listOf(result.item)
                    else {
                        // проверка на одинаковость
                        val listOrderItem = orderItems.find {
                            it.name == result.item.name && it.price == result.item.price
                        }
                        if (listOrderItem == null) {
                            this[menuGroupData] = orderItems + result.item
                        } else {
                            this[menuGroupData] = orderItems.toMutableList().also {
                                val indexItem = it.indexOf(listOrderItem)
                                it[indexItem] =
                                    listOrderItem.copy(count = listOrderItem.count + result.item.count)
                            }
                        }
                    }
                })
            }
//            is Result.EditItem -> {
//                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
//                    val menuGroupData = this.keys.find {
//                        it.name == result.typeItem
//                    } ?: return@apply
//                    val orderItems = this[menuGroupData] ?: return@apply
//                    val current = orderItems.map {
//                        if (it.name == result.item.name) result.item else it
//                    }
//                    this[menuGroupData] = current
//                })
//            }
            is Result.SubmitOrder -> {
                internalViewState.copy(isSubmit = true, resultPrint = result.resultPrint)
            }
            is Result.ChangeCountItem -> {
                internalViewState.copy(orderItems = internalViewState.orderItems.apply {
                    val orderItems = this[result.typeItem] ?: return@apply
                    this[result.typeItem] = orderItems.map {
                        if (it.name == result.item.name) it.copy(count = it.count + result.delta) else it
                    }
                })
            }
        }
    }

    sealed class Action {
        data class AddItem(val typeItem: String, val item: OrderItem) : Action()
        data class AddItems(val items: Set<MenuGridItem.Item>) : Action()
//        data class EditItem(val typeItem: String, val item: OrderItem) :
//            Action()

        data class ChangeCountItem(
            val typeItem: MenuGroupData,
            val item: OrderItem,
            val delta: Int
        ) : Action()

        data class SubmitOrder(val userId: Int) : Action()
    }

    sealed class Result {
        object Clear : Result()
        data class InitOrder(val tableGroup: TableGroup) : Result()
        data class AddMenu(val list: List<MenuGroupData>) : Result()
        data class AddItem(val typeItem: String, val item: OrderItem) : Result()
//        data class EditItem(val typeItem: String, val item: OrderItem) :
//            Result()

        data class ChangeCountItem(
            val typeItem: MenuGroupData,
            val item: OrderItem,
            val delta: Int
        ) : Result()

        data class SubmitOrder(val resultPrint: List<Int>? = null) : Result()
    }


    data class State(
        val isLoadingMenu: Boolean = true,
        val tableGroup: TableGroup? = null,
        val orderItems: MutableMap<MenuGroupData, List<OrderItem>> = mutableMapOf(),
        val resultPrint: List<Int>? = null,
        val isSubmit: Boolean = false
    )

}