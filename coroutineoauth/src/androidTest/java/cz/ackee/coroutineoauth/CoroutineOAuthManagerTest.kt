package cz.ackee.coroutineoauth

import androidx.test.InstrumentationRegistry.getTargetContext
import cz.ackee.ackroutine.CoroutineOAuthManager
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
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.HttpException
import retrofit2.Response

/**
 * Tests for [CoroutineOAuthManager] class
 */
@RunWith(JUnit4::class)
class CoroutineOAuthManagerTest {

    private var firstRun: Boolean = false

    private val accessToken = "access-token"
    private val refreshToken = "refresh-token"
    private val expiresIn = 3600L
    private val successResult = "Result"
    private val credentials = DefaultOAuthCredentials(accessToken, refreshToken, expiresIn)

    private val unauthorizedException = HttpException(Response.error<Any>(401, ResponseBody.create(null, "error body")))
    private val httpException = HttpException(Response.error<Unit>(404, ResponseBody.create(null, "error body")))

    private val refreshAction: (String) -> Deferred<OAuthCredentials> = { refresh ->
        if (refresh == refreshToken) CompletableDeferred(credentials) else throw unauthorizedException
    }

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
        val manager = CoroutineOAuthManager(getTargetContext(), refreshAction)
        assertEquals(runBlocking { manager.wrapDeferred { CompletableDeferred(successResult) }.await() }, successResult)
    }

    @Test(expected = HttpException::class)
    fun testErrorRequest() {
        val manager = CoroutineOAuthManager(getTargetContext(), refreshAction)
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
        val store = OAuthStore(getTargetContext()).also {
            it.saveCredentials(credentials.copy(expiresIn = -1))
        }

        assertTrue(store.tokenExpired())

        val manager = CoroutineOAuthManager(getTargetContext(), refreshAction)
        runBlocking { manager.wrapDeferred { CompletableDeferred(successResult) }.await() }

        assertFalse(store.tokenExpired())
    }

    @Test
    fun testExpiredAccessTokenSuccessRefresh() {
        val manager = CoroutineOAuthManager(getTargetContext(), refreshAction)

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
        OAuthStore(getTargetContext()).saveCredentials(credentials.copy(expiresIn = -1, refreshToken = refreshToken.reversed()))
        val manager = CoroutineOAuthManager(getTargetContext(), refreshAction)

        runBlocking {
            manager.wrapDeferred { CompletableDeferred(successResult) }.await()
        }
    }

    private fun refreshStore() {
        OAuthStore(getTargetContext()).saveCredentials(credentials)
    }
}