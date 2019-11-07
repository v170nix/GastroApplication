package net.arwix.gastro.boss.data.printer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Printers {
    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("printers")
    @Expose
    var printers: List<Printer>? = null
    @SerializedName("xsrf_token")
    @Expose
    var xsrfToken: String? = null

}