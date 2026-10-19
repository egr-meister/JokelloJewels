package com.jokellojewels.game.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jokellojewels.game.AppContainer
import com.jokellojewels.game.R
import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.QuizContent
import com.jokellojewels.game.ui.components.FallbackScreen
import com.jokellojewels.game.ui.screens.LevelsScreen
import com.jokellojewels.game.ui.screens.MainScreen
import com.jokellojewels.game.ui.screens.MenuScreen
import com.jokellojewels.game.ui.screens.PairsResultScreen
import com.jokellojewels.game.ui.screens.PairsScreen
import com.jokellojewels.game.ui.screens.QuizCategoryScreen
import com.jokellojewels.game.ui.screens.QuizResultScreen
import com.jokellojewels.game.ui.screens.QuizScreen
import com.jokellojewels.game.ui.screens.ResultsScreen
import com.jokellojewels.game.ui.screens.RulesScreen
import com.jokellojewels.game.ui.screens.SettingsScreen
import com.jokellojewels.game.viewmodel.PairsViewModel
import com.jokellojewels.game.viewmodel.PairsViewModelFactory
import com.jokellojewels.game.viewmodel.ProgressViewModel
import com.jokellojewels.game.viewmodel.ProgressViewModelFactory
import com.jokellojewels.game.viewmodel.QuizViewModel
import com.jokellojewels.game.viewmodel.QuizViewModelFactory

