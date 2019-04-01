package cz.ackee.ackroutine.core

/**
 * Interface for checking if provided error indicates invalid tokens.
 */
interface ErrorChecker {

    fun invalidAccessToken(t: Throwable): Boolean

    fun invalidRefreshToken(t: Throwable): Boolean
}
