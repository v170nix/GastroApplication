package net.arwix.gastro.client.feature.print.ui

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.Html
import android.text.SpannableString
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.epson.epos2.Epos2Exception
import com.epson.epos2.Epos2Exception.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import net.arwix.gastro.client.R
import net.arwix.gastro.client.data.OrderRepository
import net.arwix.gastro.client.domain.PrinterOrderUseCase
import net.arwix.gastro.library.common.notificationCompatBuilderChannel
import net.arwix.gastro.library.menu.data.MenuRepository
import net.arwix.gastro.library.order.data.OrderData
import net.arwix.gastro.library.order.domain.OrderPrintUtils
import net.arwix.gastro.library.print.data.PrinterAddress
import org.koin.android.ext.android.inject


class PrintIntentService : IntentService("PrintIntentService") {

    private val binder = PrintBinder()
    private val menuRepository: MenuRepository by inject()
    private val orderRepository: OrderRepository by inject()
    //    private val firestoreDbApp: FirestoreDbApp by inject()
    private val broadcastChannel = BroadcastChannel<PrintResult>(3)
    private var isBinder = false

    fun getResultAsFlow() = broadcastChannel.asFlow()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder {
        isBinder = true
        return binder
    }

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
        isBinder = false
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_PRINT_ORDER -> {
                val orderRef = intent.getStringExtra(EXTRA_PRINT_ORDER_PARAMS) ?: return
                val orderData = runBlocking { orderRepository.getOrder(orderRef) }
                val notification = notificationCompatBuilderChannel(
                    this, NOTIFICATION_FOREGROUND_PRINT_SERVICE_CHANNEL_ID,
                    "Print service", NotificationManagerCompat.IMPORTANCE_LOW
                )
                    .setSmallIcon(R.drawable.ic_print)
                    .setContentTitle("Printing order")
                    .setContentText("Table ${orderData.table}/${orderData.tablePart}")
                    .build()
                startForeground(NOTIFICATION_FOREGROUND_ID, notification)

                val result = runBlocking { handlePrintOrder(orderData) }

                if ((result is PrintResult.Error)) {
                    val title = "Error printing order ${orderData.table}/${orderData.tablePart}"
                    val errorText = result.printList.joinToString("<br>") {
                        "${it.printerAddress}: ${it.message}"
                    }
                    val formattedBody = SpannableString(
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Html.fromHtml(errorText)
                        else Html.fromHtml(errorText, Html.FROM_HTML_MODE_LEGACY)
                    )

                    val notificationError = notificationCompatBuilderChannel(
                        this,
                        NOTIFICATION_ERROR_ORDER_CHANNEL_ID,
                        "Error orders channel", NotificationManagerCompat.IMPORTANCE_HIGH
                    )
                        .setSmallIcon(R.drawable.ic_print)
                        .setContentTitle(title)
                        .setColor(this.resources.getColor(R.color.design_default_color_error))
                        .setStyle(
                            NotificationCompat.BigTextStyle().bigText(formattedBody).setBigContentTitle(
                                title
                            )
                        )
                        .setContentText(formattedBody)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                    NotificationManagerCompat.from(this).notify(3423, notificationError)

                }
                broadcastChannel.offer(result)
                stopForeground(true)
            }
        }
    }

    private suspend fun handlePrintOrder(orderData: OrderData): PrintResult = supervisorScope {
        val menuGroups = menuRepository.getData()

        val printerOrderUseCase = PrinterOrderUseCase(applicationContext)
        val awaitPrinterResults = mutableListOf<Deferred<Pair<PrinterAddress, Int>>>()
        val prePrinterData = OrderPrintUtils.splitOrderData(menuGroups, orderData)
        prePrinterData.forEach { (printer, orderData) ->
            printer ?: return@forEach
            val job = async(Dispatchers.IO) {
                runCatching {
                    printerOrderUseCase.print(printer, orderData).first
                }.getOrElse {
                    if (it is Epos2Exception) it.errorStatus else -100
                }.let {
                    printer to it
                }
            }
            awaitPrinterResults.add(job)
        }
        val results = awaitPrinterResults.awaitAll()
        if (results.sumBy { it.second } == 0) {
            PrintResult.Success(orderData)
        } else {
            PrintResult.Error(orderData, results.map {
                PrintErrorData(it.first, it.second, getErrorTextFromCode(it.second))
            })
        }
    }

    inner class PrintBinder : Binder() {
        fun getService() = this@PrintIntentService
    }

    companion object {
        @JvmStatic
        fun startPrintOrder(context: Context, orderRef: String) {
            val intent = Intent(context, PrintIntentService::class.java).apply {
                action = ACTION_PRINT_ORDER
                putExtra(EXTRA_PRINT_ORDER_PARAMS, orderRef)
            }
            context.startService(intent)
        }

        private fun getErrorTextFromCode(code: Int): String {
            return when (code) {
                0 -> "Success job"
                ERR_PARAM -> "An invalid parameter was specified"
                ERR_CONNECT -> "Failed to open the device"
                ERR_TIMEOUT -> "Failed to communicate with the devices within the specified time"
                ERR_MEMORY -> "Necessary memory could not be allocated"
                ERR_ILLEGAL -> "Tried to start communication with a printer with which " +
                        "communication had been already established."
                ERR_NOT_FOUND -> "The device could not be found"
                ERR_IN_USE -> "The device was in use"
                ERR_TYPE_INVALID -> "The device type is different"
                ERR_DISCONNECT -> "Failed to disconnect the device"
                ERR_ALREADY_OPENED -> "Communication box is already open"
                ERR_ALREADY_USED -> "Specified member ID is already in use"
                ERR_BOX_COUNT_OVER -> "The number of created communication boxes has exceeded the upper limit"
                ERR_BOX_CLIENT_OVER -> "The number of members belong to the communication box has exceeded the upper limit"
                ERR_UNSUPPORTED -> "A print method not supported by the printer was specified"
                ERR_FAILURE -> "An unknown error occurred"
                else -> "error code $code"
            }
        }
    }

    sealed class PrintResult {
        data class Success(val orderData: OrderData) : PrintResult()
        data class Error(val orderData: OrderData, val printList: List<PrintErrorData>) :
            PrintResult()
    }

    data class PrintErrorData(
        val printerAddress: PrinterAddress,
        val code: Int,
        val message: String
    )
}

private const val NOTIFICATION_SUCCESS_ORDER_CHANNEL_ID =
    "PrintService.NotificationOrderSuccessChannel"
private const val NOTIFICATION_FOREGROUND_PRINT_SERVICE_CHANNEL_ID =
    "PrintService.NotificationForegroundChannel"
private const val NOTIFICATION_FOREGROUND_ID = 493
private const val NOTIFICATION_ERROR_ORDER_CHANNEL_ID = "PrintService.NotificationOrderErrorChannel"

private const val ACTION_PRINT_ORDER = "net.arwix.gastro.client.print.action.PRINT_ORDER"
private const val EXTRA_PRINT_ORDER_PARAMS =
    "net.arwix.gastro.client.print.action.extra.ORDER_REF"
