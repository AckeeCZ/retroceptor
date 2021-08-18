package io.ackeecz.sample.model.rest

import io.github.ackeecz.retroceptor.IgnoreAuth
import io.github.ackeecz.retroceptor.core.OAuthCredentials
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api Description of auth endpoints
 */
interface AuthApiDescription {

    @IgnoreAuth
    @POST("login")
    suspend fun login(@Query("username") name: String, @Query("password") passwd: String): OAuthCredentials

    @IgnoreAuth
    @POST("refresh_token")
    suspend fun refreshAccessToken(@Query("refresh_token") refreshToken: String?): OAuthCredentials

    @POST("logout")
    suspend fun logout()
}
