package net.arwix.gastro.client.ui.report.day


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import com.epson.epos2.Epos2Exception
import kotlinx.android.synthetic.main.fragment_report_day.*
import kotlinx.coroutines.*
import net.arwix.gastro.client.R
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.NumberFormat


class ReportDayFragment : Fragment(), CoroutineScope by MainScope() {

    private val reportDayUseCase: ReportDayUseCase by inject()
    private var reportDayData: ReportDayData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launch {
            reportDayData = reportDayUseCase.getReport(LocalDateTime.now())
            reportDayData?.run {
                updateReport(this)
            }
        }

        report_day_print_button.setOnClickListener {
            launch(Dispatchers.IO) {
                reportDayData?.run {
                    runCatching {
                        reportDayUseCase.print(this)
                    }.onSuccess {
                        val code = it.first
                        withContext(Dispatchers.Main) { toToast(code) }

                    }.onFailure {
                        if (it is Epos2Exception) {
                            val code = it.errorStatus
                            withContext(Dispatchers.Main) { toToast(code) }
                        } else {
                            withContext(Dispatchers.Main) { toToast(it) }
                            Crashlytics.logException(it)
                        }
                    }
                }
            }
        }
    }

    private fun toToast(code: Int) {
        Toast.makeText(
            requireContext(),
            "code $code",
            Toast.LENGTH_LONG
        ).apply {
            this.setGravity(Gravity.CENTER, 0, 0)
        }.show()
    }

    private fun toToast(th: Throwable) {
        Toast.makeText(
            requireContext(),
            "Throwable $th",
            Toast.LENGTH_LONG
        ).apply {
            this.setGravity(Gravity.CENTER, 0, 0)
        }.show()
    }

    private fun updateReport(dayData: ReportDayData) {
        report_day_print_button.isEnabled = true
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val formatter = NumberFormat.getCurrencyInstance()
        val totalPriceStr = formatter.format(dayData.totalPrice / 100.0).toString()
        report_day_local_date_text.text = dateFormatter.format(dayData.day)
        report_day_check_count_text.text = "Check count: ${dayData.totalChecks}"
        report_day_items_count_text.text = "Summary items: ${dayData.totalItems}"
        report_day_total_price_text.text = "Total price: $totalPriceStr"
    }

}
