package net.arwix.gastro.client

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import net.arwix.gastro.client.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClientApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        startKoin {
            androidContext(this@ClientApplication)
            modules(mainModule)
        }
    }
}