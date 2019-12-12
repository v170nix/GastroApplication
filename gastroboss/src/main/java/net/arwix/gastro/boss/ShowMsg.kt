package net.arwix.gastro.boss

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.epson.epos2.Epos2CallbackCode
import com.epson.epos2.Epos2Exception


object ShowMsg {
    fun showException(
        e: Exception,
        method: String?,
        context: Context
    ) {
        var msg = ""
        msg = if (e is Epos2Exception) {
            java.lang.String.format(
                "%s\n\t%s\n%s\n\t%s",
                "err_code",
                getEposExceptionText(e.errorStatus),
                "title_err_method",
                method
            )
        } else {
            e.toString()
        }
        show(msg, context)
    }

    fun showResult(code: Int, errMsg: String, context: Context) {
        var msg = ""
        msg = if (errMsg.isEmpty()) {
            java.lang.String.format(
                "\t%s\n\t%s\n",
                "title_msg_result",
                getCodeText(code)
            )
        } else {
            java.lang.String.format(
                "\t%s\n\t%s\n\n\t%s\n\t%s\n",
                "msg_result",
                getCodeText(code),
                "msg_description",
                errMsg
            )
        }
        show(msg, context)
    }

    fun showMsg(msg: String, context: Context) {
        show(msg, context)
    }

