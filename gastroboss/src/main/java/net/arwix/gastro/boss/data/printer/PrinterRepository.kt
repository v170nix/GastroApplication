package net.arwix.gastro.boss.data.printer

import android.util.Log
import net.arwix.gastro.boss.data.auth.AccessTokenProvider

class PrinterRepository(
    private val printApi: GoogleCloudPrintApi,
    private val accessTokenProvider: AccessTokenProvider
) {
    var error: Throwable? = null
    private var printers: Printers? = null

    suspend fun getOrUpdatePrinters(): Printers? {
        return printers ?: updatePrinters()
    }

    suspend fun updatePrinters(): Printers? {
        val token = accessTokenProvider.getOrUpdateAccessToken()
        if (token is AccessTokenProvider.AccessTokenResult.Error) {
            Log.e("error token", token.toString())
            error = token.throwable
            return null
        }
        token as AccessTokenProvider.AccessTokenResult.Token
        return runCatching {
            printApi.getPrinters("Bearer ${token.value.accessToken}")
        }.onFailure {
            Log.e("error printers", it.toString())
            error = it
        }.onSuccess {
            printers = it
            error = null
        }.getOrNull()
    }

}