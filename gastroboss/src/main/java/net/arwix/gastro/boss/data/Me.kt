package net.arwix.gastro.boss.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Me(
    @SerializedName("message") @Expose var message: String? = null)