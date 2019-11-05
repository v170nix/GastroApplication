package net.arwix.gastro.boss

import android.app.Application
import net.arwix.gastro.boss.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BossApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BossApplication)
            modules(mainModule)
        }
    }

}