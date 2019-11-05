package net.arwix.gastro.boss.data.auth

import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccessTokenProvider(
    private val clientId: String,
    private val clientSecret: String,
    private val googleAuth2Api: GoogleAuth2Api,
    private val preferences: SharedPreferences
) {

    private var token: AccessToken? = null

    init {
        loadFromPreferences()
    }

    fun getAccessToken(): String? = token?.accessToken

    suspend fun updateToken(authCode: String? = null): AccessToken = withContext(Dispatchers.Main) {
        val result = if (authCode == null) {
            val refreshToken = token?.refreshToken ?: throw IllegalArgumentException()
            googleAuth2Api.refreshToken(
                clientId = clientId,
                clientSecret = clientSecret,
                refreshToken = refreshToken
            ).apply {
                this.refreshToken = refreshToken
            }
        } else {
            googleAuth2Api.getToken(
                clientId = clientId,
                clientSecret = clientSecret,
                authCode = authCode
            )
        }
        result.also {
            saveToPreferences(it)
            token = it
        }
    }

    private fun saveToPreferences(accessToken: AccessToken) {
        preferences
            .edit()
            .putString(ACCESS_TOKEN_KEY, Gson().toJson(accessToken))
            .apply()
    }

    private fun loadFromPreferences() {
        val tokenString = preferences.getString(ACCESS_TOKEN_KEY, null) ?: return
        token = Gson().fromJson(tokenString, AccessToken::class.java)
    }

    private companion object {
        private const val ACCESS_TOKEN_KEY = "auth.key.access_token"
    }


}