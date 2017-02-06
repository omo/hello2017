package es.flakiness.hello.androidaudio

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class AudioService() : Service() {
    companion object {
        fun sendCommand(ctx: Context, command: Command) {
            val intent = Intent(ctx, AudioService::class.java)
            intent.setAction(command.name)
            ctx.startService(intent)
        }

        fun findSampleRate() : Int {
            return intArrayOf(22050, 44100, 8000).find { c ->
                0 < AudioRecord.getMinBufferSize(c, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)
            } ?: 0
        }


        val SAMPLING_RATE = findSampleRate()
        private val TAG = "AudioService"
        private val BUFFER_SIZE_BYTES = SAMPLING_RATE * 2 * 2; // 2 sec
    }

    enum class Command {
        START,
        STOP
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return Service.START_STICKY
        }

        when (Command.valueOf(intent.action)) {
            Command.START -> startRecording()
            Command.STOP -> stopRecording()
        }

        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("not implemented")
    }

    // TODO(omo): Recording thread should be an object
    private val running = AtomicBoolean(false)

    private fun averageLevel(samples: ShortArray) : Float {
        val total = samples.fold(0L, { a, i -> a + (if (0 < i) i.toLong() else -i.toLong())})
        return total.toFloat()/(Short.MAX_VALUE*samples.size).toFloat()
    }

    private fun recordUntilDone() {
        val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE, AudioFormat.CHANNEL_IN_DEFAULT,  AudioFormat.ENCODING_PCM_16BIT,
                Math.max(BUFFER_SIZE_BYTES, AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)))
        try {
            audioRecord.startRecording()
            val silentBits = ArrayList<Int>()
            val buffer : ShortArray = ShortArray(SAMPLING_RATE/10)
            while (running.get()) {
                val ret = audioRecord.read(buffer, 0, buffer.size)
                if (ret < 0) {
                    L.e(TAG, "Read Error: ${ret}")
                    break;
                }

                silentBits.add((averageLevel(buffer)*100).toInt())
                if (silentBits.size == 10) {
                    L.i(TAG, "Silent:" + silentBits.joinToString(","))
                    silentBits.clear()
                }
            }
            L.i(TAG, "Done recording loop.")
            audioRecord.stop()
        } finally {
            audioRecord.release()
        }
    }

    private fun startRecording() {
        L.i(TAG, "startRecording")
        running.set(true)
        Thread({ recordUntilDone() }).start()
    }

    private fun stopRecording() {
        running.set(false)
        L.i(TAG, "stopRecording")
    }
}
