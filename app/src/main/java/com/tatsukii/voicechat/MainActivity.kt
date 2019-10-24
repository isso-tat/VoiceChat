package com.tatsukii.voicechat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {
    var textureView : TextureView? = null;
    var cameraDevice : CameraDevice? = null;
    val previewSize : Size = Size(100, 100);
    var imageReader : ImageReader? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textureView = findViewById(R.id.mySurfaceView)
        textureView?.surfaceTextureListener = surfaceTextureListener
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            imageReader = ImageReader.newInstance(width, height,
                ImageFormat.JPEG, /*maxImages*/ 2)

            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {

        }

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {

        }

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
            return false
        }
    }

    fun openCamera() {
        var manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            var camerId: String = manager.getCameraIdList()[0]
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
                return
            }
            manager.openCamera(camerId, stateCallback, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 200)
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            this@MainActivity.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
            this@MainActivity.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
            finish()
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = textureView?.surfaceTexture
            texture?.setDefaultBufferSize(previewSize.width, previewSize.height)
            val surface = Surface(texture)
            var previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder.addTarget(surface)
            cameraDevice?.createCaptureSession(Arrays.asList(surface, imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {

                        if (cameraDevice == null) return
                        var captureSession = cameraCaptureSession
                        try {
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            var previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(previewRequest,
                                null, Handler()
                            )
                        } catch (e: CameraAccessException) {
                            Log.e("erfs", e.toString())
                        }

                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        //Tools.makeToast(baseContext, "Failed")
                    }
                }, null)
        } catch (e: CameraAccessException) {
            Log.e("erf", e.toString())
        }
    }
}
