package com.wavesplatform.wallet.v2.data.helpers

import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.util.clone
import io.sentry.Sentry
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface
import java.lang.Exception
import java.util.*

class SentryHelper {

    companion object {
        fun logException(exception: Exception) {
            if (exception is RetrofitException) {
                Sentry.capture(EventBuilder()
                        .withTimestamp(Date())
                        .withLevel(Event.Level.ERROR)
                        .withSentryInterface(ExceptionInterface(exception))
                        .withRelease(BuildConfig.VERSION_NAME)
                        .withMessage(formatSentryMessage(exception)))
            } else {
                Sentry.capture(exception)
            }
        }

        private fun formatSentryMessage(retrofitException: RetrofitException): String {
            return "Error: ${retrofitException.kind.name}\n" +
                    "Url: ${retrofitException.url}\n" +
                    "Code: ${retrofitException.response?.code()}\n" +
                    "Message: ${retrofitException.response?.errorBody()?.clone()?.string().toString()}"
        }
    }
}