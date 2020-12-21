package cz.ackee.ackroutine

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [SingleCallHandler] class.
 */
class SingleCallHandlerTest {

    private lateinit var fakeWebServer: FakeWebServer

    @Before
    fun setup() {
        fakeWebServer = FakeWebServer()
    }

    @Test
    fun testSingleSuccessfulRequest() = runBlockingTest {
        val handler = SingleCallHandler<Int>()

        val res = handler.callSingle { fakeWebServer.makeSuccessfulRequest() }

        assertEquals(42, res)
        assertEquals(1, fakeWebServer.requestsMade)
    }

    @Test
    fun testSingleFailureRequest() {
        val handler = SingleCallHandler<Int>()

        assertThrows(Exception::class.java) {
            runBlockingTest {
                handler.callSingle { fakeWebServer.makeErrorRequest() }
            }
        }
        assertEquals(1, fakeWebServer.requestsMade)
    }

    @Test
    fun testMultipleSuccessfulRequests() = runBlockingTest {
        val handler = SingleCallHandler<Int>()

        val res = async { handler.callSingle { fakeWebServer.makeSuccessfulRequest() } }
        val resSecond = async { handler.callSingle { fakeWebServer.makeSuccessfulRequest() } }

        assertEquals(42, res.await())
        assertEquals(42, resSecond.await())
        assertEquals(1, fakeWebServer.requestsMade)
    }

    @Test
    fun testMultipleFailedRequests() = runBlockingTest {
        val handler = SingleCallHandler<Int>()

        val resFirst = launch {
            var exceptionThrown = false
            try {
                handler.callSingle { fakeWebServer.makeErrorRequest() }
            } catch (e: Exception) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)
        }

        val resSecond = launch {
            var exceptionThrown = false
            try {
                handler.callSingle { fakeWebServer.makeErrorRequest() }
            } catch (e: Exception) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)
        }

        resFirst.join()
        resSecond.join()

        assertEquals(1, fakeWebServer.requestsMade)
    }

    private class FakeWebServer {

        var requestsMade: Int = 0

        suspend fun makeSuccessfulRequest(): Int {
            delay(100)
            requestsMade++
            return 42
        }

        suspend fun makeErrorRequest(): Int {
            delay(100)
            requestsMade++
            throw Exception()
        }
    }
}