package br.com.contaemdia

import android.app.Application
import br.com.contaemdia.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ContaEmDiaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ContaEmDiaApplication)
            modules(appModules)
        }
    }
}
