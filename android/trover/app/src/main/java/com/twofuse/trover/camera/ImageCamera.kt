/**
 * Created by dmoffett on 12/12/17.
 */
package com.twofuse.trover.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Handler
import android.util.Log

/**
 * Helper class to deal with methods to deal with images from the camera.
 *
 * TRy this code.
 *
 * https://github.com/android/camera-samples/tree/master/Camera2SlowMotion
 *
 */
class ImageCamera  private constructor() {
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var startTime: Long = 0
    private var stopTime: Long = 0

    /**
     * An [ImageReader] that handles still image capture.
     */
    private var mImageReader: ImageReader? = null
    private var captureRequest: CaptureRequest? = null

    private object InstanceHolder {
        val mCamera = ImageCamera()
    }

    /**
     * Initialize the camera device
     */
    fun initializeCamera(context: Context,
                         backgroundHandler: Handler?,
                         imageAvailableListener: OnImageAvailableListener?) {
        // Discover the camera instance
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var camIds = arrayOf<String>()
        try {
            camIds = manager.cameraIdList
        } catch (e: CameraAccessException) {
            Log.d(TAG, "Cam access exception getting IDs", e)
        }
        if (camIds.size < 1) {
            Log.d(TAG, "No cameras found")
            return
        }
        val id = camIds[0]
        Log.d(TAG, "Using camera id $id")

        // Initialize the image processor
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT, ImageFormat.JPEG, MAX_IMAGES)
        mImageReader?.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)
        if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Open the camera resource
            try {
                manager.openCamera(id, mStateCallback, backgroundHandler)
            } catch (cae: CameraAccessException) {
                Log.d(TAG, "Camera access exception", cae)
            }
        }
    }

    /**
     * Callback handling device state changes
     */
    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            Log.d(TAG, "Opened camera.")
            mCameraDevice = cameraDevice
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Log.d(TAG, "Camera disconnected, closing.")
            cameraDevice.close()
        }

        override fun onError(cameraDevice: CameraDevice, i: Int) {
            Log.d(TAG, "Camera device error, closing.")
            cameraDevice.close()
        }

        override fun onClosed(cameraDevice: CameraDevice) {
            Log.d(TAG, "Closed camera, releasing")
            mCameraDevice = null
        }
    }

    /**
     * Begin a still image capture
     */
    fun startCapturingImages() {
        if (mCameraDevice == null) {
            Log.w(TAG, "Cannot capture image. Camera not initialized.")
            return
        }

        // Here, we create a CameraCaptureSession for capturing still images.
        try {
            mCameraDevice?.createCaptureSession(listOf(mImageReader!!.surface), mSessionCallback, null)
            // mCameraDevice?.createConstrainedHighSpeedCaptureSession(listOf(mImageReader!!.surface), mSessionCallback, null)
        } catch (cae: CameraAccessException) {
            Log.d(TAG, "access exception while preparing pic", cae)
        }
    }

    /**
     * Callback handling session state changes
     */
    private val mSessionCallback: CameraCaptureSession.StateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            // The camera is already closed
            if (mCameraDevice == null) {
                return
            }

            // When the session is ready, we start capture.
            mCaptureSession = cameraCaptureSession
            startCaputure()
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            Log.w(TAG, "Failed to configure camera")
        }
    }

    /**
     * Execute a new capture request within the active session
     */
    private fun triggerImageCapture() {
        try {
            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)
            // captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            // captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
            Log.d(TAG, "Session initialized.")
            captureRequest = captureBuilder.build()
        } catch (cae: CameraAccessException) {
            Log.d(TAG, "camera capture exception")
        } catch (isx: IllegalStateException) {
            Log.e(TAG, "Error trying to capture another photo.")
        }
    }


    /**
     * Callback handling capture session events
     */
    private val mCaptureCallback: CaptureCallback = object : CaptureCallback() {

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult) {
            Log.d(TAG, "Partial result")
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult) {
        }
    }

    private fun startCaputure(){
        if(captureRequest == null) {
            triggerImageCapture()
        }
        startTime = System.currentTimeMillis()
        val aCaptureRequest = captureRequest
        if(aCaptureRequest != null)
            mCaptureSession?.setRepeatingRequest(aCaptureRequest, mCaptureCallback, null)
    }

    fun stopCapture(){
        mCaptureSession?.stopRepeating()
    }

    /**
     * Close the camera resources
     */
    fun shutDown() {
        mCameraDevice?.close()
    }

    companion object {
        private val TAG = ImageCamera::class.java.simpleName
        private const val IMAGE_WIDTH = 320
        private const val IMAGE_HEIGHT = 240
        private const val MAX_IMAGES = 10
        @JvmStatic
        val instance: ImageCamera get() = InstanceHolder.mCamera

    }
}