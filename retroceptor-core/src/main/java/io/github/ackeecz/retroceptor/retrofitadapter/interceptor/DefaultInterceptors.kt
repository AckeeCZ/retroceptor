package io.github.ackeecz.retroceptor.retrofitadapter.interceptor

import io.github.ackeecz.retroceptor.retrofitadapter.chain.CallChain
import retrofit2.Call

/**
 * Predefined interceptors.
 */

internal class CallInterceptor : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Call<*> = chain.call
}
