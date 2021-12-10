import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.ckziu_app.model.InProgress
import com.ckziu_app.model.Result
import com.ckziu_app.model.Success
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/* Copyright 2019 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

/**Casts [Success] to [Result] inside [LiveData] and returns [Success.resultValue].
 * If value is NOT [Success] then [IllegalStateException] is thrown.
 *
 * Shorter version of `(LiveData.value as Success).resultValue`.
 *
 * Should be used **only** for testing purposes.
 **/
@TestOnly
fun <VALUE_TYPE> LiveData<Result<VALUE_TYPE>>.getTestSuccessValue(): VALUE_TYPE {
    return when (this.value!!) {
        is Success<*> -> {
            @Suppress("UNCHECKED_CAST")
            ((this.getOrAwaitValue() as Success<*>).resultValue as VALUE_TYPE)
        }

        is InProgress<*> -> {
            throw IllegalStateException(
                "LiveData.value is InProgress type. You probably should await the value by, for example," +
                        " calling Job.join() on suspending function which changes the value of this LiveData."
            )
        }

        else -> {
            val liveDataValueClassName = this.value?.javaClass?.simpleName?.toString()
            throw IllegalStateException("LiveData.value is NOT Success type, type = $liveDataValueClassName")
        }
    }
}