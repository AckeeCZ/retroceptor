package io.ackeecz.sample.interactor

import io.ackeecz.sample.model.SampleItem
import io.ackeecz.sample.model.rest.ApiDescription
import io.ackeecz.sample.model.rest.AuthApiDescription
import io.github.ackeecz.retroceptor.OAuthManager
import io.github.ackeecz.retroceptor.core.OAuthCredentials

/**
 * Implementation of api
 */
class ApiInteractorImpl(
    private val oAuthManager: OAuthManager,
    private val apiDescription: ApiDescription,
    private val authApiDescription: AuthApiDescription
) : ApiInteractor {

    override suspend fun getData(): List<SampleItem> {
        return apiDescription.getData()
    }

    override suspend fun login(name: String, password: String): OAuthCredentials {
        return authApiDescription.login(name, password).also {
            oAuthManager.saveCredentials(it)
        }
    }

    override suspend fun logout() {
        authApiDescription.logout()
    }
}
