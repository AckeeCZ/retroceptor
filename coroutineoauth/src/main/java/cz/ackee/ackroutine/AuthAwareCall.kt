package cz.ackee.ackroutine

import android.util.Log
import cz.ackee.ackroutine.core.ErrorChecker
import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.ackroutine.core.OAuthStore
import cz.ackee.retrofitadapter.interceptor.CallDelegate
import cz.ackee.retrofitadapter.interceptor.CallableDelegate
import kotlinx.coroutines.*
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.CoroutineContext

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
            refreshAndExecute(call, callback)
        } else {
            executeAndRefreshIfNeeded(call, callback)
        }
    }

    override fun cloneImpl(): Call<T> {
        return AuthAwareCall(call.clone(), refreshAction, store, errorChecker)
    }

    private fun refreshAndExecute(call: Call<T>, callback: Callback<T>) {
        val tokenCall = CoroutineCall {
            val value = refreshAction(store.refreshToken ?: "")
            store.saveCredentials(value)
        } as Call<Unit>

        tokenCall.execute(success = { _ ->
            call.execute(
                success = { callback.onResponse(this, it) },
                failure = { callback.onFailure(this, it) }
            )
        }, failure = {
            callback.onFailure(this, it)
        })
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

    private fun <U> Call<U>.execute(success: (Response<U>) -> Unit, failure: (Throwable) -> Unit) {
        enqueue(object : Callback<U> {
            override fun onFailure(call: Call<U>, t: Throwable) {
                failure(t)
            }

            override fun onResponse(call: Call<U>, response: Response<U>) {
                if (response.isSuccessful) {
                    success(response)
                } else {
                    failure(HttpException(response))
                }
            }
        })
    }
}
