package cz.ackee.sample.interactor

import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.sample.model.SampleItem

/**
 * Interactor for communicating with API
 */
interface ApiInteractor {

    suspend fun getData(): List<SampleItem>

    suspend fun login(name: String, password: String): OAuthCredentials

    suspend fun logout()
}
