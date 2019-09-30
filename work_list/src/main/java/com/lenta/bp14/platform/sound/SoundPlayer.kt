package com.lenta.bp14.platform.sound

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.lenta.bp14.R

interface ISoundPlayer {
    fun start()
    fun stop()
    fun playBeep()
    fun playError()
}

class SoundPlayer(private val context: Context) : ISoundPlayer {

    val succesId = R.raw.beep
    val wrongId = R.raw.wrong

    val rawsIds = listOf(succesId, wrongId)

    var mediaPlayers: MutableMap<Int, MediaPlayer> = mutableMapOf()

    override fun start() {
        for (id in rawsIds) {
            mediaPlayers[id].let {
                if (it == null) {
                    mediaPlayers[id] = MediaPlayer.create(context, id)
                }
            }
        }
    }

    override fun stop() {
        mediaPlayers.forEach {
            it.value.release()
        }
        mediaPlayers.clear()
    }

    override fun playBeep() {
        play(succesId)
    }

    override fun playError() {
        play(wrongId)
    }

    private fun play(rawId: Int) {
        mediaPlayers[rawId]?.let {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            val userVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)

            am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0)

            it.setOnCompletionListener {
                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        userVolume,
                        0)
            }

            it.start()
        }
    }

}