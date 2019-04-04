package cz.ackee.sample.interactor

import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.sample.model.SampleItem
import kotlinx.coroutines.Deferred

/**
 * Interactor for communicating with API
 */
interface ApiInteractor {

    fun getData(): Deferred<List<SampleItem>>

    fun login(name: String, password: String): Deferred<OAuthCredentials>

    fun logout(): Deferred<Unit>
}
