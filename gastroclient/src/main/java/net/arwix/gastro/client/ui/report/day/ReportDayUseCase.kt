package net.arwix.gastro.client.ui.report.day

import android.content.Context
import com.epson.epos2.printer.Printer
import com.google.firebase.firestore.FirebaseFirestore
import net.arwix.gastro.client.common.createCharString
import net.arwix.gastro.client.domain.PrinterUtils
import net.arwix.gastro.library.await
import net.arwix.gastro.library.data.CheckData
import net.arwix.gastro.library.data.OrderData
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.NumberFormat

class ReportDayUseCase(
    private val firestore: FirebaseFirestore,
    private val applicationContext: Context
) {


    suspend fun getReport(localDate: LocalDate): ReportDayData? {
        val zdtBegin = localDate.atStartOfDay(ZoneId.systemDefault()).plusHours(7)
        val beginDate = DateTimeUtils.toDate(zdtBegin.toInstant())
        val zdtEnd = localDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1)
        val endDate = DateTimeUtils.toDate(zdtEnd.toInstant())
        val query =
            firestore.collection("checks")
                .whereGreaterThan("created", beginDate)
                .whereLessThan("created", endDate)
                .get().await()
                ?: return null

        var totalChecks = 0
        var totalItems = 0L
        var totalPrice = 0L

        query.toObjects(CheckData::class.java).forEach {
            totalChecks++
            it.checkItems?.forEach { (_, checkItems) ->
                checkItems.forEach { check ->
                    totalItems += check.count
                    totalPrice += check.count * check.price
                }
            }
        }

        return ReportDayData(localDate, totalChecks, totalItems, totalPrice)

    }

    suspend fun getOrderReport(localDate: LocalDate): ReportDayData? {
        val zdtBegin = localDate.atStartOfDay(ZoneId.systemDefault()).plusHours(7)
        val beginDate = DateTimeUtils.toDate(zdtBegin.toInstant())
        val zdtEnd = localDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1)
        val endDate = DateTimeUtils.toDate(zdtEnd.toInstant())
        val query =
            firestore.collection("orders")
                .whereGreaterThan("created", beginDate)
                .whereLessThan("created", endDate)
                .get().await()
                ?: return null

        var totalChecks = 0
        var totalItems = 0L
        var totalPrice = 0L

        query.toObjects(OrderData::class.java).forEach {
            totalChecks++
            it.orderItems?.forEach { (_, checkItems) ->
                checkItems.forEach { check ->
                    totalItems += check.count
                    totalPrice += check.count * check.price
                }
            }
        }

        return ReportDayData(localDate, totalChecks, totalItems, totalPrice)

    }

    suspend fun print(report: ReportDayData): Pair<Int, Unit?> {

        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val formatter = NumberFormat.getCurrencyInstance()
        val totalPriceStr = formatter.format(report.totalPrice / 100.0).toString()

        return PrinterUtils.print(
            "TCP:192.168.0.104",
            Printer.TM_M30, Printer.MODEL_ANK, applicationContext
        ) {
            //==============================================
            addTextFont(Printer.FONT_A)
            addTextAlign(Printer.ALIGN_CENTER)
            addTextSize(2, 2)
            addText("Daily report")
            addTextSize(1, 1)
            addText(createCharString(46, "_"))
            //==============================================
            addTextSize(2, 2)
            addText(dateFormatter.format(report.day) + "\n")
            //==============================================
            addTextAlign(Printer.ALIGN_LEFT)
            addTextSize(1, 1)
            addText("Check count: ${report.totalChecks}\n")
            addText("Summary items: ${report.totalItems}\n")
            addTextSize(1, 2)
            addText("Total price: $totalPriceStr\n")
            addTextSize(1, 1)
            addText(createCharString(46, "-"))
            //==============================================
            addCut(Printer.CUT_FEED)
        }

    }


}