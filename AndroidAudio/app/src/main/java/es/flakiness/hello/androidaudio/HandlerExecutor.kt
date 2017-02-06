package es.flakiness.hello.androidaudio

import android.os.Handler
import java.util.concurrent.Executor

class HandlerExecutor(val handler: Handler) : Executor {
    override fun execute(command: Runnable?) {
        handler.post(command)
    }
}