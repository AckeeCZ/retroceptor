package cz.ackee.sample.interactor

import cz.ackee.ackroutine.CoroutineOAuthManager
import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.sample.model.SampleItem
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.AuthApiDescription

/**
 * Implementation of api
 */
class ApiInteractorImpl(
    private val oAuthManager: CoroutineOAuthManager,
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
