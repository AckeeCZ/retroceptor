package cz.ackee.ackroutine.interceptor

import cz.ackee.ackroutine.chain.CallChain
import kotlinx.coroutines.*
import retrofit2.HttpException

/**
 * Predefined interceptors.
 */

internal class BodyCallExecuteInterceptor : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return CoroutineScope(Dispatchers.Unconfined).async(start = CoroutineStart.LAZY) {
            val response = chain.call.execute()
            if (response.isSuccessful) {
                response
            } else {
                throw HttpException(response)
            }
        }
    }
}

internal class ResponseCallExecuteInterceptor : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return CoroutineScope(Dispatchers.Unconfined).async(start = CoroutineStart.LAZY) {
            chain.call.execute()
        }
    }
}