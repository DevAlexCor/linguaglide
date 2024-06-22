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
                Split the following text into segments. 
                Adjust for Readability and Natural Flow. 
                Keep the segments short.
                Put each segment on a new line.
                Example:
                User's input:
                 ```
                When people ask me what I do—taxi drivers, hairdressers—I tell them I
                work in an office. In almost nine years, no one’s ever asked what kind of
                office, or what sort of job I do there. I can’t decide whether that’s because I
                fit perfectly with their idea of what an office worker looks like, or whether
                people hear the phrase work in an office and automatically fill in the blanks
                themselves—lady doing photocopying, man tapping at a keyboard. I’m not
                complaining. I’m delighted that I don’t have to get into the fascinating
                intricacies of accounts receivable with them. When I first started working
                here, whenever anyone asked, I told them that I worked for a graphic design
                company, but then they assumed I was a creative type. It became a bit boring.
                “A couple of weeks,” I told him.
                ```
                Output:
                ```
                When people ask me what I do
                - taxi drivers, hairdressers -
                I tell them I work in an office.
                In almost nine years, no one's ever asked what kind of office,
                or what sort of job I do there.
                I can't decide whether that's because I fit perfectly with their idea of what an office worker looks like,
                or whether people hear the phrase work in an office
                and automatically fill in the blanks themselves
                - lady doing photocopying,
                man tapping at a keyboard.
                I'm not complaining.
                I'm delighted that I don't have to get into the fascinating intricacies of accounts receivable with them.
                When I first started working here,
                whenever anyone asked,
                I told them that I worked for a graphic design company,
                but then they assumed I was a creative type.
                It became a bit boring.
                "A couple of weeks," I told him.
                ```

                [no prose]
                [no blank lines]
                [output only the text]
            """.trimIndent()
    }

    private val modelId = ModelId("gpt-4o")

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