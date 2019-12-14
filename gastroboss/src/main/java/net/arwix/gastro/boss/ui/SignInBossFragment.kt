package net.arwix.gastro.boss.ui


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.crashlytics.android.Crashlytics
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener
import kotlinx.android.synthetic.main.fragment_sign_in_boss.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import net.arwix.extension.weak
import net.arwix.gastro.boss.R
import net.arwix.gastro.boss.ShowMsg
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * A simple [Fragment] subclass.
 */
class SignInBossFragment : Fragment(), CoroutineScope by MainScope(), ReceiveListener {

    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private var printer: Printer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in_boss, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.liveState.observe(this,  Observer(::renderProfile))
    }

    private fun renderProfile(state: ProfileViewModel.State) {
        Log.e("render state", state.toString())
        if (state.account != null) {
            findNavController(this).navigate(R.id.summaryBossFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_in_attach_button.setOnClickListener {
            profileViewModel.nonCancelableIntent(ProfileViewModel.Action.LoginStart(this.weak()))
        }
        test_epson_printer.setOnClickListener {
            testClick()
        }
        button_discovery_printer.setOnClickListener {
            findNavController(this).navigate(R.id.discoveryFragment)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        profileViewModel.nonCancelableIntent(ProfileViewModel.Action.LoginDoActivityResult(requestCode, data))
    }

    private fun testClick() {
        runPrintReceiptSequence()
    }

    private fun runPrintReceiptSequence(): Boolean {
        if (!initializeObject()) return false
        if (!printData()) {
            finalizeObject()
            return false
        }
        return true
    }

    private fun printData(): Boolean {
        val prt = printer ?: return false
        if (!connectPrinter()) return false

        try {
            prt.addTextAlign(Printer.ALIGN_CENTER)
            prt.addText("Hello World")
            prt.addFeedLine(1)
            prt.addText(
                buildString {
                    append("THE STORE 123 (555) 555 – 5555\n")
                    append("\n")
                    append("7/01/07 16:58 6153 05 0191 134\n")
                    append("ST# 21 OP# 001 TE# 01 TR# 747\n")
                    append("------------------------------\n")
                }
            )
            prt.addTextFont(Printer.FONT_B)
            prt.addText("Font B\n")
            prt.addText("Hello World\n")
            prt.sendData(Printer.PARAM_DEFAULT)
            prt.clearCommandBuffer()
            prt.addTextFont(Printer.FONT_C)
            prt.addText("Font C\n")
            prt.addText("Hello World\n")
            prt.sendData(Printer.PARAM_DEFAULT)
            prt.clearCommandBuffer()
            prt.addTextSize(2, 2)
            prt.addText("Text SIZE 2 - 2")
            prt.addTextSize(1, 1)
            prt.addText("Text SIZE 1 - 1")
            prt.addCut(Printer.CUT_FEED)
            prt.sendData(Printer.PARAM_DEFAULT)
        } catch (e: Exception) {
            ShowMsg.showException(e, "printData", requireContext())
            Crashlytics.logException(e)
            prt.disconnect()
            return false
        }
        return true
    }

    private fun initializeObject(): Boolean {
        try {
            printer = Printer(Printer.TM_M30, Printer.MODEL_ANK, requireContext())
            printer?.setReceiveEventListener(this)
        } catch (e: Exception) {
            ShowMsg.showException(e, "Printer", requireContext())
            Crashlytics.logException(e)
            return false
        }
        return true
    }

    private fun finalizeObject() {
        printer?.run {
            clearCommandBuffer()
            setReceiveEventListener(null)
            printer = null
        }
    }

    private fun connectPrinter(): Boolean {
        var isBeginTransaction = false
        val prt = printer ?: return false
        try {
            val tcp = editText_tcp.text.toString().trim()
            prt.connect(tcp, Printer.PARAM_DEFAULT)
        } catch (e: Exception) {
            ShowMsg.showException(e, "connect", requireContext())
            Crashlytics.logException(e)
            return false
        }

        try {
            prt.beginTransaction()
            isBeginTransaction = true
        } catch (e: Exception) {
            ShowMsg.showException(e, "beginTransaction", requireContext())
            Crashlytics.logException(e)
        }

        if (!isBeginTransaction) {
            try {
                prt.disconnect()
            } catch (e: Epos2Exception) {
                return false
            }
        }
        return true
    }

    private fun disconnectPrinter() {
        val prt = printer ?: return
        try {
            prt.endTransaction()
        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                ShowMsg.showException(e, "endTransaction", requireContext())
                Crashlytics.logException(e)
            }
        }
        try {
            prt.disconnect()
        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                ShowMsg.showException(e, "disconnect", requireContext())
                Crashlytics.logException(e)
            }
        }
    }

    override fun onPtrReceive(
        printerObj: Printer?,
        code: Int,
        status: PrinterStatusInfo?,
        printJobId: String?
    ) {
        requireActivity().runOnUiThread {
            ShowMsg.showResult(code, "onPtrReceive", requireContext())
            disconnectPrinter()
        }
    }

}
