package com.lenta.bp14.features.price_check.price_scanner

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
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

    var viewLifecycleOwner: LifecycleOwner? = null
    var canvasForScanDetection: CanvasForScanDetection? = null

    fun onViewCreated(viewLifecycleOwner: LifecycleOwner, canvasForScanDetection: CanvasForScanDetection) {
        this.viewLifecycleOwner = viewLifecycleOwner
        this.canvasForScanDetection = canvasForScanDetection
    }

    fun onDestroyView() {
        this.viewLifecycleOwner = null
        this.canvasForScanDetection = null
    }

    fun startCamera(textureView: TextureView) {

        val targetSize = getMaximumSize()

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetResolution(targetSize)
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        val textureView = textureView


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
        //preview.zoom(Rect(500, 500, 1200, 1200))


    }

    private fun getMaximumSize(): Size {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList[0]
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        var maximumSize = Size(0, 0)

        for (size in map.getOutputSizes(SurfaceTexture::class.java)) {
            Logg.d { "supported size: $size" }
            Log.i("InfoAnalyzer", "imageDimension " + size)
            if ((maximumSize.height + maximumSize.width) < (size.height + size.width)) {
                maximumSize = size
            }
        }
        Logg.d { "maximum size: $maximumSize" }
        return maximumSize
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

                    Log.d("InfoAnalyzer", "imageProxy width is : ${image.width}")
                    Log.d("InfoAnalyzer", "imageProxy height is : ${image.height}")

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
        Log.d("CameraXApp", "image size: ${image.bitmap.height}/${image.bitmap.width}")
        detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->

                    Log.d("CameraXApp", "barcodes: ${barcodes.map { it.rawValue }}")

                    canvasForScanDetection?.let { canvas ->
                        canvas.cleanAllRects()
                        barcodes.forEach {
                            canvas.addRectInfo(
                                    it!!.boundingBox!!,
                                    error = !(it.rawValue?.contains("7") ?: false),
                                    text = it.rawValue?.takeLast(20) ?: ""
                            )
                        }

                        canvas.invalidate()
                    }

                }
                .addOnFailureListener {
                    Log.d("CameraXApp", "barcodes failure: $it")
                }
    }

}