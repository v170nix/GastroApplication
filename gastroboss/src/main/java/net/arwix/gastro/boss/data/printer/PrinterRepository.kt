package net.arwix.gastro.boss.data.printer

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.arwix.gastro.boss.data.auth.AccessTokenProvider

class PrinterRepository(
    private val printApi: GoogleCloudPrintApi,
    private val accessTokenProvider: AccessTokenProvider,
    private val preferences: SharedPreferences
) {
    var error: Throwable? = null
    private val channelSelectPrinters = ConflatedBroadcastChannel(getSelectedPrinters())
    private var printers: Printers? = null
    private val updateMutex: Mutex = Mutex()

    suspend fun getOrUpdatePrinters(): Printers? {
        return printers ?: updatePrinters()
    }

    suspend fun updatePrinters(): Printers? {
        updateMutex.withLock {
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

    fun getSelectedPrinters(): List<Printer> {
        val rawString = preferences.getString(PREF_SELECTED_LIST_ID, null) ?: return listOf()
        return Gson().fromJson<List<Printer>>(
            rawString,
            object : TypeToken<List<Printer>>() {}.type
        )
    }

    suspend fun checkSelectedPrinters(list: List<Printer>): List<Printer> {
        val innerPrinters = if (updateMutex.isLocked) {
            updateMutex.withLock { printers } ?: getOrUpdatePrinters()
        } else {
            getOrUpdatePrinters()
        }
        return innerPrinters?.printers?.run {
            list.filter { listItem ->
                this.find { it.id == listItem.id } != null
            }
        } ?: list
    }

    fun setSelectedPrinters(list: List<Printer>) {
        preferences.edit().putString(PREF_SELECTED_LIST_ID, Gson().toJson(list)).apply()
        channelSelectPrinters.offer(list)
    }

    fun selectPrintersAsFlow() = channelSelectPrinters.openSubscription().consumeAsFlow()

    private companion object {
        private const val PREF_SELECTED_LIST_ID = "printers.selected.list"
    }

}