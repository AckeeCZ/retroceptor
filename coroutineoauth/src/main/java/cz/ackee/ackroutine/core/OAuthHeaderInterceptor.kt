package cz.ackee.ackroutine.core

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds Authorization header with access token from [OAuthStore] to Http requests.
 */
class OAuthHeaderInterceptor internal constructor(private val authStore: AuthStore<OAuthCredentials>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val accessToken = authStore.authCredentials?.accessToken?.takeIf { it.isNotBlank() }
        accessToken?.let { token ->
            builder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
