package io.github.ackeecz.sample

import android.app.Application
import com.facebook.stetho.Stetho
import io.github.ackeecz.sample.di.DIContainer
import io.github.ackeecz.sample.model.rest.Server

/**
 * Application class
 */
class App : Application() {

    companion object {
        lateinit var diContainer: DIContainer
    }

    override fun onCreate() {
        super.onCreate()
        Server(this)
        diContainer = DIContainer(this)
        Stetho.initializeWithDefaults(this)
    }
}
