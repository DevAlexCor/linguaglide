package ru.softstone.linguaglide.presentation.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.softstone.linguaglide.presentation.AudioPlayer
import ru.softstone.linguaglide.presentation.feature.main.MainViewModel
import ru.softstone.linguaglide.presentation.feature.prepare.PrepareTextViewModel
import ru.softstone.linguaglide.presentation.feature.settings.SettingsViewModel

val presentationModule = module {
    factoryOf(::MainViewModel)
    factoryOf(::PrepareTextViewModel)
    factoryOf(::SettingsViewModel)
    factoryOf(::AudioPlayer)
}