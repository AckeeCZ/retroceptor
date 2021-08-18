package io.github.ackeecz.sample.detail

import androidx.lifecycle.ViewModel
import io.github.ackeecz.sample.interactor.ApiInteractor
import io.github.ackeecz.sample.model.Logouter
import io.github.ackeecz.sample.model.SampleItem
import io.github.ackeecz.sample.model.State
import io.github.ackeecz.retroceptor.OAuthManager
import io.github.ackeecz.retroceptor.core.OAuthCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * ViewModel for detail screen.
 */
class DetailViewModel(
    private val api: ApiInteractor,
    private val oAuthManager: OAuthManager,
    private val logouter: Logouter
) : ViewModel(), CoroutineScope {

    private val viewStateChannel = Channel<State<List<SampleItem>>>()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val viewState: ReceiveChannel<State<List<SampleItem>>>
        get() = viewStateChannel

    init {
        fetchData()
    }

    fun fetchData() {
        launch {
            try {
                viewStateChannel.send(State.Loaded(api.getData()))
            } catch (e: Exception) {
                viewStateChannel.send(State.Error(e))
            }
        }
    }

    fun invalidateAccessToken() {
        oAuthManager.saveCredentials(OAuthCredentials("invalid-access-token", oAuthManager.refreshToken ?: "", 15))
    }

    fun invalidateRefreshToken() {
        oAuthManager.saveCredentials(OAuthCredentials(oAuthManager.accessToken ?: "", "invalid-refresh-token", 15))
    }

    fun logout() {
        launch {
            api.logout()
            logouter.logout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.complete()
    }
}
