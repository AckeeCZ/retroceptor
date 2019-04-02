package cz.ackee.ackroutine

import android.util.Log
import cz.ackee.retrofitadapter.chain.CallChain
import cz.ackee.retrofitadapter.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred

/**
 * TODO: to be continued...
 */
class OAuthCallInterceptor(private val oAuthManager: CoroutineOAuthManager) : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return if (chain.annotations.any { it is IgnoreAuth }) {
            Log.d("OAuth Interceptor", "Proceed without wrap")
            chain.proceed(chain.call)
        } else {
            Log.d("OAuth Interceptor", "Proceed with wrap")

            oAuthManager.wrapDeferred { chain.proceed(if (with(chain.call) { isExecuted || isCanceled }) chain.call.clone() else chain.call) }
        }
    }
}