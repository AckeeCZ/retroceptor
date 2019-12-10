package cz.ackee.sample.model.rest

import cz.ackee.ackroutine.IgnoreAuth
import cz.ackee.ackroutine.core.DefaultOAuthCredentials
import kotlinx.coroutines.Deferred
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api Description of auth endpoints
 */
interface AuthApiDescription {

    @IgnoreAuth
    @POST("login")
    suspend fun login(@Query("username") name: String, @Query("password") passwd: String): DefaultOAuthCredentials

    @IgnoreAuth
    @POST("refresh_token")
    suspend fun refreshAccessToken(@Query("refresh_token") refreshToken: String?): DefaultOAuthCredentials

    @POST("logout")
    suspend fun logout()
}
