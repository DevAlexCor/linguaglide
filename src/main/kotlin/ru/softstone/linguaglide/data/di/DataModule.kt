package ru.softstone.linguaglide.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.softstone.linguaglide.data.*
import ru.softstone.linguaglide.data.agent.EnglishTeacherAgent
import ru.softstone.linguaglide.data.agent.TextFormatterAgent
import ru.softstone.linguaglide.domain.OpenAIProvider
import ru.softstone.linguaglide.domain.repository.SettingsRepository
import ru.softstone.linguaglide.domain.repository.SpeechRepository
import ru.softstone.linguaglide.domain.repository.TextRepository
import ru.softstone.linguaglide.domain.repository.TextToSpeech
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.pathString

val dataModule = module {
    single<DataStore<Preferences>> {
        createDataStore { "user.preferences_pb" }
    }
    singleOf(::OpenAIProviderImpl) {
        bind<OpenAIProvider>()
    }
    singleOf(::TextRepositoryImpl) {
        bind<TextRepository>()
    }
    singleOf(::TextToSpeechImpl) {
        bind<TextToSpeech>()
    }
    singleOf(::SpeechRepositoryImpl) {
        bind<SpeechRepository>()
    }
    singleOf(::SettingsRepositoryImpl) {
        bind<SettingsRepository>()
    }
    singleOf(::TextFormatterAgent)
    singleOf(::EnglishTeacherAgent)
}

private fun createDataStore(producePath: () -> String): DataStore<Preferences>{
    val userHome = System.getProperty("user.home")
    val appDirectory = Paths.get(userHome, ".linguaglide")
    if (!Files.exists(appDirectory)) {
        Files.createDirectories(appDirectory)
    }
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { appDirectory.resolve(producePath()).pathString.toPath() }
    )
}