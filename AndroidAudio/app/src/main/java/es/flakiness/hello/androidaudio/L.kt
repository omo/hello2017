package es.flakiness.hello.androidaudio

import android.util.Log

object L {
    inline fun d(tag: String, message: String) {
        if (Log.isLoggable(tag, Log.DEBUG))
            Log.d(tag, message)
    }

    inline fun i(tag: String, message: String) {
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, message)
        }
    }

    inline fun e(tag: String, message: String) {
        if (Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, message)
        }
    }
}