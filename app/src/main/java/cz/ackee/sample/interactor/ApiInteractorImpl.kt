package cz.ackee.sample.interactor

import cz.ackee.ackroutine.CoroutineOAuthManager
import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.sample.model.SampleItem
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.AuthApiDescription
import kotlinx.coroutines.Deferred

/**
 * Implementation of api
 */
class ApiInteractorImpl(
    private val oAuthManager: CoroutineOAuthManager,
    private val apiDescription: ApiDescription,
    private val authApiDescription: AuthApiDescription
) : IApiInteractor {

//    override fun login(name: String, password: String): Deferred<OAuthCredentials> {
//        return authApiDescription.login(name, password)
//                .doOnSuccess { oAuthManager.saveCredentials(it) }
//                .map { it }
//    }

    override fun login(name: String, password: String): Deferred<OAuthCredentials> = authApiDescription.login(name, password)

    override fun getData(): Deferred<List<SampleItem>> = apiDescription.getData()

    override fun logout(): Deferred<Unit> = authApiDescription.logout()
}
