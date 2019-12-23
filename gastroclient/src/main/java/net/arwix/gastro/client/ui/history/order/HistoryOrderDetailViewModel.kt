package net.arwix.gastro.client.ui.history.order

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import net.arwix.gastro.client.domain.PrinterOrderUseCase
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.FirestoreDbApp
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupDoc
import net.arwix.mvi.SimpleIntentViewModel

class HistoryOrderDetailViewModel(
    private val firestoreDbApp: FirestoreDbApp,
    private val sharedPreferences: SharedPreferences
) : SimpleIntentViewModel<HistoryOrderDetailViewModel.Action, HistoryOrderDetailViewModel.Result, HistoryOrderDetailViewModel.State>() {

    private lateinit var menuGroupTypes: List<MenuGroupData>

    init {
        viewModelScope.launch {
            val doc = firestoreDbApp.refs.menu.orderBy("order").get().await()!!
            val menu =
                doc.documents.map { it.toObject(MenuGroupDoc::class.java)!!.toMenuData(it.id) }
            menuGroupTypes = menu
        }
    }


    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData<Result> {
            val lastOrder =
                firestoreDbApp.refs.orders.orderBy("created", Query.Direction.DESCENDING)
                    .limit(1).get().await()
            lastOrder?.run {
                val data =
                    documents.firstOrNull()?.toObject(OrderData::class.java) ?: return@liveData
                emit(Result.LastOrder(data))
            }
        }
    }

    override fun reduce(result: Result): State {
        return when (result) {
            is Result.LastOrder -> {
                State(orderData = result.orderData)
            }
        }
    }

    suspend fun print(context: Context): List<Int> {
        var orderBonNumber = sharedPreferences.getLong("orderBonNumber", 120555)
        val printers = transformMenuToPrinters(menuGroupTypes)
        val partsOrders = transformOrders(printers, internalViewState.orderData!!)
        val resultCodes = mutableListOf<Int>()

        partsOrders.forEach { (printerAddress, orderData) ->
            val result = internalViewState.orderData?.run {
                PrinterOrderUseCase(context).printOrder(printerAddress, orderData, orderBonNumber)
            } ?: orderBonNumber to -100
            orderBonNumber = result.first
            resultCodes.add(result.second)
        }
        sharedPreferences.edit().putLong("orderBonNumber", orderBonNumber).apply()
        return resultCodes
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


    override var internalViewState: State = State()

    sealed class Action {
        object GetLastOrder : Action()
    }

    sealed class Result {
        data class LastOrder(val orderData: OrderData) : Result()
    }

    data class State(val orderData: OrderData? = null)

}