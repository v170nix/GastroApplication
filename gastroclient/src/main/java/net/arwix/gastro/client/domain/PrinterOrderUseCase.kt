package net.arwix.gastro.client.domain

import android.content.Context
import android.util.Log
import com.epson.epos2.printer.Printer
import net.arwix.gastro.client.common.createCharString
import net.arwix.gastro.library.menu.data.MenuGroupData
import net.arwix.gastro.library.menu.data.MenuGroupName
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.order.data.OrderItem
import net.arwix.gastro.library.print.data.PrinterAddress
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.NumberFormat

class PrinterOrderUseCase(private val applicationContext: Context) {

    //Bon Nr: 120555

    // return first bonNumber last code

    suspend fun print(
        printerAddress: PrinterAddress,
        orderData: OrderData,
        menus: List<MenuGroupData>
    ): Pair<Int, Unit?> {
        return PrinterUtils.print(
            printerAddress,
            Printer.TM_M30,
            Printer.MODEL_ANK,
            applicationContext
        ) {
            val printTable =
                orderData.table.toString() + "/" + String.format("%02d", orderData.tablePart)
            val formatter = NumberFormat.getCurrencyInstance()
            val data = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(orderData.created!!.seconds), ZoneId.systemDefault()
            )
            val timeFormatter =
                DateTimeFormatter.ofPattern("dd.MM. HH:mm:ss")

            val bonNumbers = orderData.bonNumbers

            orderData.orderItems.forEach { (menuGroupName: MenuGroupName, items: List<OrderItem>) ->
                val bonNumber = bonNumbers?.get(menuGroupName)
                val menuGroupData = menus.firstOrNull { it.name == menuGroupName }
                //==============================================
                addTextFont(Printer.FONT_A)
                addTextSize(2, 2)
                addText("Tisch: $printTable   ")
                addTextSize(1, 1)
                if (bonNumber == null) {
                    addText("\n")
                } else {
                    addText("Bon Nr: $bonNumber\n")
                }
                //==============================================
                addTextSize(2, 2)
                addText(timeFormatter.format(data) + "\n")
                //==============================================
                addTextSize(1, 1)
                addText("Kellner: Dmitriy Shtoh\n")
                addText(createCharString(46, "_"))
                addTextSize(2, 2)
                addText("$menuGroupName:\n")
                addText("\n")
                addTextFont(Printer.FONT_A)
                addTextSize(1, 2)
                //==============================================
                // 38
                // 42
                items.forEach { item ->
                    val itemFont = when (menuGroupData?.findPrintFont(item.name)) {
                        2 -> Printer.FONT_B
                        3 -> Printer.FONT_C
                        else -> Printer.FONT_A
                    }
                    addTextFont(itemFont)

                    val linePaddingRight = 6
                    // val lineSize = 42
                    val lineSize = getPrintLineSize(itemFont) - linePaddingRight
                    Log.e("item", "$lineSize $item")


                    val priceTotal = item.price * item.count
                    var itemString = createCharString(lineSize, " ")
                    val nameString = "   ${item.count}x ${item.name}"
                    val priceString = formatter.format(priceTotal / 100.0).dropLast(2)
                    if (nameString.length > lineSize - priceString.length - 4) {
                        addText("$nameString\n")
//                        itemString = itemString.replaceRange(
//                            itemString.length - priceString.length,
//                            itemString.length,
//                            priceString
//                        )
//                        addText("$itemString\n")
                        addText("$priceString\n")
                    } else {
                        itemString = itemString.replaceRange(0, nameString.length, nameString)
                        itemString = itemString.replaceRange(
                            itemString.length - priceString.length,
                            itemString.length,
                            priceString
                        )
                        addText("$itemString\n")
                    }
                    addText(createCharString(getPrintLineSize(itemFont) - 2, "-"))
                }
                addCut(Printer.CUT_FEED)
            }
        }
    }

