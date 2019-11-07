package net.arwix.gastro.boss.data.auth

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant

class AccessTokenProvider(
    private val clientId: String,
    private val clientSecret: String,
    private val googleAuth2Api: GoogleAuth2Api,
    private val preferences: SharedPreferences
) {

    sealed class AccessTokenResult {
        data class Token(val value: AccessToken) : AccessTokenResult()
        data class Error(val throwable: Throwable) : AccessTokenResult()
    }

    private var token: AccessToken? = null
    private val updateLockMutex = Mutex()

    init {
        loadFromPreferences()
    }

//    fun getAccessToken(): String? = token?.accessToken

    suspend fun getOrUpdateAccessToken() = withContext(Dispatchers.Main) {
        val innerToken = getAccessToken()
        Log.e("getOrUpdateAccessToken", "get begin $innerToken")
        if (innerToken != null) return@withContext AccessTokenResult.Token(innerToken)
        if (!updateLockMutex.isLocked) {
            Log.e("getOrUpdateAccessToken", "update begin")
            return@withContext updateToken()
        } else {
            Log.e("getOrUpdateAccessToken", "update lock")
            updateLockMutex.withLock {
                Log.e("getOrUpdateAccessToken", "update begin lock")
                val token = getAccessToken()
                if (token != null) return@withContext AccessTokenResult.Token(token)
            }
            return@withContext updateToken()
        }
    }

    private fun getAccessToken(): AccessToken? {
        val innerToken = token
        return if (innerToken != null && innerToken.obtainingTime + innerToken.expiresTime > Instant.now().epochSecond) {
            return innerToken
        } else null
    }

    fun clearToken() {
        //TODO
    }

    suspend fun updateToken(authCode: String? = null): AccessTokenResult =
        withContext(Dispatchers.Main) {
            updateLockMutex.withLock {
                Log.e("updateToken", "update $authCode")
                val token = runCatching {
                    if (authCode == null) {
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
                }.onSuccess {
                    saveToPreferences(it)
                    token = it
                }

                if (token.isSuccess) {
                    AccessTokenResult.Token(token.getOrThrow())
                } else {
                    AccessTokenResult.Error(token.exceptionOrNull()!!)
                }
            }
        }

    private fun saveToPreferences(accessToken: AccessToken) {
        Log.e("save access token", accessToken.toString())
        preferences
            .edit()
            .putString(ACCESS_TOKEN_KEY, Gson().toJson(accessToken))
            .apply()
    }

    private fun loadFromPreferences() {
        val tokenString = preferences.getString(ACCESS_TOKEN_KEY, null) ?: return
        token = Gson().fromJson(tokenString, AccessToken::class.java)
        Log.e("loadFromPreferences", token.toString())
    }

    private companion object {
        private const val ACCESS_TOKEN_KEY = "auth.key.access_token"
    }


}