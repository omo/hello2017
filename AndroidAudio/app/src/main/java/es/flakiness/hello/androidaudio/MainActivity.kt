package es.flakiness.hello.androidaudio

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import android.Manifest.permission
import android.Manifest.permission.RECORD_AUDIO
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat



class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
        val PERMISSIO_REQUEST_CODE = 1
    }

    // https://github.com/JetBrains/kotlin-examples/blob/master/gradle/android-butterknife/app/src/main/java/org/example/kotlin/butterknife/SimpleActivity.kt
    @BindView(R.id.button_start_service)
    lateinit var startServiceButton: Button;
    @BindView(R.id.button_stop_service)
    lateinit var stopServiceButton: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        requestPermissions()
    }

    @OnClick(R.id.button_start_service)
    fun startServivice() = AudioService.sendCommand(this, AudioService.Command.START)

    @OnClick(R.id.button_stop_service)
    fun stopServivice() = AudioService.sendCommand(this, AudioService.Command.STOP)

    private fun requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            return
        // TODO(omo): Show explanation dialog if needed by ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.RECORD_AUDIO), PERMISSIO_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != PERMISSIO_REQUEST_CODE || permissions.isEmpty()) {
            // TODO(morrita): Show some dialog
            finish()
            return
        }
    }
}
