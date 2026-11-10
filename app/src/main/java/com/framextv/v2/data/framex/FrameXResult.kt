package com.framextv.v2.data.framex

sealed interface FrameXResult<out T> {
    data class Success<T>(val value: T) : FrameXResult<T>
    data class Failure(val message: String) : FrameXResult<Nothing>
}

fun <T> FrameXResult<T>.getOrNull(): T? = when (this) {
    is FrameXResult.Success -> value
    is FrameXResult.Failure -> null
}
