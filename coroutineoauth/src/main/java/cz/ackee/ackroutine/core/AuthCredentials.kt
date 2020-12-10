package cz.ackee.ackroutine.core

/**
 * Common interface for all auth credentials.
 */
interface AuthCredentials {

    /**
     * Returns true if current credentials are expired and refresh should be performed before
     * trying to perform network operation.
     */
    fun areExpired(): Boolean = false
}