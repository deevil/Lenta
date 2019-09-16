package com.lenta.bp14.platform.sound

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.lenta.bp14.R

interface ISoundPlayer {
    fun playBeep()
}

class SoundPlayer(private val context: Context) : ISoundPlayer {

    override fun playBeep() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val userVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)

        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0)

        val mp = MediaPlayer.create(context, R.raw.beep)

        mp.setOnCompletionListener { mediaPlayer ->
            mediaPlayer.release()
            am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    userVolume,
                    0)
        }

        mp.start()
    }

}