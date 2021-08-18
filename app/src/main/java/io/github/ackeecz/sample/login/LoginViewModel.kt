package io.github.ackeecz.sample.login

import androidx.lifecycle.ViewModel
import io.github.ackeecz.sample.interactor.ApiInteractor
import io.github.ackeecz.sample.model.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * ViewModel for login screen.
 */
class LoginViewModel(private val api: ApiInteractor) : ViewModel(), CoroutineScope {

    private val viewStateChannel = Channel<State<Unit>>()
    private val job = Job()

    val viewState: ReceiveChannel<State<Unit>>
        get() = viewStateChannel

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun login(name: String, password: String) {
        launch {
            viewStateChannel.send(
                try {
                    State.Loaded(api.login(name, password).let { Unit })
                } catch (e: Exception) {
                    State.Error(e)
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.complete()
    }
}
