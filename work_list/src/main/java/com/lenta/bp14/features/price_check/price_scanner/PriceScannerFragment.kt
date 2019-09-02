package com.lenta.bp14.features.price_check.price_scanner

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.lifecycle.Lifecycle
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentPriceScannerBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class PriceScannerFragment : CoreFragment<FragmentPriceScannerBinding, PriceScannerViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_price_scanner

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("127")
    }

    override fun getViewModel(): PriceScannerViewModel {
        provideViewModel(PriceScannerViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.scan_price_description)


    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
    }


    private fun startCamera() {
        val manager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList[0]
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        for (size in map.getOutputSizes(SurfaceTexture::class.java)) {
            Log.i("InfoAnalyzer", "imageDimension " + size)
        }

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetResolution(Size(720, 960))
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        val textureView = binding!!.textureView


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
            setTargetResolution(Size(720, 960))
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

        // дизэйблим scrollview
        binding!!.scrollView.setOnTouchListener { _, _ -> true }


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

                Log.d("CameraXApp", "lifecycle.currentState: ${lifecycle.currentState}")

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


        /*var bitmap = textureView.bitmap
        bitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.getWidth(),
            bitmap.getHeight(),
            textureView.getTransform(null),
            true
        )


        detector.detectInImage(FirebaseVisionImage.fromBitmap(bitmap))*/
        //detector.detectInImage(FirebaseVisionImage.fromBitmap(textureView.bitmap))
        detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->

                    Log.d("CameraXApp", "barcodes: ${barcodes.map { it.rawValue }}")

                    binding!!.myCanvas.let { canvas ->
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