//    suspend fun printOrder(
//        addressPrinter: String,
//        orderData: OrderData
//    ) =
//        suspendCancellableCoroutine<Int> { cont ->
//
//            val printer = Printer(Printer.TM_M30, Printer.MODEL_ANK, applicationContext)
//
//            printer.setReceiveEventListener { ptr: Printer?,
//                                              code: Int,
//                                              printerStatusInfo: PrinterStatusInfo?,
//                                              s: String? ->
//                if (ptr != null) disconnectPrinter(ptr)
//                cont.resume(code)
//                printer.clearCommandBuffer()
//                printer.setReceiveEventListener(null)
//            }
//
//            cont.invokeOnCancellation {
//                runCatching {
//                    disconnectPrinter(printer)
//                }
//            }
//
//            val printTable =
//                orderData.table.toString() + "/" + String.format("%02d", orderData.tablePart)
//            val formatter = NumberFormat.getCurrencyInstance()
//
//
//            val data = ZonedDateTime.ofInstant(
//                Instant.ofEpochSecond(orderData.created!!.seconds), ZoneId.systemDefault()
//            )
//            val timeFormatter =
//                DateTimeFormatter.ofPattern("dd.MM. HH:mm:ss")
//
//            val bonNumbers = orderData.bonNumbers
//
//            orderData.orderItems.forEach { (type: String, items: List<OrderItem>) ->
//                val bonNumber = bonNumbers?.get(type)
//                //==============================================
//                printer.addTextFont(Printer.FONT_A)
//                printer.addTextSize(2, 2)
//                printer.addText("Tisch: $printTable   ")
//                printer.addTextSize(1, 1)
//                if (bonNumber == null) {
//                    printer.addText("\n")
//                } else {
//                    printer.addText("Bon Nr: $bonNumber\n")
//                }
//                //==============================================
//                printer.addTextSize(2, 2)
//                printer.addText(timeFormatter.format(data) + "\n")
//                //==============================================
//                printer.addTextSize(1, 1)
//                printer.addText("Kellner: Dmitriy Shtoh\n")
//                printer.addText(createCharString(46, "_"))
//                printer.addTextSize(2, 2)
//                printer.addText("$type:\n")
//                printer.addText("\n")
//                printer.addTextFont(Printer.FONT_A)
//                printer.addTextSize(1, 2)
//                //==============================================
//                // 38
//                // 42
//                items.forEach { item ->
//                    val lineSize = 42
//                    val priceTotal = item.price * item.count
//                    var itemString = createCharString(lineSize, " ")
//                    val nameString = "   ${item.count}x ${item.name}"
//                    val priceString = formatter.format(priceTotal / 100.0).dropLast(2)
//                    if (nameString.length > lineSize - priceString.length - 4) {
//                        printer.addText("$nameString\n")
//                        itemString = itemString.replaceRange(
//                            itemString.length - priceString.length,
//                            itemString.length,
//                            priceString
//                        )
//                        printer.addText("$itemString\n")
//                    } else {
//                        itemString = itemString.replaceRange(0, nameString.length, nameString)
//                        itemString = itemString.replaceRange(
//                            itemString.length - priceString.length,
//                            itemString.length,
//                            priceString
//                        )
//                        printer.addText("$itemString\n")
//                    }
//                    printer.addText(createCharString(46, "-"))
//                }
//                printer.addCut(Printer.CUT_FEED)
//            }
//
//            printer.connect(addressPrinter, Printer.PARAM_DEFAULT)
//            printer.beginTransaction()
//            val status = printer.status
//            printer.sendData(Printer.PARAM_DEFAULT)
//        }

    private companion object {
        const val PRINT_LINE_SIZE_FONT_A = 48
        const val PRINT_LINE_SIZE_FONT_B = 57
        const val PRINT_LINE_SIZE_FONT_C = 64

        fun getPrintLineSize(font: Int) = when (font) {
            Printer.FONT_A -> PRINT_LINE_SIZE_FONT_A
            Printer.FONT_B -> PRINT_LINE_SIZE_FONT_B
            Printer.FONT_C -> PRINT_LINE_SIZE_FONT_C
            else -> throw IllegalArgumentException("get print line illegal $font")
        }


    }
}


//private fun disconnectPrinter(printer: Printer) {
//    try {
//        printer.endTransaction()
//    } catch (e: Exception) {
//        Crashlytics.logException(e)
//    }
//    try {
//        printer.clearCommandBuffer()
//    } catch (e: Exception) {
//        Crashlytics.logException(e)
//    }
//    try {
//        printer.disconnect()
//    } catch (e: Exception) {
//        Crashlytics.logException(e)
//    }
//}