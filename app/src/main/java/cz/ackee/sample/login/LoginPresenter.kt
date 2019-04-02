package cz.ackee.sample.login

import cz.ackee.sample.App

/**
 * Presenter for login screen
 */
class LoginPresenter {

    private val apiInteractor = App.diContainer.apiInteractor
    private var view: ILoginView? = null

    fun login(name: String, password: String) {
        // TODO: perform login action
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
