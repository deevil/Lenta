package com.lenta.shared.scan.atol

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.utilities.Logg

class AtolScanHelper : IScanHelper {

    companion object {
        private val intentFilterAction = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST"
        private val barcodeDataKey = "EXTRA_BARCODE_DECODING_DATA"
        private val symbologyTypeKey = "EXTRA_BARCODE_DECODING_SYMBOLE"
    }


    override val scanResult: MutableLiveData<String> = MutableLiveData()

    override fun startListen(activity: Activity) {
        val intentFilter = IntentFilter().apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addAction(intentFilterAction)
        }
        activity.registerReceiver(mBarcodeReadBroadCast, intentFilter)
    }

    override fun stopListen(activity: Activity) {
        activity.unregisterReceiver(mBarcodeReadBroadCast)
    }

    private val mBarcodeReadBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Logg.d { "atol action: $action" }


            if (action == intentFilterAction) {

                val decodedData = intent.getStringExtra(barcodeDataKey)
                val symbologyType = intent.getStringExtra(symbologyTypeKey)

                Logg.d { "decodedData: $decodedData" }
                Logg.d { "symbologyType: $symbologyType" }

                if (decodedData != null) {
                    scanResult.postValue(decodedData)
                }
            }
        }
    }


}