package cz.ackee.ackroutine

import android.util.Log
import cz.ackee.retrofitadapter.AckroutineCallAdapter
import cz.ackee.retrofitadapter.chain.CallChain
import cz.ackee.retrofitadapter.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred
import retrofit2.Call

/**
 * [AckroutineCallAdapter] interceptor that conditionally wraps original [Deferred] future with
 * token check & retrieval logic.
 */
class OAuthCallInterceptor(private val oAuthManager: CoroutineOAuthManager) : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Call<*> {
        Log.d("D_OAUTH", "INTERCEPTING: ${chain.call.request()}")
        return if (chain.annotations.any { it is IgnoreAuth }) {
            Log.d("D_OAUTH", "NO AUTH, PROCEED")
            chain.proceed(chain.call)
        } else {
            Log.d("D_OAUTH", "AUTH, CHECK TOKENS")
            val callToProceedWith = if (with(chain.call) { isExecuted || isCanceled }) chain.call.clone() else chain.call
            chain.proceed(oAuthManager.wrapAuthCheck(callToProceedWith))
        }
    }
}
