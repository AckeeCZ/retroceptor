package cz.ackee.ackroutine.core

import android.text.TextUtils
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds Authorisation header with access token from [OAuthStore] to Http requests.
 */
class OAuthInterceptor internal constructor(private val oAuthStore: OAuthStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        if (!TextUtils.isEmpty(oAuthStore.accessToken)) {
            val accToken = oAuthStore.accessToken
            Log.d("OKHTTP", "Setting token header $accToken")
            builder.addHeader("Authorization", "Bearer $accToken")
        } else {
            Log.d("OKHTTP", "No access token")
        }
        return chain.proceed(builder.build())
    }
}
