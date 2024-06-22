package ru.softstone.linguaglide.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.softstone.linguaglide.domain.repository.SpeechRepository
import ru.softstone.linguaglide.domain.repository.TextToSpeech
import java.io.File
import java.io.FileOutputStream

class SpeechRepositoryImpl(
    private val textToSpeech: TextToSpeech
) : SpeechRepository {
    private val mutex = Mutex()

    /**
     * Get the mp3 file for the given text.
     *
     * @param text the text to convert to speech.
     * @return the mp3 file.
     */
    override suspend fun getMp3(text: String): File = withContext(Dispatchers.IO) {
        mutex.withLock {
            val fileHash = text.hashCode().toString()
            val tmpFile = File(System.getProperty("java.io.tmpdir"), "tts-$fileHash.mp3")

            if (!tmpFile.exists()) {
                val byteArray = textToSpeech.generateSpeech(text)
                FileOutputStream(tmpFile).use { fos ->
                    fos.write(byteArray)
                }
                tmpFile.deleteOnExit()
            }
            tmpFile
        }
    }
}