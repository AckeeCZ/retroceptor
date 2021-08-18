package io.github.ackeecz.retroceptor

import android.content.Context
import android.content.SharedPreferences
import io.github.ackeecz.retroceptor.core.AuthErrorChecker
import io.github.ackeecz.retroceptor.core.DefaultAuthErrorChecker
import io.github.ackeecz.retroceptor.core.OAuthCredentials
import io.github.ackeecz.retroceptor.core.OAuthHeaderInterceptor
import io.github.ackeecz.retroceptor.core.OAuthStore
import okhttp3.Interceptor

/**
 * OAuth implementation of [AuthManager].
 */
open class OAuthManager internal constructor(
    protected val oAuthStore: OAuthStore,
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

    override fun provideAuthInterceptor(): Interceptor = OAuthHeaderInterceptor(authStore)
}
