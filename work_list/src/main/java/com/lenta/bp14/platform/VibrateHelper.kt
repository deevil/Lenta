package com.lenta.bp14.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import javax.inject.Inject

class VibrateHelper @Inject constructor(private val context: Context) : IVibrateHelper {

    private val oneShortTime = 200L


    override fun shortVibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(oneShortTime, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(oneShortTime)
        }
    }


}

interface IVibrateHelper {
    fun shortVibrate()
}