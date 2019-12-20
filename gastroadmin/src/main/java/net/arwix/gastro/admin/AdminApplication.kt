package net.arwix.gastro.admin

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import net.arwix.gastro.admin.di.AppAdminModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AdminApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        startKoin {
            androidContext(this@AdminApplication)
            modules(AppAdminModule)
        }
    }
}