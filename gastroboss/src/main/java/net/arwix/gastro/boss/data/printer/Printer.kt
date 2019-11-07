package net.arwix.gastro.boss.data.printer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Printer(
    @SerializedName("isTosAccepted") @Expose var isTosAccepted: Boolean? = null,
    @SerializedName("displayName") @Expose var displayName: String? = null,
    @SerializedName("description") @Expose var description: String? = null,
    @SerializedName("capsHash") @Expose var capsHash: String? = null,
    @SerializedName("updateTime") @Expose var updateTime: String? = null,
    @SerializedName("type") @Expose var type: String? = null,
    @SerializedName("tags") @Expose var tags: List<String>? = null,
    @SerializedName("proxy") @Expose var proxy: String? = null,
    @SerializedName("createTime") @Expose var createTime: String? = null,
    @SerializedName("defaultDisplayName") @Expose var defaultDisplayName: String? = null,
    @SerializedName("name") @Expose var name: String? = null,
    @SerializedName("connectionStatus") @Expose var connectionStatus: String? = null,
    @SerializedName("id") @Expose var id: String? = null,
    @SerializedName("status") @Expose var status: String? = null,
    @SerializedName("accessTime") @Expose var accessTime: String? = null
)