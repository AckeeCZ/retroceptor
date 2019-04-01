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
        val deferred = chain.proceed(chain.call)

        return if (chain.annotations.any { it is IgnoreAuth }) {
            Log.d("CALL", "No wrap token")
            deferred
        } else {
            Log.d("CALL", "With wrap token")
            deferred.wrapWithAcessTokenCheck(oAuthManager)
        }
    }
}