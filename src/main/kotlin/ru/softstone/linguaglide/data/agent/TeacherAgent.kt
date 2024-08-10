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
class TeacherAgent(
    private val openAIProvider: OpenAIProvider,
    private val settingsRepository: SettingsRepository,
) {
    companion object {
        val DEFAULT_PROMPT = """
                Your task is to explain a Dutch sentence for a learner. 
                Explain each word and words combinations and also grammar.
                Translate it first and then explain in details.
                [no prose]
                
                Your response example for "Heeft iemand Tibbe gezien?": 
                Translation: "Has anyone seen Tibbe?"

                Now let's break down the sentence:

                1. **Heeft**: This is the third person singular form of the verb "hebben," which means "to have." In Dutch, "heeft" translates to "has" when referring to the third person. It is used here as an auxiliary verb in forming a question in the present perfect tense.

                2. **iemand**: This word means "someone" or "anyone." It is used to refer to an unspecified person. In this context, it indicates that the speaker is asking about the presence or knowledge of Tibbe, without specifying who they are asking.

                3. **Tibbe**: This is a proper noun, likely a name, and refers to a specific person. In this sentence, Tibbe is the subject of the inquiry. Proper nouns in Dutch, as in English, do not have grammatical gender and do not change form.

                4. **gezien**: This is the past participle of the verb "zien," which means "to see." In this context, it translates to "seen." The use of "gezien" indicates that the sentence is about whether someone has visually encountered Tibbe in the past.

                **Grammar Breakdown:**
                - The structure of the sentence follows the pattern of asking a question in the present perfect tense. In Dutch, this tense is formed by using a form of "hebben" (to have) plus the past participle of the main verb (in this case, "zien" – "gezien").
                - In this case, the subject of the inquiry (Tibbe) is placed after the verb and the object (iemand) comes before it, which is a common structure in Dutch questions.

                Overall, the sentence is a question asking if anyone has previously seen a person named Tibbe. The use of "iemand" makes it generic, indicating that it could be directed to any number of people who might know Tibbe.
            """.trimIndent()
    }

    private val modelId = ModelId("gpt-4o-mini")

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