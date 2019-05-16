package com.lenta.shared.scan.mobilbase

import android.app.Activity
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import app.dsic.barcodetray.IBarcodeInterface
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.utilities.Logg

class MobilBaseScanHelper : IScanHelper {

    /*Barcode AIDL Interface*/
    private var mBarcode: IBarcodeInterface? = null

    /*Barcode AIDL Connection Event Handler*/
    private val SERVICE_CONNECTED = 0
    private val SERVICE_DISCONNECTED = 1

    private val mServiceConnectionHandler = ServiceConnectionHandler()

    private val srvConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mBarcode = IBarcodeInterface.Stub.asInterface(service)
            mServiceConnectionHandler.sendEmptyMessage(SERVICE_CONNECTED)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mServiceConnectionHandler.sendEmptyMessage(SERVICE_DISCONNECTED)
            mBarcode = null
        }
    }

    override val scanResult: MutableLiveData<String> = MutableLiveData()

    private val mBarcodeReadBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "app.dsic.barcodetray.BARCODE_BR_DECODING_DATA") {
                val symbology_ident = BarcodeDeclaration.SYMBOLOGY_IDENT.fromInteger(
                        intent.getIntExtra("EXTRA_BARCODE_DECODED_SYMBOLE", -1))
                if (symbology_ident !== BarcodeDeclaration.SYMBOLOGY_IDENT.NOT_READ) {
                    val data = intent.getStringExtra("EXTRA_BARCODE_DECODED_DATA")
                    val type = symbology_ident.toString()
                    scanResult.value = data
                    scanResult.value = null

                } else {
                    Logg.d { "BarCode NOT READ" }
                }


            }
        }
    }


    override fun startListen(activity: Activity) {
        /*AIDL Service connect*/
        val intent = Intent("app.dsic.barcodetray.IBarcodeInterface")
        intent.`package` = activity.packageName
        activity.bindService(intent,
                srvConn, BIND_AUTO_CREATE)
        /*Set Broadcast receiver*/
        activity.registerReceiver(mBarcodeReadBroadCast,
                IntentFilter("app.dsic.barcodetray.BARCODE_BR_DECODING_DATA"))
    }

    override fun stopListen(activity: Activity) {
        /*unbind Broadcast receiver*/
        activity.unregisterReceiver(mBarcodeReadBroadCast)
        /*AIDL Service disconnect*/
        activity.unbindService(srvConn)
    }


    internal inner class ServiceConnectionHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                SERVICE_CONNECTED -> connect()
                SERVICE_DISCONNECTED -> disconnect()
            }
        }

        private fun connect() {
            try {
                /*Set Receive type to Intent event*/
                mBarcode?.SetRecvType(BarcodeDeclaration.RECEIVE_TYPE.INTENT_EVENT.ordinal)
                //setNotificationItems();
                //setDecodingCharSet();
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        private fun disconnect() {}
    }


}