package io.github.ackeecz.retroceptor.core

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistence store of OAuth credentials.
 */
internal class OAuthStore : AuthStore<OAuthCredentials> {

    companion object {

        internal const val DEFAULT_SP_NAME = "oauth2"
        internal const val ACCESS_TOKEN_KEY_OLD = "oath2_access_token"
        internal const val REFRESH_TOKEN_KEY_OLD = "oath2_refresh_token"
        internal const val ACCESS_TOKEN_KEY = "oauth2_access_token"
        internal const val REFRESH_TOKEN_KEY = "oauth2_refresh_token"
        internal const val EXPIRES_AT_KEY = "oauth2_expires_at"
    }

    private val sp: SharedPreferences

    val accessToken: String?
        get() = sp.getString(ACCESS_TOKEN_KEY, null)

    val refreshToken: String?
        get() = sp.getString(REFRESH_TOKEN_KEY, null)

    val expiresAt: Long?
        get() = sp.getLong(EXPIRES_AT_KEY, -1).takeIf { it > -1 }

    override val authCredentials: OAuthCredentials?
        get() {
            return if (accessToken != null || refreshToken != null) {
                OAuthCredentials(
                    accessToken = accessToken ?: "",
                    refreshToken = refreshToken ?: "",
                    expiresIn = expiresAt
                )
            } else {
                null
            }
        }

    constructor(context: Context) {
        sp = context.getSharedPreferences(DEFAULT_SP_NAME, Context.MODE_PRIVATE)
        ensureNewKeys()
    }

    constructor(sp: SharedPreferences) {
        this.sp = sp
        ensureNewKeys()
    }

    override fun saveCredentials(credentials: OAuthCredentials) {
        sp.edit()
            .putString(ACCESS_TOKEN_KEY, credentials.accessToken)
            .putString(REFRESH_TOKEN_KEY, credentials.refreshToken)
            .apply {
                credentials.expiresIn?.let { expiresIn -> putLong(EXPIRES_AT_KEY, System.currentTimeMillis() + expiresIn * 1000) }
            }
            .apply()
    }

    override fun clearCredentials() {
        sp.edit().clear().apply()
    }

    fun tokenExpired() = credentialsExpired()

    // There were typos in keys, check the keys and migrate them if needed
    private fun ensureNewKeys() {
        if (sp.contains(ACCESS_TOKEN_KEY_OLD)) {
            sp.edit()
                .putString(
                    ACCESS_TOKEN_KEY, sp.getString(
                        ACCESS_TOKEN_KEY_OLD, null
                    )
                )
                .remove(ACCESS_TOKEN_KEY_OLD)
                .apply()
        }
        if (sp.contains(REFRESH_TOKEN_KEY_OLD)) {
            sp.edit()
                .putString(
                    REFRESH_TOKEN_KEY, sp.getString(
                        REFRESH_TOKEN_KEY_OLD, null
                    )
                )
                .remove(REFRESH_TOKEN_KEY_OLD)
                .apply()
        }
    }
}
