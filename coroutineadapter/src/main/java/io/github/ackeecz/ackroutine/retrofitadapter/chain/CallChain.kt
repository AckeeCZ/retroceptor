package io.github.ackeecz.ackroutine.retrofitadapter.chain

import io.github.ackeecz.ackroutine.retrofitadapter.interceptor.CallFactoryInterceptor
import retrofit2.Call

interface CallChain {

    val call: Call<*>

    val annotations: Array<out Annotation>

    fun proceed(call: Call<*>): Call<*>
}

class CallChainImpl(
    private val chainIndex: Int,
    private val actualCall: Call<*>,
    private val annotationArray: Array<out Annotation>,
    private val interceptors: List<CallFactoryInterceptor>
) : CallChain {

    override val call: Call<*>
        get() = actualCall

    override val annotations: Array<out Annotation>
        get() = annotationArray

    override fun proceed(call: Call<*>): Call<*> {
        check(chainIndex <= interceptors.lastIndex) {"chainIndex ${chainIndex + 1} does not match with actual interceptor size ${interceptors.size}" }

        val newChain = CallChainImpl(chainIndex + 1, call, annotations, interceptors)
        return interceptors[chainIndex].intercept(newChain)
    }
}
