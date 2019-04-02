package cz.ackee.ackroutine

import cz.ackee.retrofitadapter.AckroutineCallAdapter
import cz.ackee.retrofitadapter.chain.CallChain
import cz.ackee.retrofitadapter.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred

/**
 * [AckroutineCallAdapter] interceptor that conditionally wraps original [Deferred] future with
 * token check & retrieval logic.
 */
class OAuthCallInterceptor(private val oAuthManager: CoroutineOAuthManager) : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return if (chain.annotations.any { it is IgnoreAuth }) {
            chain.proceed(chain.call)
        } else {
            oAuthManager.wrapDeferred { chain.proceed(if (with(chain.call) { isExecuted || isCanceled }) chain.call.clone() else chain.call) }
        }
    }
}