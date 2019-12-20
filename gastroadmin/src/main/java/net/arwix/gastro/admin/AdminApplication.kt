package net.arwix.gastro.admin

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class AdminApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
//        startKoin {
//            androidContext(this@ClientApplication)
//            modules(mainModule)
//        }
    }
}