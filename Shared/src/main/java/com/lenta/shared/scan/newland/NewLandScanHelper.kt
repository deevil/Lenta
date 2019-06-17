package com.lenta.shared.scan.newland

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.scan.IScanHelper

class NewLandScanHelper : IScanHelper {

    val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val data = intent.getStringExtra("SCAN_BARCODE1")
            //val scanResult_2 = intent.getStringExtra("SCAN_BARCODE2")
            //val scanStatus = intent.getStringExtra("EXTRA_SCAN_STATE")
            if (data != null) {
                scanResult.postValue(data)
            }

        }
    }


    override val scanResult: MutableLiveData<String> = MutableLiveData()

    override fun startListen(activity: Activity) {

        val mFilter = IntentFilter("nlscan.action.SCANNER_RESULT")

        activity.registerReceiver(mReceiver, mFilter)


    }

    override fun stopListen(activity: Activity) {
        activity.unregisterReceiver(mReceiver)
    }


}