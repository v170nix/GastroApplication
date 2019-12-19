package net.arwix.gastro.client.domain

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import net.arwix.gastro.client.common.createCharString
import net.arwix.gastro.library.data.OrderData
import net.arwix.gastro.library.data.OrderItem
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.NumberFormat
import kotlin.coroutines.resume

class PrinterOrderUseCase(private val applicationContext: Context) {

    //Bon Nr: 120555

    // return first bonNumber last code
    suspend fun printOrder(addressPrinter: String, orderData: OrderData, bonNumber: Long) =
        suspendCancellableCoroutine<Pair<Long, Int>> { cont ->

            var innerBonNumber = bonNumber
            val printer = Printer(Printer.TM_M30, Printer.MODEL_ANK, applicationContext)

            printer.setReceiveEventListener { ptr: Printer?,
                                              code: Int,
                                              printerStatusInfo: PrinterStatusInfo?,
                                              s: String? ->
                if (ptr != null) disconnectPrinter(ptr)
                cont.resume(innerBonNumber to code)
                printer.clearCommandBuffer()
                printer.setReceiveEventListener(null)
            }

            cont.invokeOnCancellation {
                runCatching {
                    disconnectPrinter(printer)
                }
            }

            val printTable =
                orderData.table.toString() + "/" + String.format("%02d", orderData.tablePart)
            val formatter = NumberFormat.getCurrencyInstance()


            val data = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(orderData.created!!.seconds), ZoneId.systemDefault()
            )
            val timeFormatter =
                DateTimeFormatter.ofPattern("dd.MM. HH:mm:ss")

            orderData.orderItems?.forEach { (type: String, items: List<OrderItem>) ->
                //==============================================
                printer.addTextFont(Printer.FONT_A)
                printer.addTextSize(2, 2)
                printer.addText("Tisch: $printTable   ")
                printer.addTextSize(1, 1)
                printer.addText("Bon Nr: $innerBonNumber\n")
                //==============================================
                printer.addTextSize(2, 2)
                printer.addText(timeFormatter.format(data) + "\n")
                //==============================================
                printer.addTextSize(1, 1)
                printer.addText("Kellner: Dmitriy Shtoh\n")
                printer.addText(createCharString(46, "_"))
                printer.addTextSize(2, 2)
                printer.addText("$type:\n")
                printer.addText("\n")
                printer.addTextFont(Printer.FONT_A)
                printer.addTextSize(1, 2)
                //==============================================
                // 38
                // 42
                items.forEach { item ->
                    val lineSize = 42
                    val priceTotal = item.price * item.count
                    var itemString = createCharString(lineSize, " ")
                    val nameString = "   ${item.count}x ${item.name}"
                    val priceString = formatter.format(priceTotal / 100.0).dropLast(2)
                    if (nameString.length > lineSize - priceString.length - 4) {
                        printer.addText("$nameString\n")
                        itemString = itemString.replaceRange(
                            itemString.length - priceString.length,
                            itemString.length,
                            priceString
                        )
                        printer.addText("$itemString\n")
                    } else {
                        itemString = itemString.replaceRange(0, nameString.length, nameString)
                        itemString = itemString.replaceRange(
                            itemString.length - priceString.length,
                            itemString.length,
                            priceString
                        )
                        printer.addText("$itemString\n")
                    }
                    printer.addText(createCharString(46, "-"))
                }
                printer.addCut(Printer.CUT_FEED)
                innerBonNumber++
            }

            printer.connect(addressPrinter, Printer.PARAM_DEFAULT)
            printer.beginTransaction()
            val status = printer.status
            printer.sendData(Printer.PARAM_DEFAULT)
        }
}


private fun disconnectPrinter(printer: Printer) {
    try {
        printer.endTransaction()
    } catch (e: Exception) {
        Crashlytics.logException(e)
    }
    try {
        printer.clearCommandBuffer()
    } catch (e: Exception) {
        Crashlytics.logException(e)
    }
    try {
        printer.disconnect()
    } catch (e: Exception) {
        Crashlytics.logException(e)
    }
}