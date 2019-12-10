package cz.ackee.retrofitadapter

import cz.ackee.retrofitadapter.chain.CallChainImpl
import cz.ackee.retrofitadapter.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * Call adapter for [Deferred] interface.
 */
class AckroutineCallAdapter<T>(
    private val responseType: Type,
    private val annotations: Array<out Annotation>,
    private val interceptors: List<CallFactoryInterceptor>
) : CallAdapter<T, Call<T>> {

    override fun responseType() = responseType

    @Suppress("UNCHECKED_CAST")
    override fun adapt(call: Call<T>): Call<T> {
        val chain = CallChainImpl(0, call, annotations, interceptors)
        return chain.proceed(call) as Call<T>
    }
}
