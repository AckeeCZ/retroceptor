package io.github.ackeecz.retroceptor.retrofitadapter.interceptor

import io.github.ackeecz.retroceptor.retrofitadapter.chain.CallChain
import retrofit2.Call

/**
 * Common interface for all call factory interfaces.
 */
interface CallFactoryInterceptor {

    fun intercept(chain: CallChain): Call<*>
}
