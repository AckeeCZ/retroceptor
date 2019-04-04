package cz.ackee.retrofitadapter.interceptor

import cz.ackee.retrofitadapter.chain.CallChain
import kotlinx.coroutines.Deferred

/**
 * Common interface for all call factory interfaces.
 */
interface CallFactoryInterceptor {

    fun intercept(chain: CallChain): Deferred<*>
}