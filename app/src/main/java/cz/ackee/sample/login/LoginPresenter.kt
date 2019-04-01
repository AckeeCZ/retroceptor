package cz.ackee.sample.login

import android.util.Log
import cz.ackee.sample.App
import kotlinx.coroutines.*

/**
 * Presenter for login screen
 */
class LoginPresenter {

    private val apiInteractor = App.diContainer.apiInteractor
    private var view: ILoginView? = null

    fun login(name: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiInteractor.login(name, password).await()
                }

                Log.d("LOGIN", "Received ${result.accessToken}")
                onLoggedIn()
            } catch (e: Exception) {
                onErrorHappened(e)
            }
        }
    }

    fun onViewAttached(view: ILoginView) {
        this.view = view
    }

    fun onViewDetached() {
        this.view = null
    }

    private fun onLoggedIn() {
        view?.openDetail()
    }

    private fun onErrorHappened(throwable: Throwable) {
        throwable.printStackTrace()
    }
}
