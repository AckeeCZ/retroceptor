package io.github.ackeecz.retroceptor.retrofitadapter

import io.github.ackeecz.retroceptor.retrofitadapter.interceptor.CallFactoryInterceptor
import io.github.ackeecz.retroceptor.retrofitadapter.interceptor.CallInterceptor
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * [CallAdapter.Factory] for [RetroceptorCallAdapter].
 */
class RetroceptorCallAdapterFactory(vararg interceptor: CallFactoryInterceptor) : CallAdapter.Factory() {

    private val interceptors = interceptor.toMutableList()

    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (Call::class.java != getRawType(returnType)) {
            return null
        }

        check(returnType is ParameterizedType) { "Call return type must be parameterized as Call<Foo> or Call<out Foo>" }

        val responseType = getParameterUpperBound(0, returnType)
        return RetroceptorCallAdapter<Any>(responseType, annotations, interceptors + CallInterceptor())
    }
}
