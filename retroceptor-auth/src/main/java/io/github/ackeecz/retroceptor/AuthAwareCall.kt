package io.github.ackeecz.retroceptor

import io.github.ackeecz.retroceptor.core.AuthCredentials
import io.github.ackeecz.retroceptor.core.AuthErrorChecker
import io.github.ackeecz.retroceptor.core.AuthStore
import io.github.ackeecz.retroceptor.retrofitadapter.interceptor.CallDelegate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

/**
 * [CallDelegate] which handles credentials retrieval.
 */
internal class AuthAwareCall<T, C : AuthCredentials>(
    private val call: Call<T>,
    private val refreshAction: suspend (C?) -> C,
    private val store: AuthStore<C>,
    private val errorChecker: AuthErrorChecker
) : CallDelegate<T, T>(call) {

    override fun enqueueImpl(callback: Callback<T>) {
        if (store.credentialsExpired()) {
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
            val value = refreshAction(store.authCredentials)
            store.saveCredentials(value)
        }

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
                if (errorChecker.invalidCredentials(it)) {
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
