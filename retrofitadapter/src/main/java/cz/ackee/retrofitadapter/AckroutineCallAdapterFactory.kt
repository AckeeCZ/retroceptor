package cz.ackee.retrofitadapter

import cz.ackee.retrofitadapter.interceptor.BodyCallExecuteInterceptor
import cz.ackee.retrofitadapter.interceptor.CallFactoryInterceptor
import cz.ackee.retrofitadapter.interceptor.ResponseCallExecuteInterceptor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * [CallAdapter.Factory] for [AckroutineCallAdapter].
 */
class AckroutineCallAdapterFactory(vararg interceptor: CallFactoryInterceptor) : CallAdapter.Factory() {

    private val interceptors = interceptor.toMutableList()

    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }

        if (returnType !is ParameterizedType) {
            throw IllegalStateException("Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>")
        }

        val responseType = getParameterUpperBound(0, returnType)
        val rawDeferredType = getRawType(responseType)

        val callInterceptor = if (rawDeferredType == Response::class.java) {
            if (responseType !is ParameterizedType) {
                throw IllegalStateException("Response must be parameterized as Response<Foo> or Response<out Foo>")
            }

            ResponseCallExecuteInterceptor()
        } else {
            BodyCallExecuteInterceptor()
        }

        val my = CompletableDeferred(1)








        return AckroutineCallAdapter<Any>(responseType, annotations,interceptors + callInterceptor)
    }
}