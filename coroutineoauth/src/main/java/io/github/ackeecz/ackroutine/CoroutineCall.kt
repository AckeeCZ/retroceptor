package io.github.ackeecz.ackroutine

import io.github.ackeecz.ackroutine.retrofitadapter.interceptor.CallableDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * [CallableDelegate] which invokes suspending function.
 */
internal class CoroutineCall<OUT>(val operation: suspend () -> OUT) : CallableDelegate<OUT>, CoroutineScope {

    companion object {

        private const val TIMEOUT_S: Long = 15
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    override fun enqueue(callback: Callback<OUT>) {
        launch {
            try {
                val value = operation()
                callback.onResponse(this@CoroutineCall, Response.success(value))
                job.complete()
            } catch (e: Exception) {
                callback.onFailure(this@CoroutineCall, e)
                job.completeExceptionally(e)
            }
        }
    }

    override fun isExecuted(): Boolean {
        return job.isCompleted
    }

    override fun clone(): Call<OUT> {
        return CoroutineCall(operation)
    }

    override fun isCanceled(): Boolean {
        return job.isCancelled
    }

    override fun cancel() {
        job.cancel()
    }

    override fun execute(): Response<OUT> {
        throw NotImplementedError()
    }

    override fun request(): Request {
        throw NotImplementedError()
    }

    override fun timeout(): Timeout {
        return Timeout().timeout(TIMEOUT_S, TimeUnit.SECONDS)
    }
}
