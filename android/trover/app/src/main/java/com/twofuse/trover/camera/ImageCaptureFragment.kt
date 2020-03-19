package com.twofuse.trover.camera

import android.graphics.BitmapFactory
import android.media.Image
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.twofuse.io.iomanager.IOMgrRequest
import com.twofuse.trover.R
import com.twofuse.trover.TRoverImageCapture
import com.twofuse.trover.camera.ImageCamera.Companion.instance
import com.twofuse.trover.iomanager.FileUploadReq
import com.twofuse.trover.iomanager.IOManagerCallback
import com.twofuse.trover.iomanager.IOReqInterface
import com.twofuse.trover.iomanager.SaveBitmapRequest
import com.twofuse.trover.utils.BusEvent
import com.twofuse.trover.utils.OnewayBus.EndPoint
import com.twofuse.trover.utils.TFLog
import com.twofuse.trover.utils.TFUtils
import java.io.File
import java.net.MalformedURLException

/**
 * Created by dmoffett on 12/8/17.
 */
class ImageCaptureFragment : Fragment(), EndPoint, IOManagerCallback, View.OnClickListener {
    lateinit var pictureCountTextView: TextView
    lateinit var imageRateTextView: TextView
    lateinit var ipAddressTextView: EditText
    lateinit var pictureTakingButton: ImageButton
    private var isRecording = false
    private var imageCount = 0
    private var startTime = 0L
    private var imageCamera: ImageCamera? = null
    private var cameraHandler: Handler? = null
    private var cameraThread: HandlerThread? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.image_capture_fragment, container, false)
        pictureCountTextView = view.findViewById(R.id.picture_count)
        imageRateTextView = view.findViewById(R.id.images_per_sec_count)
        ipAddressTextView = view.findViewById(R.id.ip_address)
        pictureTakingButton = view.findViewById(R.id.picture_start_stop)
        pictureTakingButton.setOnClickListener(this)
        return view
    }

    override fun onResume() {
        super.onResume()
        TRoverImageCapture.getEventBus().addEndpoint(this)
        prepareCamera()
    }

    override fun onPause() {
        TRoverImageCapture.getEventBus().removeEndpoint(this)
        imageCamera?.shutDown()
        super.onPause()
    }

    private fun prepareCamera() {
        TFLog.d("prepareCamera")
        cameraThread = HandlerThread("CameraBackground")
        cameraThread?.start()
        cameraHandler = Handler(cameraThread!!.looper)
        imageCamera = instance
        val aContext = activity
        if(aContext != null)
            imageCamera?.initializeCamera(aContext, cameraHandler, mOnImageAvailableListener)
    }

    /**
     * Process image data as desired.
     */
    private fun processImage(image: Image) {
        TFLog.d("ProcessImage")
        //Process image data
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer[bytes]
        val currentBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val file = File(activity?.getExternalFilesDir(null), TFUtils.randomString(16) + ".jpg")
        val saveBitmapRequest = SaveBitmapRequest(currentBitmap, file)
        saveBitmapRequest.setCallback(this)
        TRoverImageCapture.getIoManager().addRequest(saveBitmapRequest)
    }

    private val mOnImageAvailableListener = OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage()
        if(image != null) {
            processImage(image)
            image.close()
        }
    }

    override fun onClick(view: View) {
        if (isRecording) {
            imageCamera?.stopCapture()
            isRecording = false
            pictureTakingButton.setImageResource(R.drawable.ic_play_arrow_24px)
        } else {
            isRecording = true
            startTime = System.currentTimeMillis()
            imageCamera!!.startCapturingImages()
            pictureTakingButton.setImageResource(R.drawable.ic_pause_circle_filled_24px)
        }
    }

    override fun newEvent(anEvent: BusEvent) {

    }

    fun postResults(imageCount: Int, startTime: Long, stopTime: Long){
        pictureCountTextView.text = String.format("%d", imageCount)
        val elapsedTimeInSeconds = (stopTime - startTime)/1000
        if(elapsedTimeInSeconds > 0)
            imageRateTextView.text = String.format("%d/s", imageCount/elapsedTimeInSeconds)
    }

    override fun ioRequestFinished(ioReq: IOReqInterface) {
        if (ioReq is SaveBitmapRequest) {
            val saveBitmapRequest = ioReq
            val bitmap = saveBitmapRequest.bitmap
            bitmap?.recycle()
            saveBitmapRequest.nullBitmap()
            // Ship Image
            val ipAddress = ipAddressTextView.text
            if(!ipAddress.isBlank()) {
                val urlStr = "http://${ipAddress}:3000/image_capture"
                try {
                    val fileUploadReq = FileUploadReq(urlStr, saveBitmapRequest.file, "image/jpg")
                    fileUploadReq.setCallback(this)
                    TRoverImageCapture.getIoManager().addRequest(fileUploadReq)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
                imageCount++
                postResults(imageCount, startTime, System.currentTimeMillis())
            } else {
                Toast.makeText(activity, getText(R.string.ip_address_missing), Toast.LENGTH_LONG).show()
            }
        } else if (ioReq is FileUploadReq) {
            if (ioReq.getRequestStatusCode() === IOMgrRequest.RequestStatusCode.OK) {
                TFLog.d("File uploaded.")
            } else {
                TFLog.d("File upload failed.")
            }
        }
    }

    companion object {
    }

}