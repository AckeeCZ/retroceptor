package io.github.ackeecz.retroceptor

import io.github.ackeecz.retroceptor.retrofitadapter.RetroceptorCallAdapter
import io.github.ackeecz.retroceptor.retrofitadapter.chain.CallChain
import io.github.ackeecz.retroceptor.retrofitadapter.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred
import retrofit2.Call

/**
 * [RetroceptorCallAdapter] interceptor that conditionally wraps original [Deferred] future with
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