@Composable
fun AppNavHost(container: AppContainer) {
    val nav = rememberNavController()
    val progressFactory = remember(container) { ProgressViewModelFactory(container) }

    NavHost(navController = nav, startDestination = MainRoute) {

        composable<MainRoute> { entry ->
            MainScreen(onStart = { nav.go(entry, MenuRoute) })
        }

        composable<MenuRoute> { entry ->
            MenuScreen(
                onBack = { nav.back(entry) },
                onQuiz = { nav.go(entry, QuizCategoriesRoute) },
                onPairs = { nav.go(entry, LevelsRoute) },
                onResults = { nav.go(entry, ResultsRoute) },
                onRules = { nav.go(entry, RulesRoute) },
                onSettings = { nav.go(entry, SettingsRoute) },
            )
        }

        composable<QuizCategoriesRoute> { entry ->
            val vm: ProgressViewModel = viewModel(factory = progressFactory)
            QuizCategoryScreen(
                viewModel = vm,
                onBack = { nav.back(entry) },
                onCategory = { id -> nav.go(entry, QuizRoute(categoryId = id)) },
            )
        }

        composable<QuizRoute> { entry ->
            val category = QuizContent.category(entry.safeRoute<QuizRoute>()?.categoryId)
            if (category == null) {
                FallbackScreen(stringResource(R.string.fallback_message)) { nav.back(entry) }
            } else {
                val vm: QuizViewModel = viewModel(factory = QuizViewModelFactory(category.id, container))
                QuizScreen(
                    viewModel = vm,
                    onFinished = { score ->
                        nav.goFromEvent(entry, QuizResultRoute(category.id, score)) {
                            popUpTo<QuizRoute> { inclusive = true }
                        }
                    },
                    onMenu = { nav.toMenu(entry) },
                )
            }
        }

        composable<QuizResultRoute> { entry ->
            val route = entry.safeRoute<QuizResultRoute>()
            val category = QuizContent.category(route?.categoryId)
            if (route == null || category == null || !GameLogic.isValidQuizScore(route.score)) {
                FallbackScreen(stringResource(R.string.fallback_message)) { nav.back(entry) }
            } else {
                val vm: ProgressViewModel = viewModel(factory = progressFactory)
                QuizResultScreen(
                    viewModel = vm,
                    category = category,
                    score = route.score,
                    onContinue = {
                        nav.go(entry, QuizCategoriesRoute) { popUpTo<QuizCategoriesRoute> { inclusive = false } }
                    },
                    onRestart = {
                        nav.go(entry, QuizRoute(category.id)) { popUpTo<QuizResultRoute> { inclusive = true } }
                    },
                    onMenu = { nav.toMenu(entry) },
                )
            }
        }

        composable<LevelsRoute> { entry ->
            val vm: ProgressViewModel = viewModel(factory = progressFactory)
            LevelsScreen(
                viewModel = vm,
                onBack = { nav.back(entry) },
                onLevel = { level -> nav.go(entry, PairsRoute(level)) },
            )
        }

        composable<PairsRoute> { entry ->
            val config = GameLogic.levelConfig(entry.safeRoute<PairsRoute>()?.level ?: 0)
            if (config == null) {
                FallbackScreen(stringResource(R.string.fallback_message)) { nav.back(entry) }
            } else {
                val vm: PairsViewModel = viewModel(factory = PairsViewModelFactory(config.level, container))
                PairsScreen(
                    viewModel = vm,
                    onBack = { nav.back(entry) },
                    onFinished = { outcome ->
                        nav.goFromEvent(entry, PairsResultRoute(config.level, outcome.won, outcome.matchedPairs)) {
                            popUpTo<PairsRoute> { inclusive = true }
                        }
                    },
                    onMenu = { nav.toMenu(entry) },
                )
            }
        }

        composable<PairsResultRoute> { entry ->
            val route = entry.safeRoute<PairsResultRoute>()
            val config = GameLogic.levelConfig(route?.level ?: 0)
            val valid = route != null && config != null && route.matched in 0..config.pairs &&
                (!route.won || route.matched == config.pairs)
            if (!valid || route == null || config == null) {
                FallbackScreen(stringResource(R.string.fallback_message)) { nav.back(entry) }
            } else {
                val vm: ProgressViewModel = viewModel(factory = progressFactory)
                val next = if (route.won) GameLogic.nextLevel(config.level) else null
                PairsResultScreen(
                    viewModel = vm,
                    config = config,
                    won = route.won,
                    matched = route.matched,
                    hasNextLevel = next != null,
                    onContinue = {
                        if (next != null) {
                            nav.go(entry, PairsRoute(next)) { popUpTo<PairsResultRoute> { inclusive = true } }
                        } else {
                            nav.go(entry, LevelsRoute) { popUpTo<LevelsRoute> { inclusive = false } }
                        }
                    },
                    onRestart = {
                        nav.go(entry, PairsRoute(config.level)) { popUpTo<PairsResultRoute> { inclusive = true } }
                    },
                    onMenu = { nav.toMenu(entry) },
                )
            }
        }

        composable<ResultsRoute> { entry ->
            val vm: ProgressViewModel = viewModel(factory = progressFactory)
            ResultsScreen(viewModel = vm, onBack = { nav.back(entry) })
        }

        composable<SettingsRoute> { entry ->
            val vm: ProgressViewModel = viewModel(factory = progressFactory)
            SettingsScreen(viewModel = vm, onBack = { nav.back(entry) })
        }

        composable<RulesRoute> { entry ->
            RulesScreen(onBack = { nav.back(entry) })
        }
    }
}

/** Decodes typed args without ever throwing; null means "show the fallback". */
private inline fun <reified T : Any> NavBackStackEntry.safeRoute(): T? =
    try {
        toRoute<T>()
    } catch (e: Exception) {
        null
    }

private fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED

/** User-initiated navigation: ignored unless the source screen is resumed (no double-tap pushes). */
private fun <T : Any> NavController.go(
    from: NavBackStackEntry,
    route: T,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    if (!from.isResumed()) return
    navigate(route) {
        launchSingleTop = true
        builder()
    }
}

/** Event-driven navigation (game finished): allowed in any state, but only from the top entry. */
private fun <T : Any> NavController.goFromEvent(
    from: NavBackStackEntry,
    route: T,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    if (currentBackStackEntry?.id != from.id) return
    navigate(route) {
        launchSingleTop = true
        builder()
    }
}

private fun NavController.back(from: NavBackStackEntry) {
    if (!from.isResumed()) return
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        // Never pop the last entry (that would leave an empty screen): go home instead.
        navigate(MainRoute) {
            popUpTo(graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
}

private fun NavController.toMenu(from: NavBackStackEntry) {
    if (currentBackStackEntry?.id != from.id) return
    navigate(MenuRoute) {
        popUpTo<MenuRoute> { inclusive = false }
        launchSingleTop = true
    }
}
