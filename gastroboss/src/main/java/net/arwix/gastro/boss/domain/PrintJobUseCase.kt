package net.arwix.gastro.boss.domain

import android.graphics.pdf.PdfDocument
import android.util.Base64
import android.util.Log
import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mjson.Json
import net.arwix.gastro.boss.data.auth.AccessTokenProvider
import net.arwix.gastro.boss.data.printer.GoogleCloudPrintApi
import net.arwix.gastro.boss.data.printer.Printer
import net.arwix.gastro.boss.data.printer.PrinterRepository
import java.io.ByteArrayOutputStream
import java.io.File

class PrintJobUseCase(
    private val googlePrintApi: GoogleCloudPrintApi,
    private val printerRepository: PrinterRepository,
    private val accessTokenProvider: AccessTokenProvider
) {

    private var scope: CoroutineScope? = null
    private var printers: List<Printer> = listOf()
    private var printJobs: Any? = null
    private var xsrfToken: String? = null

    @MainThread
    fun attachScope(scope: CoroutineScope) {
        this.scope?.cancel()
        this.scope = scope
        scope.launch(Dispatchers.Main) {
            printerRepository.selectPrintersAsFlow().collect {
                printers = it
            }
        }
    }

    suspend fun print(path: String, title: String) {
        try {
            Log.e("context", "-1")
            val file = File(path)
            Log.e("context", "0")
            val base64Context = Base64.encodeToString(file.readBytes(), Base64.DEFAULT)
            Log.e("context", "1")
            val printer = if (printers.isEmpty()) {
                printerRepository.getSelectedPrinters().firstOrNull()
            } else {
                printers.first()
            }
            Log.e("context", "2")
            printer ?: return
            Log.e("context", "3")
            val result = accessTokenProvider.getOrUpdateAccessToken()
            Log.e("context", "4")
            if (result is AccessTokenProvider.AccessTokenResult.Token) {
                Log.e("context", "5")
                print(printer, result.value.accessToken!!, title, base64Context)
                Log.e("context", "6")
            }
        } catch (e: Exception) {
            Log.e("ext", e.toString())
        }
    }

    suspend fun print(pdfDocument: PdfDocument) {
        val out = ByteArrayOutputStream()
        pdfDocument.writeTo(out)
        val base64Context = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        val printer = if (printers.isEmpty()) {
            printerRepository.getSelectedPrinters().firstOrNull()
        } else {
            printers.first()
        }
        printer ?: return
        val result = accessTokenProvider.getOrUpdateAccessToken()
        if (result is AccessTokenProvider.AccessTokenResult.Token) {
            print(printer, result.value.accessToken!!, "title", base64Context)
        }
    }

    private suspend fun getXsrfToken(): String {
        return xsrfToken ?: printerRepository.getOrUpdatePrinters()!!.xsrfToken!!.also {
            xsrfToken = it
        }
    }

    private suspend fun print(
        printer: Printer,
        accessToken: String,
        title: String,
        b64Context: String
    ) {
        val responseBody = googlePrintApi.submitFilePrintJob(
            "Bearer $accessToken",
            "base64",
            getXsrfToken(),
            printer.id!!,
            title,
            getTicket(),
            b64Context,
            "application/pdf"
        )
        Log.e("print", responseBody.toString())
    }

    private fun getTicket(): String {
        val ticket = Json.`object`()
        val print = Json.`object`()
        ticket.set("version", "1.0")
        print.set("vendor_ticket_item", Json.array())
        print.set("color", Json.`object`("type", "STANDARD_MONOCHROME"))
        print.set("copies", Json.`object`("copies", 1));
        ticket.set("print", print)
        return ticket.toString()
    }


}