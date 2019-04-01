package cz.ackee.sample.detail

import cz.ackee.ackroutine.core.DefaultOAuthCredentials
import cz.ackee.sample.App
import cz.ackee.sample.model.SampleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presenter for detail screen
 */
class DetailPresenter {

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
        GlobalScope.launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    apiInteractor.getData().await()
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
        GlobalScope.launch {
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
