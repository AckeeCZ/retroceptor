package cz.ackee.ackroutine

import cz.ackee.ackroutine.chain.CallChainImpl
import cz.ackee.ackroutine.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * Call adapter for [Deferred] interface.
 */
class AckroutineCallAdapter<T>(
    private val responseType: Type,
    private val interceptors: List<CallFactoryInterceptor>
) : CallAdapter<T, Deferred<T>> {

    override fun responseType() = responseType

    @Suppress("UNCHECKED_CAST")
    override fun adapt(call: Call<T>): Deferred<T> {
        val chain = CallChainImpl(0, call, interceptors)
        return chain.proceed(call) as Deferred<T>
    }
}