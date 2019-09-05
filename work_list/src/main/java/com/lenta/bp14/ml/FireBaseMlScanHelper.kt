package com.lenta.bp14.ml

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.*
import androidx.camera.core.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.lenta.shared.utilities.Logg
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit


class FireBaseMlScanHelper(val context: Context) {

    private var viewLifecycleOwner: LifecycleOwner? = null
    private var rootView: View? = null
    private var canvasForScanDetection: CanvasForScanDetection? = null
    private lateinit var checkStatusFunction: (String) -> CheckStatus?

    private var ratio = 1F

    fun onViewCreated(viewLifecycleOwner: LifecycleOwner, canvasForScanDetection: CanvasForScanDetection, rootView: View, textureView: TextureView, checkStatusFunction: (String) -> CheckStatus?) {
        this.viewLifecycleOwner = viewLifecycleOwner
        this.canvasForScanDetection = canvasForScanDetection
        this.rootView = rootView
        this.checkStatusFunction = checkStatusFunction
        startCamera(textureView)
    }

    fun onDestroyView() {
        this.viewLifecycleOwner = null
        this.canvasForScanDetection = null
        this.rootView = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera(textureView: TextureView) {

        val targetSize = getMaximumSize() ?: return

        rootView?.apply {
            val params = layoutParams as ViewGroup.LayoutParams
            ratio = rootView.width / targetSize.height.toFloat()
            params.height = (targetSize.width * ratio).toInt()
            this.layoutParams = params
            rootView.invalidate()
        }


        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetResolution(targetSize)
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)

            textureView.surfaceTexture = it.surfaceTexture
        }


        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // Use a worker thread for image analysis to prevent glitches
            setTargetResolution(targetSize)
            //setTargetAspectRatio(Rational(SCALE.toInt(), SCALE.toInt()))
            setImageQueueDepth(5)
            val analyzerThread = HandlerThread(
                    "LuminosityAnalysis"
            ).apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            analyzer = LuminosityAnalyzer()
        }

        CameraX.bindToLifecycle(viewLifecycleOwner, preview, analyzerUseCase)

        //зум можно устанавливать только после CameraX.bindToLifecycle
        //val centerPoint = Size(targetSize.width / 2, targetSize.height / 2)
        val centerPoint = Size(400, 400)
        val zoomValue = 0.8
        val xZoom = (centerPoint.width * zoomValue).toInt()
        val yZoom = (centerPoint.height * zoomValue).toInt()

        val originalZoom = Rect(
                0,
                0,
                3200,
                3200
        )

        val zoom = Rect(
                0,
                0,
                centerPoint.width + xZoom,
                centerPoint.height + yZoom
        )

        var zoomEnable = false

        val gestureDetector = GestureDetector(
                textureView.context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent?): Boolean {
                        zoomEnable = !zoomEnable
                        preview.zoom(
                                (if (zoomEnable) zoom else originalZoom).apply {
                                    Logg.d { "zoom rect: $this" }
                                }

                        )
                        return true
                    }
                })

        textureView.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            true
        }


    }

    private fun getMaximumSize(): Size? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList[0] ?: return null
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        var maximumSize = Size(0, 0)

        val constraint = 1300

        if (map?.getOutputSizes(SurfaceTexture::class.java)?.isNotEmpty() == true) {
            for (size in map.getOutputSizes(SurfaceTexture::class.java)) {
                Logg.d { "supported size: $size" }
                if ((constraint > size.height && constraint > size.width) && (maximumSize.height + maximumSize.width) < (size.height + size.width)) {
                    maximumSize = size
                }
            }
            Logg.d { "maximum size: $maximumSize" }
            return maximumSize
        }

        return null
    }

    inner class LuminosityAnalyzer : ImageAnalysis.Analyzer {
        private var lastAnalyzedTimestamp = 0L

        /**
         * Helper extension function used to extract a byte array from an
         * image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastAnalyzedTimestamp >=
                    TimeUnit.MILLISECONDS.toMillis(200)
            ) {

                viewLifecycleOwner?.apply {
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        return
                    }

                    val buffer = image.planes[0].buffer

                    val metadata = FirebaseVisionImageMetadata.Builder()
                            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                            .setHeight(image.height)
                            .setWidth(image.width)
                            .setRotation(getRotation(rotationDegrees))
                            .build()

                    scanBarcodes(FirebaseVisionImage.fromByteBuffer(buffer, metadata))


                    lastAnalyzedTimestamp = currentTimestamp
                }

            }
        }
    }

    private fun getRotation(rotationCompensation: Int): Int {
        return when (rotationCompensation) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> {
                FirebaseVisionImageMetadata.ROTATION_0
            }
        }
    }

    private fun scanBarcodes(image: FirebaseVisionImage) {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_QR_CODE
                )
                .build()

        val detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options)
        Logg.d { "image size: ${image.bitmap.height}/${image.bitmap.width}" }
        detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->

                    Logg.d { "barcodes: ${barcodes.map { it.rawValue }}" }

                    canvasForScanDetection?.let { canvas ->
                        canvas.cleanAllRects()
                        barcodes.forEach {
                            canvas.addRectInfo(
                                    it!!.boundingBox!!.transformWithRatio(ratio),
                                    checkStatus = it.rawValue?.let { rawValue ->
                                        checkStatusFunction(rawValue)
                                    }
                            )
                        }

                        canvas.invalidate()
                    }

                }
                .addOnFailureListener {
                    Logg.d { "barcodes failure: $it" }
                }
    }

}

private fun Rect.transformWithRatio(ratio: Float): Rect {
    return Rect(
            (this.left * ratio).toInt(),
            (this.top * ratio).toInt(),
            (this.right * ratio).toInt(),
            (this.bottom * ratio).toInt()
    )
}
