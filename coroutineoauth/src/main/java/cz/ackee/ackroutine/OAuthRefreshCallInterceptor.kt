package cz.ackee.ackroutine

import cz.ackee.retrofitadapter.AckroutineCallAdapter
import cz.ackee.retrofitadapter.chain.CallChain
import cz.ackee.retrofitadapter.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred
import retrofit2.Call

/**
 * [AckroutineCallAdapter] interceptor that conditionally wraps original [Deferred] future with
 * token check & retrieval logic.
 */
class OAuthRefreshCallInterceptor(private val oAuthManager: OAuthManager) : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Call<*> {
        return if (chain.annotations.any { it is IgnoreAuth }) {
            chain.proceed(chain.call)
        } else {
            val callToProceedWith = if (with(chain.call) { isExecuted || isCanceled }) chain.call.clone() else chain.call
            chain.proceed(oAuthManager.wrapAuthCheck(callToProceedWith))
        }
    }
}