package cz.ackee.retrofitadapter.interceptor

import cz.ackee.retrofitadapter.chain.CallChain
import retrofit2.Call

/**
 * Common interface for all call factory interfaces.
 */
interface CallFactoryInterceptor {

    fun intercept(chain: CallChain): Call<*>
}