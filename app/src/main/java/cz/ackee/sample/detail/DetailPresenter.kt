package cz.ackee.sample.detail

import android.util.Log
import cz.ackee.ackroutine.core.DefaultOAuthCredentials
import cz.ackee.sample.App
import cz.ackee.sample.model.SampleItem
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Presenter for detail screen
 */
class DetailPresenter(override val coroutineContext: CoroutineContext) : CoroutineScope {

    private val apiInteractor = App.diContainer.apiInteractor
    private var view: IDetailView? = null
    private var items: List<SampleItem>? = null

    fun onViewAttached(view: IDetailView) {
        this.view = view
        if (items != null) {
            view.showData(items!!)
        }
    }

    fun onViewDetached() {
        this.view = null
    }

    fun refresh() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    Log.d("Detail", "Refresh")
                    val deferred = apiInteractor.getData()
                    Log.d("Detail", "Before await")
                    val res = deferred.await()
                    Log.d("Detail", "After await")
                    return@withContext res

                }
                onDataLoaded(data)
            } catch (e: Exception) {
                onErrorHappened(e)
            }
        }
    }

    private fun onErrorHappened(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun onDataLoaded(sampleItems: List<SampleItem>) {
        this.items = sampleItems
        view?.showData(sampleItems)
    }

    fun invalidateAccessToken() {
        with(App.diContainer.oAuthManager) { saveCredentials(DefaultOAuthCredentials("bla", refreshToken ?: "", 15)) }
    }

    fun invalidateRefreshToken() {
        with(App.diContainer.oAuthManager) { saveCredentials(DefaultOAuthCredentials(accessToken ?: "", "bla", 15)) }
    }

    fun logout() {
        GlobalScope.launch(Dispatchers.Default) {
            try {
                withContext(Dispatchers.IO) {
                    apiInteractor.logout().await()
                }
                App.diContainer.logouter.logout()
            } catch (e: Exception) {
                onErrorHappened(e)
            }
        }
    }
}
