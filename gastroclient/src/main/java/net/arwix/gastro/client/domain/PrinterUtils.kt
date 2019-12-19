package net.arwix.gastro.client.domain

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PrinterUtils {

    suspend inline fun <T> print(
        addressPrinter: String,
        printerSeries: Int,
        lang: Int,
        context: Context,
        crossinline block: Printer.() -> T?
    ): Pair<Int, T?> = suspendCancellableCoroutine { cont ->

        val printer = runCatching {
            Printer(printerSeries, lang, context)
        }.onFailure {
            cont.resumeWithException(it)
        }.getOrNull()!!

        var result: T? = null

        cont.invokeOnCancellation { runCatching { disconnectPrinter(printer) } }

        printer.setReceiveEventListener { ptr: Printer?,
                                          code: Int,
                                          _: PrinterStatusInfo?,
                                          _: String? ->
            if (ptr != null) disconnectPrinter(ptr)
            cont.resume(code to result)
            printer.clearCommandBuffer()
            printer.setReceiveEventListener(null)
        }

        runCatching {
            result = block(printer)
            printer.connect(addressPrinter, Printer.PARAM_DEFAULT)
            printer.beginTransaction()
            val status = printer.status
            printer.sendData(Printer.PARAM_DEFAULT)
        }.onFailure {
            cont.resumeWithException(it)
        }
    }

    fun disconnectPrinter(printer: Printer) {
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
}


