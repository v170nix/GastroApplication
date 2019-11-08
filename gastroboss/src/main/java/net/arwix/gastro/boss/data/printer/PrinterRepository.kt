package net.arwix.gastro.boss.data.printer

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.arwix.gastro.boss.data.auth.AccessTokenProvider

class PrinterRepository(
    private val printApi: GoogleCloudPrintApi,
    private val accessTokenProvider: AccessTokenProvider,
    private val preferences: SharedPreferences
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

    suspend fun getSelectedPrinters(string: String): List<Printer> {
        val rawString = preferences.getString(PREF_SELECTED_LIST_ID, null) ?: return listOf()
        val list =
            Gson().fromJson<List<Printer>>(rawString, object : TypeToken<List<Printer>>() {}.type)
        return getOrUpdatePrinters()?.printers?.run {
            list.filter { listItem ->
                this.find { it.id == listItem.id } != null
            }
        } ?: list
    }

    fun setSelectedPrinters(list: List<Printer>) {
        preferences.edit().putString(PREF_SELECTED_LIST_ID, Gson().toJson(list)).apply()
    }

    private companion object {
        private const val PREF_SELECTED_LIST_ID = "printers.selected.list"
    }

}