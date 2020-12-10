package cz.ackee.ackroutine

import android.content.Context
import android.content.SharedPreferences
import cz.ackee.ackroutine.core.AuthErrorChecker
import cz.ackee.ackroutine.core.DefaultAuthErrorChecker
import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.ackroutine.core.OAuthHeaderInterceptor
import cz.ackee.ackroutine.core.OAuthStore

/**
 * OAuth implementation of [AuthManager].
 */
class OAuthManager internal constructor(
    oAuthStore: OAuthStore,
    refreshTokenAction: suspend (OAuthCredentials?) -> OAuthCredentials,
    onRefreshTokenFailed: (Throwable) -> Unit = {},
    errorChecker: AuthErrorChecker = DefaultAuthErrorChecker()
) : AuthManager<OAuthCredentials>(
    authStore = oAuthStore,
    refreshCredentialsAction = refreshTokenAction,
    onRefreshCredentialsFailed = onRefreshTokenFailed,
    errorChecker = errorChecker
) {

    constructor(
        sp: SharedPreferences,
        refreshTokenAction: suspend (OAuthCredentials?) -> OAuthCredentials,
        onRefreshTokenFailed: (Throwable) -> Unit = {},
        errorChecker: AuthErrorChecker = DefaultAuthErrorChecker()
    ) : this(OAuthStore(sp), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    constructor(
        context: Context,
        refreshTokenAction: suspend (OAuthCredentials?) -> OAuthCredentials,
        onRefreshTokenFailed: (Throwable) -> Unit = {},
        errorChecker: AuthErrorChecker = DefaultAuthErrorChecker()
    ) : this(OAuthStore(context), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    val accessToken: String?
        get() = authStore.authCredentials?.accessToken

    val refreshToken: String?
        get() = authStore.authCredentials?.refreshToken

    override fun provideAuthInterceptor() = OAuthHeaderInterceptor(authStore)
}