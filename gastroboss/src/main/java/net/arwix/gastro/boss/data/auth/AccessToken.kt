package net.arwix.gastro.boss.data.auth

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

data class AccessToken(
    @SerializedName("access_token") @Expose var accessToken: String? = null,
    @SerializedName("refresh_token") @Expose var refreshToken: String? = null,
    @SerializedName("scope") @Expose var scope: String? = null,
    @SerializedName("expires_in") @Expose var expiresTime: Int = 36,
    @SerializedName("obtaining_time") @Expose val obtainingTime: Long = Instant.now().epochSecond
)