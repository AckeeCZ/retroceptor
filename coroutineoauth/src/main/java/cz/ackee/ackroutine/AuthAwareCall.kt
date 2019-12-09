package cz.ackee.ackroutine

import android.util.Log
import cz.ackee.ackroutine.core.ErrorChecker
import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.ackroutine.core.OAuthStore
import cz.ackee.retrofitadapter.interceptor.CallDelegate
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * [CallDelegate] which handles token retrieval.
 */
internal class AuthAwareCall<T>(
    private val call: Call<T>,
    private val refreshAction: suspend (String) -> OAuthCredentials,
    private val store: OAuthStore,
    private val errorChecker: ErrorChecker
): CallDelegate<T, T>(call) {

    override fun enqueueImpl(callback: Callback<T>) {
        if (store.tokenExpired()) {
            Log.d("D_OAUTH_CALL", "TOKEN EXPIRED, LOAD AND CALL")
            refreshAndExecute(call, callback)
        } else {
            Log.d("D_OAUTH_CALL", "TOKEN OK, CALL")
            executeAndRefreshIfNeeded(call, callback)
        }
    }

    override fun cloneImpl(): Call<T> {
        return AuthAwareCall(call.clone(), refreshAction, store, errorChecker)
    }

    private fun refreshAndExecute(call: Call<T>, callback: Callback<T>) {
        // TODO: create a premise and chain with next call
        val tokens = runBlocking { refreshAction(store.refreshToken ?: "") }
        store.saveCredentials(tokens)

        call.execute(
            success = { callback.onResponse(this, it) },
            failure = { callback.onFailure(this, it) }
        )
    }

    private fun executeAndRefreshIfNeeded(call: Call<T>, callback: Callback<T>) {
        call.execute(
            success = { callback.onResponse(this, it) },
            failure = {
                if (errorChecker.invalidAccessToken(it)) {
                    refreshAndExecute(call.clone(), callback)
                } else {
                    callback.onFailure(this, it)
                }
            }
        )
    }

    private fun Call<T>.execute(success: (Response<T>) -> Unit, failure: (Throwable) -> Unit) {
        enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.d("D_OAUTH_CALL_D", "CALL FAIL, ${t}")
                failure(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                Log.d("D_OAUTH_CALL_D", "CALL OK, ${response}")
                success(response)
            }
        })
    }
}
