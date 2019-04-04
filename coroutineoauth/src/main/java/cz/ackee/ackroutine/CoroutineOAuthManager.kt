package cz.ackee.ackroutine

import android.content.Context
import android.content.SharedPreferences
import cz.ackee.ackroutine.core.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
 * Factory for [Deferred] api calls.
 */
typealias DeferredCallFactory<T> = () -> Deferred<T>

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
    private val refreshTokenAction: (String) -> Deferred<OAuthCredentials>,
    private val onRefreshTokenFailed: (Throwable) -> Unit = {},
    private val errorChecker: ErrorChecker = DefaultErrorChecker()
) {

    constructor(sp: SharedPreferences, refreshTokenAction: (String) -> Deferred<OAuthCredentials>,
        onRefreshTokenFailed: (Throwable) -> Unit = {}, errorChecker: ErrorChecker = DefaultErrorChecker()) :
        this(OAuthStore(sp), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    constructor(context: Context, refreshTokenAction: (String) -> Deferred<OAuthCredentials>,
        onRefreshTokenFailed: (Throwable) -> Unit = {}, errorChecker: ErrorChecker = DefaultErrorChecker()) :
        this(OAuthStore(context), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    val accessToken get() = oAuthStore.accessToken

    val refreshToken get() = oAuthStore.refreshToken

    fun saveCredentials(credentials: OAuthCredentials) {
        oAuthStore.saveCredentials(credentials)
    }

    fun clearCredentials() {
        oAuthStore.clearCredentials()
    }

    fun provideAuthInterceptor() = OAuthInterceptor(oAuthStore)

    fun <T> wrapDeferred(callFactory: DeferredCallFactory<T>): Deferred<T> {
        return GlobalScope.async(start = CoroutineStart.LAZY) {
            if (oAuthStore.tokenExpired()) {
                saveCredentials(refreshAccessToken())

                callFactory().await()
            } else {
                try {
                    callFactory().await()
                } catch (e: Exception) {
                    if (errorChecker.invalidAccessToken(e)) {
                        saveCredentials(refreshAccessToken())

                        callFactory().await()
                    } else {
                        throw e
                    }
                }
            }
        }
    }

    private suspend fun refreshAccessToken(): OAuthCredentials {
        return try {
            refreshTokenAction(oAuthStore.refreshToken ?: "").await()
        } catch (e: Exception) {
            if (errorChecker.invalidRefreshToken(e)) {
                clearCredentials()
                onRefreshTokenFailed(e)
            }

            throw e
        }
    }
}