package ru.softstone.linguaglide.data

import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.softstone.linguaglide.domain.OpenAIProvider
import ru.softstone.linguaglide.domain.error.NoTokenException
import ru.softstone.linguaglide.domain.repository.SettingsRepository

class OpenAIProviderImpl(
    private val settingsRepository: SettingsRepository
) : OpenAIProvider {
    private var lastToken: String? = null
    private var openAI: OpenAI? = null
    private val mutex = Mutex()

    override suspend fun getClient(): OpenAI = mutex.withLock {
        val token = settingsRepository.getToken()
        if (token.isNullOrBlank()) {
            throw NoTokenException()
        }
        if (token != lastToken) {
            lastToken = token
            openAI = OpenAI(token)
        }
        openAI!!
    }
}