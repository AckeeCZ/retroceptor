package cz.ackee.ackroutine.interceptor

import cz.ackee.ackroutine.chain.CallChain
import kotlinx.coroutines.*
import retrofit2.HttpException

/**
 * Predefined interceptors.
 */

internal class BodyCallExecuteInterceptor<T> : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return CoroutineScope(Dispatchers.IO).async(start = CoroutineStart.LAZY) {
            val response = chain.call.execute()
            return@async if (response.isSuccessful) {
                response
            } else {
                throw HttpException(response)
            }
        }
    }
}

internal class ResponseCallExecuteInterceptor<T> : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return CoroutineScope(Dispatchers.IO).async(start = CoroutineStart.LAZY) {
            chain.call.execute()
        }
    }
}

/**
 * These are just for debugging, TODO: do not belong to the final lib !!!
 */

class LogInterceptor : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        val deferred = chain.proceed(chain.call)

        deferred.invokeOnCompletion { error ->
            if (deferred.isCompleted && error != null) {
                chain.call.request()
                // retrieve whole bunch of stuff from request and log it
            }
        }

        return deferred
    }
}

class MockOAuthInterceptor : CallFactoryInterceptor {

    private val isExpired = false

    override fun intercept(chain: CallChain): Deferred<*> {
        val deferred = chain.proceed(chain.call)

        return CoroutineScope(Dispatchers.IO).async(start = CoroutineStart.LAZY) {
            if (isExpired) {
                // call for a new token
            }

            deferred.await()
        }
    }
}