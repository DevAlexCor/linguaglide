package ru.softstone.linguaglide.data.agent

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.softstone.linguaglide.domain.OpenAIProvider
import ru.softstone.linguaglide.domain.repository.SettingsRepository
import ru.softstone.linguaglide.normalizeNewLines
import ru.softstone.linguaglide.normalizeWhitespace
import ru.softstone.linguaglide.removeNonQwertyChars
import ru.softstone.linguaglide.replaceNonKeyboardChars

// TODO: Make agents generic
class TextFormatterAgent(
    private val openAIProvider: OpenAIProvider,
    private val settingsRepository: SettingsRepository,
) {
    companion object {
        val DEFAULT_PROMPT = """
            Split the following text into segments for dictation exercise.
            Keep the segments short, 5-10 words.
            Put each segment on a new line.
            One word segments are not allowed.

            [no prose]
            [no blank lines]
            [output only the text
            """.trimIndent()
    }

    private val modelId = ModelId("gpt-4o-mini")

    suspend fun prepareTextForTyping(text: String): Flow<String> {
        val chatMessages = listOf(
            ChatMessage(
                role = ChatRole.System,
                content = settingsRepository.getTextFormatterPrompt()
            ),
            ChatMessage(role = ChatRole.User, content = text.normalizeWhitespace())
        )
        val request = chatCompletionRequest {
            model = modelId
            messages = chatMessages
            temperature = 0.0
        }
        val stringBuffer = StringBuffer()
        return openAIProvider.getClient().chatCompletions(request).map { response ->
            val diff = response.choices.first().delta.content
                ?.replaceNonKeyboardChars()
                ?.removeNonQwertyChars()
                ?: ""
            stringBuffer.append(diff)
            stringBuffer.toString().normalizeNewLines()
        }
    }
}