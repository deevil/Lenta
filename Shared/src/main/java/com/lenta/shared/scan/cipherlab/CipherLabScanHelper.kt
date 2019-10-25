package com.lenta.shared.scan.cipherlab

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.utilities.Logg

class CipherLabScanHelper : IScanHelper {

    companion object {
        private const val intentFilterAction = "com.lenta.cipherscan"
        private const val barcodeDataKey = "EXTRA_CIPHER_DATA"
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
            Logg.d { "cipherlab action: $action" }


            if (action == intentFilterAction) {
                val decodedData = intent.getStringExtra(barcodeDataKey)
                Logg.d { "decodedData: $decodedData" }
                if (decodedData != null) {
                    scanResult.postValue(decodedData)
                }
            }
        }
    }


}