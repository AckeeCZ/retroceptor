package cz.ackee.ackroutine

import cz.ackee.retrofitadapter.chain.CallChain
import cz.ackee.retrofitadapter.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred

/**
 * TODO: to be continued...
 */
class OAuthCallInterceptor : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        val deferred = chain.proceed(chain.call)



        return deferred
    }
}