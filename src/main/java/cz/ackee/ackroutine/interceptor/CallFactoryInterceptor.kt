package cz.ackee.ackroutine.interceptor

import cz.ackee.ackroutine.chain.CallChain
import kotlinx.coroutines.Deferred

/**
 * Common interface for all call factory interfaces.
 */
interface CallFactoryInterceptor {

    fun intercept(chain: CallChain): Deferred<*>
}