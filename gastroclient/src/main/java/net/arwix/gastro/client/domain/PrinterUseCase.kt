package net.arwix.gastro.client.domain

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import net.arwix.gastro.library.data.CheckData
import java.text.NumberFormat
import kotlin.coroutines.resume

class PrinterUseCase(private val applicationContext: Context) {

    suspend fun printCheck(checkData: CheckData) =
        suspendCancellableCoroutine<Int> { cont ->

            val printer = Printer(Printer.TM_M30, Printer.MODEL_ANK, applicationContext)

            printer.setReceiveEventListener { ptr: Printer?,
                                              code: Int,
                                              printerStatusInfo: PrinterStatusInfo?,
                                              s: String? ->
                if (ptr != null) disconnectPrinter(ptr)
                cont.resume(code)
                printer.clearCommandBuffer()
                printer.setReceiveEventListener(null)
            }

            cont.invokeOnCancellation {
                runCatching {
                    disconnectPrinter(printer)
                }
            }


            val formatter = NumberFormat.getCurrencyInstance()
            var totalPrice = 0L

            printer.addTextFont(Printer.FONT_A)
            printer.addText("0123456789012345678901234567890123456789\n")
            printer.addTextSize(1, 2)
            printer.addText("0123456789012345678901234567890123456789\n")
            printer.addTextFont(Printer.FONT_B)
            printer.addTextSize(1, 1)
            printer.addText("01234567890123456789012345678901234567890123456789\n")
            printer.addFeedLine(1)
            printer.addTextFont(Printer.FONT_A)
            printer.addTextAlign(Printer.ALIGN_CENTER)
            printer.addText("LUXX")
            printer.addFeedLine(1)
            printer.addText("Inh.: Arthur Schuller\n")
            printer.addText("Friedrichsplatz 4, 68165 Mannheim\n")
            printer.addText("Tel.L 062117025511\n")
            printer.addText("www.luxx-mannheim.de | hallo@luxx-mannhelm.de")
            printer.addFeedLine(2)
            printer.addTextAlign(Printer.ALIGN_LEFT)
            printer.addText("Tisch:\n")
            printer.addText("Kellner:\n")
            printer.addFeedLine(1)
            printer.addText("RNr.: 12345")
            printer.addHPosition(210)
            printer.addText("19.12.2019\n")
            printer.addHLine(0, 42, Printer.LINE_THIN)
            checkData.checkItems?.forEach { (type, checkItems) ->
                checkItems.forEach {
                    //                        val str = buildString(35) { repeat(35) { append(" ") } }
//                        var positionString = "${it.count}x ${it.name}"
//                        str.replaceRange(1..20, )
//                        CharSequence
//                        positionString.asSequence().drop(20).toString()
//                        var positionStringSize = positionString.length
//                        if (positionStringSize > 20) {
//                            positionString.replaceRange(0..2, "dfw")
//                            positionString = positionString.substring(1, 20)
//                        }
//                        positionString.substring(0, 20)
                    printer.addTextAlign(Printer.ALIGN_LEFT)
                    printer.addText("${it.count}x ${it.name}")
                    printer.addTextAlign(Printer.ALIGN_RIGHT)

                    totalPrice = it.price * it.count

                    val price1 = formatter.format(it.price / 100.0)
                    val price2 = formatter.format(it.price * it.count / 100.0)

                    printer.addText("$price1 $price2 \n")
                }
            }
            printer.addHLine(0, 42, Printer.LINE_THIN)
            printer.addTextAlign(Printer.ALIGN_LEFT)
            printer.addTextFont(Printer.FONT_B)
            printer.addTextSize(1, 2)
            printer.addText("Total:")
            val totalPriceString = formatter.format(totalPrice / 100.0)
            printer.addTextAlign(Printer.ALIGN_RIGHT)
            printer.addText(totalPriceString + "\n")
            printer.addHLine(0, 42, Printer.LINE_THIN)
            printer.addCut(Printer.CUT_FEED)

            printer.connect("TCP:192.168.0.104", Printer.PARAM_DEFAULT)
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