package cz.ackee.ackroutine

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * Handles asynchronous calls in a way that guarantees that no parallel calls are made.
 */
class SingleCallHandler<T> {

    private val mutex = Mutex()

    private var authRefreshResult: Result<T> = Result.failure(IllegalStateException("Uninitialized result"))
    private var requestsCount: AtomicInteger = AtomicInteger(0)

    suspend fun callSingle(requestCall: suspend () -> T): T {
        val pendingRequests = requestsCount.incrementAndGet()

        mutex.withLock {
            return try {
                if (pendingRequests == 1) {
                    // First request, perform requestCall
                    requestCall().also { result -> authRefreshResult = Result.success(result) }
                } else {
                    // Request fired before first one completed, get its cached result instead of
                    // calling requestCall again
                    authRefreshResult.getOrThrow()
                }
            } catch (e: Exception) {
                authRefreshResult = Result.failure(e)
                throw e
            } finally {
                requestsCount.decrementAndGet()
            }
        }
    }
}