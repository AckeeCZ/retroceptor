package io.github.ackeecz.ackroutine.core

/**
 * Default OAuth implementation of [AuthCredentials].
 */
data class OAuthCredentials(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long? = null
) : AuthCredentials {

    override fun areExpired(): Boolean {
        return expiresIn?.let { expiration ->
            System.currentTimeMillis() >= expiration
        } ?: false
    }
}
