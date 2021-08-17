package io.github.ackeecz.ackroutine.core

/**
 * Interface for checking if provided error indicates invalid tokens.
 */
interface AuthErrorChecker {

    /**
     * Decides if [t] error is caused by invalid credentials while performing some regular API
     * request.
     *
     * When using OAuth, this is equivalent to expired access token.
     *
     * @return true if [t] is caused by invalid/expired credentials and refresh should be performed.
     */
    fun invalidCredentials(t: Throwable): Boolean

    /**
     * Decides if [t] error is caused by invalid credentials while trying to refresh credentials.
     * This means that credentials refresh failed and user should be logged out and new login
     * should be performed.
     *
     * When using OAuth, this is equivalent to expired refresh token.
     *
     * @return true if [t] is caused by invalid/expired credentials and refreshed credentials cannot
     * be obtained from data with old credentials.
     */
    fun invalidRefreshCredentials(t: Throwable): Boolean
}
