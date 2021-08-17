package io.ackeecz.sample.interactor

import io.github.ackeecz.ackroutine.core.OAuthCredentials
import io.ackeecz.sample.model.SampleItem

/**
 * Interactor for communicating with API
 */
interface ApiInteractor {

    suspend fun getData(): List<SampleItem>

    suspend fun login(name: String, password: String): OAuthCredentials

    suspend fun logout()
}
