package com.ckziu_app.model

/** Custom class designed for indicating state of some kind of request.
 *
 * State might have three values: [Success], [InProgress], [Failure]
 *
 * */
sealed class Result<out T> {
    fun isSuccess() = this is Success
    fun isInProgress() = this is InProgress
    fun isFailure() = this is Failure

    /** Performs action on [Result] **only** when generic type is [Success].*/
    fun ifSuccessThen(action: (success: Success<T>) -> Unit) {
        if (this is Success) action(this)
    }

    /** Performs action on [Result] **only** when generic type is [InProgress].*/
    fun ifInProgressThen(action: (InProgress<T>) -> Unit) {
        if (this is InProgress) action(this)

    }

    /** Performs action on [Result] **only** when generic type is [Failure].*/
    fun ifFailureThen(action: (Failure<T>) -> Unit) {
        if (this is Failure) action(this)
    }

}

/** Indicates that request was finished successfully.
 * @param resultValue returned value of successful request.*/
class Success<out T>(val resultValue: T) : Result<T>()

/** Indicates that request is still in progress.*/
class InProgress<out T> : Result<T>()

/** Indicates that request was finished unsuccessful.*/
class Failure<out T>(
    val errorMsg: String? = null,
    val error: Throwable? = null
) : Result<T>()