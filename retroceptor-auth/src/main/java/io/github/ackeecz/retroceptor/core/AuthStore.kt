package io.github.ackeecz.retroceptor.core

/**
 * [AuthStore]s are responsible for storage of auth credentials [C].
 */
interface AuthStore<C : AuthCredentials> {

    /**
     * Latest auth credentials.
     */
    val authCredentials: C?

    /**
     * Replace [authCredentials] with [credentials].
     */
    fun saveCredentials(credentials: C)

    /**
     * Remove stored [authCredentials].
     */
    fun clearCredentials()

    /**
     * Returns true if [authCredentials] are expired or they do not exist.
     */
    fun credentialsExpired(): Boolean = authCredentials?.areExpired() ?: true
}
