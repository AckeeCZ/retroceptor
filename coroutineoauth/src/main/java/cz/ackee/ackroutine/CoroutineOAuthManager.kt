package cz.ackee.ackroutine

import android.content.Context
import android.content.SharedPreferences
import cz.ackee.ackroutine.core.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import retrofit2.Call

/**
 * CoorutineOAuthManager provides wrapping for [Deferred] future values, which automatically handles
 * access token expiration and performs refresh token logic defined with [refreshTokenAction],
 * provided by user.
 * In case of success, new credentials are stored in [OAuthStore].
 *
 * The user may provide fallback for refresh token expiration in [onRefreshTokenFailed].
 *
 * The user may provide custom [ErrorChecker] containing access and refresh token expiration
 * checking logic. Otherwise, [DefaultErrorChecker] is applied.
 */
class CoroutineOAuthManager internal constructor(
    private val oAuthStore: OAuthStore,
    private val refreshTokenAction: suspend (String) -> OAuthCredentials,
    private val onRefreshTokenFailed: (Throwable) -> Unit = {},
    private val errorChecker: ErrorChecker = DefaultErrorChecker()
) {

    constructor(sp: SharedPreferences, refreshTokenAction: suspend (String) -> OAuthCredentials,
        onRefreshTokenFailed: (Throwable) -> Unit = {}, errorChecker: ErrorChecker = DefaultErrorChecker()) :
        this(OAuthStore(sp), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    constructor(context: Context, refreshTokenAction: suspend (String) -> OAuthCredentials,
        onRefreshTokenFailed: (Throwable) -> Unit = {}, errorChecker: ErrorChecker = DefaultErrorChecker()) :
        this(OAuthStore(context), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    val accessToken: String?
        get() = oAuthStore.accessToken

    val refreshToken: String?
        get() = oAuthStore.refreshToken

    fun saveCredentials(credentials: OAuthCredentials) {
        oAuthStore.saveCredentials(credentials)
    }

    fun clearCredentials() {
        oAuthStore.clearCredentials()
    }

    fun provideAuthInterceptor() = OAuthInterceptor(oAuthStore)

    fun <T> wrapAuthCheck(call: Call<T>): Call<T> {
        return AuthAwareCall(call, { refreshAccessToken() }, oAuthStore, errorChecker)
    }

    private suspend fun refreshAccessToken(): OAuthCredentials {
        return try {
            refreshTokenAction(oAuthStore.refreshToken ?: "")
        } catch (e: Exception) {
            if (errorChecker.invalidRefreshToken(e)) {
                clearCredentials()
                onRefreshTokenFailed(e)
            }

            throw e
        }
    }
}
