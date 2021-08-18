package io.github.ackeecz.retroceptor.retrofitadapter.interceptor

import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface CallableDelegate<OUT> : Call<OUT>

abstract class CallDelegate<IN, OUT>(
    protected val proxy: Call<IN>
) : CallableDelegate<OUT> {

    override fun execute(): Response<OUT> = throw NotImplementedError()
    override fun enqueue(callback: Callback<OUT>) = enqueueImpl(callback)
    override fun clone(): Call<OUT> = cloneImpl()

    override fun cancel() = proxy.cancel()
    override fun request(): Request = proxy.request()
    override fun isExecuted() = proxy.isExecuted
    override fun isCanceled() = proxy.isCanceled
    override fun timeout() = proxy.timeout()

    abstract fun enqueueImpl(callback: Callback<OUT>)
    abstract fun cloneImpl(): Call<OUT>
}
