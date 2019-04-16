package cz.ackee.retrofitadapter.interceptor

import android.util.Log
import cz.ackee.retrofitadapter.chain.CallChain
import kotlinx.coroutines.*
import retrofit2.HttpException

/**
 * Predefined interceptors.
 */

internal class BodyCallExecuteInterceptor : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return GlobalScope.async(start = CoroutineStart.LAZY) {
            val response = chain.call.execute()
            if (response.isSuccessful) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }
}

internal class ResponseCallExecuteInterceptor : CallFactoryInterceptor {

    override fun intercept(chain: CallChain): Deferred<*> {
        return GlobalScope.async(start = CoroutineStart.LAZY) {
            chain.call.execute()
        }
    }
}