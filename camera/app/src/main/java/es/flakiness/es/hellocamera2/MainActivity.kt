package es.flakiness.es.hellocamera2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Bundle
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.subjects.SingleSubject
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions


fun <T> SingleSubject<T>.successIfNeeded(t: T) {
    if (!this.hasValue())
        onSuccess(t)
}


data class Dimension(val width: Int, val height: Int) {
    val isEmpty get() = width == 0 && height == 0

    fun fit(dimension: Dimension) : Dimension =
        if (dimension.width < dimension.height * this.width / this.height)
            Dimension(dimension.height * this.width / this.height, dimension.height)
        else
            Dimension(dimension.width, dimension.width * this.height / this.width)
}


class CameraTextureView : TextureView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defstyle: Int) : super(context, attrs, defstyle)

    var ratio: Dimension = Dimension(0, 0)
        set(value) {
            field = value
            requestLayout()
        }

    val dimension: Dimension get() = Dimension(width, height)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val given = Dimension(widthMeasureSpec, heightMeasureSpec)
        if (ratio.isEmpty) {
            given
        } else {
            ratio.fit(given)
        }.let {
            setMeasuredDimension(it.width, it.height)
        }
    }
}

class Shooter(dimension: Dimension, activity: Activity) {
    val displayRotation = activity.windowManager.defaultDisplay.rotation;
    val dispplaySize = Point().apply { activity.windowManager.defaultDisplay.getSize(this) }
    val manager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager;

    private fun pickCamera() : String? = manager.cameraIdList.first {
        manager.getCameraCharacteristics(it).let {
            val frontFacing = it.get(CameraCharacteristics.LENS_FACING).let { it != CameraCharacteristics.LENS_FACING_FRONT }
            val hasScalerStreamConfig = it.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) != null
            frontFacing && hasScalerStreamConfig
        }
    }

    private fun setup(id: String) {
        val crs = manager.getCameraCharacteristics(id)
        val map = crs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val largest = map.getOutputSizes(ImageFormat.JPEG).maxBy { it.height * it.width }!!
        val reader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 2)
        // XXX: setOnImageAvailableListener
        val sensorOrientation = crs.get((CameraCharacteristics.SENSOR_ORIENTATION))
        val swappedDimension = when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> sensorOrientation in listOf(90, 270)
            Surface.ROTATION_90, Surface.ROTATION_270 -> sensorOrientation in listOf(0, 180)
            else -> throw RuntimeException("Invalid display rotation: ${displayRotation}")
        }

    }
}

@RuntimePermissions
class MainActivity : Activity() {
    val textureAvailable : SingleSubject<Dimension> = SingleSubject.create()
    val cameraPermission : SingleSubject<Activity> = SingleSubject.create()
    var shooter: Single<Shooter> = textureAvailable.zipWith(cameraPermission, BiFunction { s, a ->  Shooter(s, a)})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surface.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) = Unit // XXX Impl
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) =
                    textureAvailable.successIfNeeded(Dimension(width, height))
        }

        prepareCameraWithPermissionCheck()

        shooter.subscribe(Consumer{
            Toast.makeText(this, "Shooter is created!", Toast.LENGTH_LONG).show()
        })
    }

    override fun onResume() {
        super.onResume()
        if (surface.isAvailable)
            textureAvailable.successIfNeeded(surface.dimension)
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun prepareCamera() {
        cameraPermission.successIfNeeded(this)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun cameraDenied() {
        Toast.makeText(this, "You need camera!", Toast.LENGTH_LONG).show()
        finish()
    }
}
