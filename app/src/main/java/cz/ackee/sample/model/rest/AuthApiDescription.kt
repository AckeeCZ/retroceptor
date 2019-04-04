package cz.ackee.sample.model.rest

import cz.ackee.ackroutine.core.DefaultOAuthCredentials
import kotlinx.coroutines.Deferred
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api Description of auth endpoints
 */
interface AuthApiDescription {

    @POST("login")
    fun login(@Query("username") name: String, @Query("password") passwd: String): Deferred<DefaultOAuthCredentials>

    @POST("refresh_token")
    fun refreshAccessToken(@Query("refresh_token") refreshToken: String?): Deferred<DefaultOAuthCredentials>

    @POST("logout")
    fun logout(): Deferred<Unit>
}