package io.github.ackeecz.retroceptor

import io.github.ackeecz.retroceptor.core.AuthCredentials
import io.github.ackeecz.retroceptor.core.AuthErrorChecker
import io.github.ackeecz.retroceptor.core.AuthStore
import io.github.ackeecz.retroceptor.core.DefaultAuthErrorChecker
import okhttp3.Interceptor
import retrofit2.Call

/**
 * AuthManager provides wrapping for Retrofit [Call]s, which automatically handles
 * access token expiration and performs refresh token logic defined with [refreshCredentialsAction],
 * provided by user.
 * In case of success, new credentials are stored in [authStore].
 *
 * The user may provide fallback for refresh token expiration in [onRefreshCredentialsFailed].
 *
 * The user may provide custom [AuthErrorChecker] containing access and refresh token expiration
 * checking logic. Otherwise, [DefaultAuthErrorChecker] is applied.
 */
abstract class AuthManager<C : AuthCredentials>(
    protected val authStore: AuthStore<C>,
    protected val refreshCredentialsAction: suspend (C?) -> C,
    protected val onRefreshCredentialsFailed: (Throwable) -> Unit = {},
    protected val errorChecker: AuthErrorChecker = DefaultAuthErrorChecker()
) {

    private val singleCallHandler = SingleCallHandler<C>()

    /**
     * Latest auth credentials.
     */
    val authCredentials: C?
        get() = authStore.authCredentials

    /**
     * Store new auth [credentials].
     */
    fun saveCredentials(credentials: C) {
        authStore.saveCredentials(credentials)
    }

    /**
     * Clear all saved auth credentials. This should be performed on user logout or when credentials
     * expire and cannot be refreshed without user intervention.
     */
    fun clearCredentials() {
        authStore.clearCredentials()
    }

    /**
     * Provides internal dependencies for credential refresh interceptor.
     */
    fun <T> wrapAuthCheck(call: Call<T>): Call<T> {
        return AuthAwareCall(call, { singleCallHandler.callSingle { refreshAuthCredentials() } }, authStore, errorChecker)
    }

    /**
     * Provide OkHttp [Interceptor] that adds authorization metadata to all Retrofit requests
     * that are not annotated with [IgnoreAuth].
     */
    abstract fun provideAuthInterceptor(): Interceptor

    private suspend fun refreshAuthCredentials(): C {
        return try {
            refreshCredentialsAction(authStore.authCredentials)
        } catch (e: Exception) {
            if (errorChecker.invalidRefreshCredentials(e)) {
                clearCredentials()
                onRefreshCredentialsFailed(e)
            }

            throw e
        }
    }
}
