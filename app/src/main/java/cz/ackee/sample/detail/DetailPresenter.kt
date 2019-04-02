package cz.ackee.sample.detail

import cz.ackee.ackroutine.core.DefaultOAuthCredentials
import cz.ackee.sample.App
import cz.ackee.sample.model.SampleItem

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
        // TODO: perform data fetch action
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
        // TODO: perform logout action
    }
}
