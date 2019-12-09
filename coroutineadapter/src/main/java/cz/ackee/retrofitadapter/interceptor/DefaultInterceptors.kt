package cz.ackee.retrofitadapter.interceptor

import cz.ackee.retrofitadapter.chain.CallChain
import retrofit2.Call

/**
 * Predefined interceptors.
 */

internal class CallInterceptor: CallFactoryInterceptor {
    override fun intercept(chain: CallChain): Call<*> = chain.call
}
