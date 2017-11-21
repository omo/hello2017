package es.flakiness.es.hellowear

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.wearable.activity.WearableActivity
import android.widget.TextView

class MainActivity : WearableActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById<TextView>(R.id.text)
        textView.setBackgroundColor(Color.BLUE)
//        // Enables Always-on
//        setAmbientEnabled()


        val i = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, i, 0)

        // XXX: Takes care of channels.
        val notifManager = NotificationManagerCompat.from(this)
        val notification = NotificationCompat.Builder(this).apply {
//            setContentText("I can say something here!") // This doesn't work
            setContentTitle("Hello!")
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.BigTextStyle().bigText("I can say something here!"))
            addAction(R.mipmap.ic_launcher, "Open", pi)
        }.build()

        notifManager.notify(1, notification)
    }
}
