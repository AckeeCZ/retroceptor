package cz.ackee.ackroutine

import cz.ackee.ackroutine.core.DefaultOAuthCredentials
import cz.ackee.ackroutine.core.OAuthCredentials
import cz.ackee.ackroutine.core.OAuthStore
import junit.framework.TestCase.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Tests for [CoroutineOAuthManager] class.
 */
class OAuthManagerTest {

    private val accessToken = "access-token"
    private val refreshToken = "refresh-token"
    private val expiresIn = 3600L
    private val successResult = "Result"
    private val credentials = DefaultOAuthCredentials(accessToken, refreshToken, expiresIn)

    private val unauthorizedException = HttpException(Response.error<Any>(401, ResponseBody.create(null, "error body")))
    private val httpException = HttpException(Response.error<Unit>(404, ResponseBody.create(null, "error body")))

    private val store = OAuthStore(MockedSharedPreferences())

    private val refreshAction: (String) -> Deferred<OAuthCredentials> = { refresh ->
        if (refresh == refreshToken) CompletableDeferred(credentials) else throw unauthorizedException
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
        val manager = CoroutineOAuthManager(store, refreshAction)
        assertEquals(runBlocking { manager.wrapDeferred { CompletableDeferred(successResult) }.await() }, successResult)
    }

    @Test(expected = HttpException::class)
    fun testErrorRequest() {
        val manager = CoroutineOAuthManager(store, refreshAction)
        runBlocking {
            manager.wrapDeferred {
                CompletableDeferred<Unit>(null).apply {
                    completeExceptionally(httpException)
                }
            }.await()
        }
    }

    @Test
    fun testExpiredAccessTokenCheckLocal() {
        store.saveCredentials(credentials.copy(expiresIn = -1))
        assertTrue(store.tokenExpired())

        val manager = CoroutineOAuthManager(store, refreshAction)
        runBlocking { manager.wrapDeferred { CompletableDeferred(successResult) }.await() }

        assertFalse(store.tokenExpired())
    }

    @Test
    fun testExpiredAccessTokenSuccessRefresh() {
        val manager = CoroutineOAuthManager(store, refreshAction)

        val result = runBlocking {
            manager.wrapDeferred {
                if (firstRun) {
                    firstRun = false
                    throw unauthorizedException
                } else {
                    CompletableDeferred(successResult)
                }
            }.await()
        }

        assertEquals(successResult, result)
    }

    @Test(expected = HttpException::class)
    fun testExpiredAccessTokenErrorRefresh() {
        store.saveCredentials(credentials.copy(expiresIn = -1, refreshToken = refreshToken.reversed()))
        val manager = CoroutineOAuthManager(store, refreshAction)

        runBlocking {
            manager.wrapDeferred { CompletableDeferred(successResult) }.await()
        }
    }

    private fun refreshStore() {
        store.saveCredentials(credentials)
    }
}