    private fun show(msg: String, context: Context) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, whichButton -> return@OnClickListener })
        alertDialog.create()
        alertDialog.show()
    }

    private fun getEposExceptionText(state: Int): String? {
        var return_text = ""
        return_text = when (state) {
            Epos2Exception.ERR_PARAM -> "ERR_PARAM"
            Epos2Exception.ERR_CONNECT -> "ERR_CONNECT"
            Epos2Exception.ERR_TIMEOUT -> "ERR_TIMEOUT"
            Epos2Exception.ERR_MEMORY -> "ERR_MEMORY"
            Epos2Exception.ERR_ILLEGAL -> "ERR_ILLEGAL"
            Epos2Exception.ERR_PROCESSING -> "ERR_PROCESSING"
            Epos2Exception.ERR_NOT_FOUND -> "ERR_NOT_FOUND"
            Epos2Exception.ERR_IN_USE -> "ERR_IN_USE"
            Epos2Exception.ERR_TYPE_INVALID -> "ERR_TYPE_INVALID"
            Epos2Exception.ERR_DISCONNECT -> "ERR_DISCONNECT"
            Epos2Exception.ERR_ALREADY_OPENED -> "ERR_ALREADY_OPENED"
            Epos2Exception.ERR_ALREADY_USED -> "ERR_ALREADY_USED"
            Epos2Exception.ERR_BOX_COUNT_OVER -> "ERR_BOX_COUNT_OVER"
            Epos2Exception.ERR_BOX_CLIENT_OVER -> "ERR_BOX_CLIENT_OVER"
            Epos2Exception.ERR_UNSUPPORTED -> "ERR_UNSUPPORTED"
            Epos2Exception.ERR_FAILURE -> "ERR_FAILURE"
            else -> String.format("%d", state)
        }
        return return_text
    }

    private fun getCodeText(state: Int): String? {
        var return_text = ""
        return_text = when (state) {
            Epos2CallbackCode.CODE_SUCCESS -> "PRINT_SUCCESS"
            Epos2CallbackCode.CODE_PRINTING -> "PRINTING"
            Epos2CallbackCode.CODE_ERR_AUTORECOVER -> "ERR_AUTORECOVER"
            Epos2CallbackCode.CODE_ERR_COVER_OPEN -> "ERR_COVER_OPEN"
            Epos2CallbackCode.CODE_ERR_CUTTER -> "ERR_CUTTER"
            Epos2CallbackCode.CODE_ERR_MECHANICAL -> "ERR_MECHANICAL"
            Epos2CallbackCode.CODE_ERR_EMPTY -> "ERR_EMPTY"
            Epos2CallbackCode.CODE_ERR_UNRECOVERABLE -> "ERR_UNRECOVERABLE"
            Epos2CallbackCode.CODE_ERR_FAILURE -> "ERR_FAILURE"
            Epos2CallbackCode.CODE_ERR_NOT_FOUND -> "ERR_NOT_FOUND"
            Epos2CallbackCode.CODE_ERR_SYSTEM -> "ERR_SYSTEM"
            Epos2CallbackCode.CODE_ERR_PORT -> "ERR_PORT"
            Epos2CallbackCode.CODE_ERR_TIMEOUT -> "ERR_TIMEOUT"
            Epos2CallbackCode.CODE_ERR_JOB_NOT_FOUND -> "ERR_JOB_NOT_FOUND"
            Epos2CallbackCode.CODE_ERR_SPOOLER -> "ERR_SPOOLER"
            Epos2CallbackCode.CODE_ERR_BATTERY_LOW -> "ERR_BATTERY_LOW"
            Epos2CallbackCode.CODE_ERR_TOO_MANY_REQUESTS -> "ERR_TOO_MANY_REQUESTS"
            Epos2CallbackCode.CODE_ERR_REQUEST_ENTITY_TOO_LARGE -> "ERR_REQUEST_ENTITY_TOO_LARGE"
            Epos2CallbackCode.CODE_CANCELED -> "CODE_CANCELED"
            Epos2CallbackCode.CODE_ERR_NO_MICR_DATA -> "ERR_NO_MICR_DATA"
            Epos2CallbackCode.CODE_ERR_ILLEGAL_LENGTH -> "ERR_ILLEGAL_LENGTH"
            Epos2CallbackCode.CODE_ERR_NO_MAGNETIC_DATA -> "ERR_NO_MAGNETIC_DATA"
            Epos2CallbackCode.CODE_ERR_RECOGNITION -> "ERR_RECOGNITION"
            Epos2CallbackCode.CODE_ERR_READ -> "ERR_READ"
            Epos2CallbackCode.CODE_ERR_NOISE_DETECTED -> "ERR_NOISE_DETECTED"
            Epos2CallbackCode.CODE_ERR_PAPER_JAM -> "ERR_PAPER_JAM"
            Epos2CallbackCode.CODE_ERR_PAPER_PULLED_OUT -> "ERR_PAPER_PULLED_OUT"
            Epos2CallbackCode.CODE_ERR_CANCEL_FAILED -> "ERR_CANCEL_FAILED"
            Epos2CallbackCode.CODE_ERR_PAPER_TYPE -> "ERR_PAPER_TYPE"
            Epos2CallbackCode.CODE_ERR_WAIT_INSERTION -> "ERR_WAIT_INSERTION"
            Epos2CallbackCode.CODE_ERR_ILLEGAL -> "ERR_ILLEGAL"
            Epos2CallbackCode.CODE_ERR_INSERTED -> "ERR_INSERTED"
            Epos2CallbackCode.CODE_ERR_WAIT_REMOVAL -> "ERR_WAIT_REMOVAL"
            Epos2CallbackCode.CODE_ERR_DEVICE_BUSY -> "ERR_DEVICE_BUSY"
            Epos2CallbackCode.CODE_ERR_IN_USE -> "ERR_IN_USE"
            Epos2CallbackCode.CODE_ERR_CONNECT -> "ERR_CONNECT"
            Epos2CallbackCode.CODE_ERR_DISCONNECT -> "ERR_DISCONNECT"
            Epos2CallbackCode.CODE_ERR_MEMORY -> "ERR_MEMORY"
            Epos2CallbackCode.CODE_ERR_PROCESSING -> "ERR_PROCESSING"
            Epos2CallbackCode.CODE_ERR_PARAM -> "ERR_PARAM"
            else -> String.format("%d", state)
        }
        return return_text
    }
}