package net.arwix.gastro.library.order.domain

import androidx.collection.arrayMapOf
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.print.data.PrinterAddress

object OrderPrintUtils {

    fun splitOrderData(
        menuGroups: List<MenuGroupData>,
        summaryOrderData: OrderData
    ): Map<PrinterAddress?, OrderData> {
        return splitOrders(menuGroupByPrinter(menuGroups), summaryOrderData)
    }

    @Suppress("UNCHECKED_CAST")
    private fun menuGroupByPrinter(menuGroups: List<MenuGroupData>)
            : Map<PrinterAddress, List<MenuGroupName>> {
        return menuGroups.groupBy(
            keySelector = { it.printer },
            valueTransform = { it.name })
            .filterKeys {
                it != null
            } as Map<PrinterAddress, List<MenuGroupName>>
    }

    private fun splitOrders(
        printerMap: Map<PrinterAddress, List<MenuGroupName>>,
        summaryOrderData: OrderData
    ): Map<PrinterAddress?, OrderData> {

        val result = arrayMapOf<PrinterAddress?, OrderData>()
        summaryOrderData.orderItems.forEach { (menuGroupName, listOrders) ->
            val printer = printerMap.findPrinter(menuGroupName)
            val orderData =
                result.getOrPut(printer) { summaryOrderData.copy(orderItems = arrayMapOf()) }
            val orderItemsMap = orderData.orderItems as MutableMap
            val orderItemsList = orderItemsMap.getOrPut(menuGroupName) {
                mutableListOf()
            } as MutableList
            orderItemsList.addAll(listOrders)
        }
        return result
    }

    private fun Map<PrinterAddress, List<MenuGroupName>>.findPrinter(menuName: MenuGroupName): PrinterAddress? {
        this.forEach { (printer, partMenusInPrinter) ->
            if (partMenusInPrinter.indexOf(menuName) > -1) {
                return printer
            }
        }
        return null
    }

}

