package cz.ackee.ackroutine

import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.ackroutine.core.OAuthStore
import cz.ackee.retrofitadapter.interceptor.CallableDelegate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

/**
 * Tests for [OAuthManager] class.
 */
class OAuthManagerTest {

    private val accessToken = "access-token"
    private val refreshToken = "refresh-token"
    private val expiresIn = 3600L
    private val successResult = "Result"
    private val credentials = OAuthCredentials(accessToken, refreshToken, expiresIn)

    private val unauthorizedException = HttpException(Response.error<Any>(401, ResponseBody.create(null, "error body")))
    private val httpException = HttpException(Response.error<Unit>(404, ResponseBody.create(null, "error body")))

    private val store = OAuthStore(MockedSharedPreferences())

    private val refreshAction: suspend (OAuthCredentials?) -> OAuthCredentials = ::refreshToken

    private suspend fun refreshToken(oldCredentials: OAuthCredentials?): OAuthCredentials {
        return if (oldCredentials?.accessToken == refreshToken) credentials else throw unauthorizedException
    }

    private val successCall = object : CallableDelegate<String> {
        override fun enqueue(callback: Callback<String>) {
            callback.onResponse(this, Response.success(successResult))
        }

        override fun isExecuted(): Boolean = false
        override fun clone(): Call<String> = this
        override fun isCanceled(): Boolean = false
        override fun cancel() {}
        override fun execute(): Response<String> = throw NotImplementedError()
        override fun request(): Request = throw NotImplementedError()
        override fun timeout(): Timeout = Timeout.NONE
    }

    private val failureCall = object : CallableDelegate<String> {
        override fun enqueue(callback: Callback<String>) {
            val response = Response.error<Any>(
                403,
                ResponseBody.create(MediaType.parse("application/json"), "{ reason: \"donno\"")
            )

            callback.onFailure(this, HttpException(response))
        }

        override fun isExecuted(): Boolean = false
        override fun clone(): Call<String> = this
        override fun isCanceled(): Boolean = false
        override fun cancel() {}
        override fun execute(): Response<String> = throw NotImplementedError()
        override fun request(): Request = throw NotImplementedError()
        override fun timeout(): Timeout = Timeout.NONE
    }

    private var firstRun = true

    @Before
    fun setup() {
        refreshStore()
        firstRun = true
    }

    @After
    fun clean() {
        refreshStore()
    }

    @Test
    fun testSuccessRequest() {
        val manager = OAuthManager(store, refreshAction)

        manager.wrapAuthCheck(successCall).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                throw IllegalStateException("Unexpected case")
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                assertEquals(response.body(), successResult)
            }
        })
    }

    @Test
    fun testErrorRequest() {
        val manager = OAuthManager(store, refreshAction)

        manager.wrapAuthCheck(failureCall).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                assertTrue(t is HttpException)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                throw IllegalStateException("Unexpected case")
            }
        })
    }

    @Test
    fun testExpiredAccessTokenCheckLocal() {
        store.saveCredentials(credentials.copy(expiresIn = -1))
        assertTrue(store.tokenExpired())

        val manager = OAuthManager(store, refreshAction)
        manager.wrapAuthCheck(successCall).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                throw IllegalStateException("Unexpected case")
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                assertFalse(store.tokenExpired())
            }
        })
    }

    @Test
    fun testExpiredAccessTokenErrorRefresh() {
        store.saveCredentials(credentials.copy(expiresIn = -1, refreshToken = refreshToken.reversed()))
        val manager = OAuthManager(store, refreshAction)

        manager.wrapAuthCheck(failureCall).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                assertTrue(t is HttpException)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                throw IllegalStateException("Unexpected case")
            }
        })
    }

    private fun refreshStore() {
        store.saveCredentials(credentials)
    }
}
