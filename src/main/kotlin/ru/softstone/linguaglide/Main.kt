package ru.softstone.linguaglide

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.KoinContext
import org.koin.core.context.GlobalContext.startKoin
import ru.softstone.linguaglide.data.di.dataModule
import ru.softstone.linguaglide.presentation.di.presentationModule
import ru.softstone.linguaglide.presentation.feature.dictation.DictationScreen
import ru.softstone.linguaglide.presentation.feature.main.MainScreen
import ru.softstone.linguaglide.presentation.feature.prepare.PrepareTextScreen
import ru.softstone.linguaglide.presentation.feature.settings.SettingsScreen

fun main() = application {
    startKoin {
        modules(
            listOf(
                presentationModule,
                dataModule
            )
        )
    }
    Window(
        title = "LinguaGlide",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
        onCloseRequest = ::exitApplication
    ) {
        KoinContext {
            App()
        }
    }
}

@Composable
fun App() {
    MaterialTheme(
        colors = darkColors().copy(
            primary = Color(0xFFEE9102),
            secondary = Color(0xFFD6EE02),
        )
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = NavDestination.DICTATION,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(route = NavDestination.MAIN) {
                MainScreen(
                    onNavigateToPrepareText = {
                        navController.navigate(NavDestination.PREPARE_TEXT)
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavDestination.SETTINGS)
                    }
                )
            }

            composable(route = NavDestination.DICTATION) {
                DictationScreen(
                    onNavigateToPrepareText = {
                        navController.navigate(NavDestination.PREPARE_TEXT)
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavDestination.SETTINGS)
                    }
                )
            }

            composable(route = NavDestination.PREPARE_TEXT) {
                PrepareTextScreen(
                    onNavigateNext = {
                        navController.navigate(NavDestination.MAIN)
                    }
                )
            }

            composable(route = NavDestination.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

    }
}

private object NavDestination {
    const val MAIN = "main"
    const val PREPARE_TEXT = "prepare_text"
    const val SETTINGS = "settings"
    const val DICTATION = "dictation"
}