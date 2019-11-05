package net.arwix.gastro.boss.data.auth

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AccessToken(
    @SerializedName("access_token") @Expose var accessToken: String? = null,
    @SerializedName("refresh_token") @Expose var refreshToken: String? = null,
    @SerializedName("scope") @Expose var scope: String? = null
)