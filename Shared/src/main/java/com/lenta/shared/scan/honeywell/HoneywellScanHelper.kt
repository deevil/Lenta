package com.lenta.shared.scan.honeywell

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.honeywell.aidc.*
import com.lenta.shared.scan.IScanHelper
import java.util.HashMap

class HoneywellScanHelper : IScanHelper {

    private var barcodeReader: BarcodeReader? = null

    private var manager: AidcManager? = null

    fun init(activity: Activity) {
        AidcManager.create(activity, AidcManager.CreatedCallback { aidcManager ->
            manager = aidcManager
            manager?.let {
                try {
                    barcodeReader = it.createBarcodeReader()
                    onBarcodeReady(activity)
                } catch (e: InvalidScannerNameException) {
                    Toast.makeText(activity, "Invalid Scanner Name Exception: " + e.message, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(activity, "Exception: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

    private fun onBarcodeReady(activity: Activity) {
        activity.runOnUiThread {

            barcodeReader?.let {

                // register bar code event listener
                it.addBarcodeListener(barcodeListener)

                // set the trigger mode to client control
                try {
                    it.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                            BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL)
                } catch (e: UnsupportedPropertyException) {
                    Toast.makeText(activity, "Failed to apply properties", Toast.LENGTH_SHORT).show()
                }

                // register trigger state change listener
                it.addTriggerListener(barcodeTriggerListener)

                val properties = HashMap<String, Any>()
                // Set Symbologies On/Off
                properties[BarcodeReader.PROPERTY_CODE_128_ENABLED] = true
                properties[BarcodeReader.PROPERTY_GS1_128_ENABLED] = true
                properties[BarcodeReader.PROPERTY_QR_CODE_ENABLED] = true
                properties[BarcodeReader.PROPERTY_CODE_39_ENABLED] = true
                properties[BarcodeReader.PROPERTY_DATAMATRIX_ENABLED] = true
                properties[BarcodeReader.PROPERTY_UPC_A_ENABLE] = true
                properties[BarcodeReader.PROPERTY_EAN_13_ENABLED] = true
                properties[BarcodeReader.PROPERTY_AZTEC_ENABLED] = true
                properties[BarcodeReader.PROPERTY_CODABAR_ENABLED] = false
                properties[BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED] = false
                properties[BarcodeReader.PROPERTY_PDF_417_ENABLED] = true
                // Set Max Code 39 barcode length
                properties[BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH] = 10
                // Turn on center decoding
                properties[BarcodeReader.PROPERTY_CENTER_DECODE] = true
                // Disable bad read response, handle in onFailureEvent
                properties[BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED] = false
                // Apply the settings
                it.setProperties(properties)

            }

        }
    }


    val barcodeListener: BarcodeReader.BarcodeListener = object : BarcodeReader.BarcodeListener {
        override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        }

        override fun onBarcodeEvent(event: BarcodeReadEvent?) {
            event?.let {
                scanResult.postValue(it.barcodeData)
            }
        }
    }


    val barcodeTriggerListener: BarcodeReader.TriggerListener = object : BarcodeReader.TriggerListener {
        override fun onTriggerEvent(event: TriggerStateChangeEvent?) {
            barcodeReader?.let {
                try {
                    // only handle trigger presses
                    // turn on/off aimer, illumination and decoding
                    it.aim(event!!.state)
                    it.light(event.state)
                    it.decode(event.state)

                } catch (e: ScannerNotClaimedException) {
                    e.printStackTrace()
                } catch (e: ScannerUnavailableException) {
                    e.printStackTrace()
                }
            }

        }
    }


    override val scanResult: MutableLiveData<String> = MutableLiveData()

    override fun startListen(activity: Activity) {

        barcodeReader?.let {
            try {
                it.claim()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
            }
        }

    }

    override fun stopListen(activity: Activity) {
        barcodeReader?.release()
    }


}