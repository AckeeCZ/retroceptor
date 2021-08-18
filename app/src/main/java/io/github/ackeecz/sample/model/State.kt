package io.github.ackeecz.sample.model

/**
 * Base state that can be maintained by any view model
 */
sealed class State<out T> {

    object Loading : State<Nothing>()
    object Empty : State<Nothing>()
    object Idle : State<Nothing>()
    data class Error(val error: Throwable) : State<Nothing>()
    data class Loaded<out T>(val data: T) : State<T>()
    data class Reloading<out T>(val previousData: T?) : State<T>()
}
