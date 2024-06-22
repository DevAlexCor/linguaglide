package ru.softstone.linguaglide.presentation

import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume

class AudioPlayer {
    suspend fun playMp3(file: File) = suspendCancellableCoroutine<Unit> {
        val fileInputStream = FileInputStream(file)
        val player = AdvancedPlayer(fileInputStream)

        player.playBackListener = object : PlaybackListener() {
            override fun playbackFinished(evt: PlaybackEvent?) {
                it.resume(Unit)
            }
        }
        MainScope().launch {
            withContext(Dispatchers.IO) {
                try {
                    player.play()
                } catch (e: JavaLayerException) {
                    it.cancel(e)
                }
            }
        }

        it.invokeOnCancellation { player.close() }
    }
}