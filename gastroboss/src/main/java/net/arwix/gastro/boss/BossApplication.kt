package net.arwix.gastro.boss

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.jakewharton.threetenabp.AndroidThreeTen
import net.arwix.gastro.boss.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BossApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Fresco.initialize(this)
        startKoin {
            androidContext(this@BossApplication)
            modules(mainModule)
        }
    }

}