package ru.softstone.linguaglide.data

import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.SpeechResponseFormat
import com.aallam.openai.api.audio.Voice
import com.aallam.openai.api.model.ModelId
import ru.softstone.linguaglide.domain.OpenAIProvider
import ru.softstone.linguaglide.domain.repository.TextToSpeech

class TextToSpeechImpl(
    private val openAIProvider: OpenAIProvider
) : TextToSpeech {
    override suspend fun generateSpeech(text: String): ByteArray {
        if (text.length > 1000) error("Text is too long")
        val request = SpeechRequest(
            model = ModelId("tts-1"),
            input = text,
            responseFormat = SpeechResponseFormat.Mp3,
            voice = Voice.Onyx,
        )
        return openAIProvider.getClient().speech(request)
    }
}