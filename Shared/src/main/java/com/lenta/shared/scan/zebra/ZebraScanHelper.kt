package com.lenta.shared.scan.zebra

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.utilities.Logg

class ZebraScanHelper : IScanHelper {

    companion object {
        private const val intentFilterAction = "com.lenta"
        private const val datawedgeIntentKeySource = "com.symbol.datawedge.source"
        private const val datawedgeIntentKeyLabelType = "com.symbol.datawedge.label_type"
        private const val datawedgeIntentKeyData = "com.symbol.datawedge.data_string"
        private const val datawedgeIntentKeySourceLegacy = "com.motorolasolutions.emdk.datawedge.source"
        private const val datawedgeIntentKeyLabelTypeLegacy = "com.motorolasolutions.emdk.datawedge.label_type"
        private const val datawedgeIntentKeyDataLegacy = "com.motorolasolutions.emdk.datawedge.data_string"
    }


    override val scanResult: MutableLiveData<String> = MutableLiveData()

    override fun startListen(activity: Activity) {
        val intentFilter = IntentFilter().apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addAction("com.lenta")
        }
        activity.registerReceiver(mBarcodeReadBroadCast, intentFilter)
    }

    override fun stopListen(activity: Activity) {
        activity.unregisterReceiver(mBarcodeReadBroadCast)
    }

    private val mBarcodeReadBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == intentFilterAction) {
                val decodedSource: String? = intent.getStringExtra(datawedgeIntentKeySource)
                        ?: intent.getStringExtra(datawedgeIntentKeySourceLegacy)
                val decodedData = intent.getStringExtra(datawedgeIntentKeyData)
                        ?: intent.getStringExtra(datawedgeIntentKeyDataLegacy)
                val decodedLabelType = intent.getStringExtra(datawedgeIntentKeyLabelType)
                        ?: intent.getStringExtra(datawedgeIntentKeyLabelTypeLegacy)
                Logg.d { "Decode source: $decodedSource" }
                Logg.d { "decodedLabelType: $decodedLabelType" }

                if (decodedData != null) {
                    scanResult.postValue(decodedData)
                }
            }
        }
    }


}