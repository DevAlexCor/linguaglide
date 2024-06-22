package ru.softstone.linguaglide.data.agent

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.softstone.linguaglide.domain.OpenAIProvider
import ru.softstone.linguaglide.domain.repository.SettingsRepository
import ru.softstone.linguaglide.normalizeWhitespace

// TODO: Make agents generic
class EnglishTeacherAgent(
    private val openAIProvider: OpenAIProvider,
    private val settingsRepository: SettingsRepository,
) {
    companion object {
        val DEFAULT_PROMPT = """
                You are an English teacher. You have a student who is learning English as a second language.
                The student is struggling with the following text. The student has asked you to explain the text to them.
                Your task is to explain the text to the student in a way that is easy to understand.
                The part that needs to be explained is marked with the following tag <explain></explain>.
                
                Your response example: 
                "Palpable" means something you can almost physically feel because it's so intense or noticeable. In this case, it means that the sense of Friday joy is very strong and obvious.
                
                [no prose]
            """.trimIndent()
    }

    private val modelId = ModelId("gpt-4o")

    suspend fun explain(text: String): Flow<String> {
        val chatMessages = listOf(
            ChatMessage(
                role = ChatRole.System,
                content = settingsRepository.getEnglishTeacherPrompt()
            ),
            ChatMessage(role = ChatRole.User, content = text.normalizeWhitespace())
        )
        val request = chatCompletionRequest {
            model = modelId
            messages = chatMessages
            temperature = 1.0
        }
        val stringBuffer = StringBuffer()
        return openAIProvider.getClient().chatCompletions(request).map { response ->
            stringBuffer.append(response.choices.first().delta.content ?: "")
            stringBuffer.toString()
        }
    }
}