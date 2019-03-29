package cz.ackee.ackroutine.chain

import cz.ackee.ackroutine.interceptor.CallFactoryInterceptor
import kotlinx.coroutines.Deferred
import retrofit2.Call

interface CallChain {

    val call: Call<*>

    fun proceed(call: Call<*>): Deferred<*>
}

class CallChainImpl(
    private val chainIndex: Int,
    private val actualCall: Call<*>,
    private val interceptors: List<CallFactoryInterceptor>
) : CallChain {

    override val call: Call<*>
        get() = actualCall

    override fun proceed(call: Call<*>): Deferred<*> {
        if (chainIndex + 1 > interceptors.size) {
            throw IllegalStateException("chainIndex ${chainIndex + 1} does not match with actual interceptor size ${interceptors.size}")
        }

        val newChain = CallChainImpl(chainIndex + 1, call, interceptors)
        return interceptors[chainIndex].intercept(newChain)
    }
}