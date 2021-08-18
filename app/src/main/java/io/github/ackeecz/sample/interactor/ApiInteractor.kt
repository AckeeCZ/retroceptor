package io.github.ackeecz.sample.interactor

import io.github.ackeecz.sample.model.SampleItem
import io.github.ackeecz.retroceptor.core.OAuthCredentials

/**
 * Interactor for communicating with API
 */
interface ApiInteractor {

    suspend fun getData(): List<SampleItem>

    suspend fun login(name: String, password: String): OAuthCredentials

    suspend fun logout()
}
