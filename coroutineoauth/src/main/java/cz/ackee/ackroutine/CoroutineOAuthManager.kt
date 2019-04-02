package cz.ackee.ackroutine

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import cz.ackee.ackroutine.core.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
 * TODO: to be continued...
 */

typealias DeferredCallFactory<T> = () -> Deferred<T>

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
            Log.d("Manager", "Enter")
            if (oAuthStore.tokenExpired()) {
                Log.d("Manager", "Expired")
                saveCredentials(refreshAccessToken())

                //callFactory().await()
                Log.d("Manager", "Refreshed ?")
                callFactory().await().also {
                    Log.d("Manager", "Original call")
                }
            } else {
                //Log.d("Manager", "Non Expired")
                try {
                    Log.d("Manager", "Original call")
                    callFactory().await()
                } catch (e: Exception) {
                    if (errorChecker.invalidAccessToken(e)) {
                        Log.d("Manager", "Invalid token")
                        saveCredentials(refreshAccessToken())
                        Log.d("Manager", "Refreshed ? 2")
                        callFactory().await().also {
                            Log.d("Manager", "Original call 2")
                        }
                    } else {
                        Log.d("Manager", "Throw error")
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