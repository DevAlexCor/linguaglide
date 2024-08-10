package ru.softstone.linguaglide.data.agent

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.softstone.linguaglide.domain.OpenAIProvider

class OpenAICompletionAgent(
    private val modelId: String = "gpt-4o",
    private val openAIProvider: OpenAIProvider,
) : CompletionAgent {
    override suspend fun complete(
        prompt: String,
        message: String,
        temperature: Double
    ): Flow<String> {
        val chatMessages = listOf(
            ChatMessage(
                role = ChatRole.System,
                content = prompt
            ),
            ChatMessage(
                role = ChatRole.User,
                content = message
            )
        )
        val request = chatCompletionRequest {
            model = ModelId(modelId)
            messages = chatMessages
            this.temperature = temperature
        }
        val stringBuffer = StringBuffer()
        return openAIProvider.getClient().chatCompletions(request).map { response ->
            stringBuffer.append(response.choices.first().delta.content ?: "")
            stringBuffer.toString()
        }
    }
}