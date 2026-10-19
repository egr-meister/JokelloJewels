package com.jokellojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jokellojewels.game.R
import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.LevelConfig
import com.jokellojewels.game.logic.QuizContent
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.GameBackground
import com.jokellojewels.game.ui.components.JewelCard
import com.jokellojewels.game.ui.components.ModalPanel
import com.jokellojewels.game.ui.components.PlateButton
import com.jokellojewels.game.ui.components.ScreenScaffold
import com.jokellojewels.game.ui.components.StatLine
import com.jokellojewels.game.ui.theme.JJColors
import com.jokellojewels.game.viewmodel.ProgressViewModel

/** Matching Pairs result: "Well Done" or "Out of Time", drawn as a Compose modal panel. */
@Composable
fun PairsResultScreen(
    viewModel: ProgressViewModel,
    config: LevelConfig,
    won: Boolean,
    matched: Int,
    hasNextLevel: Boolean,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val best = state.bestMatched(config.level).coerceAtLeast(matched)
    GameBackground {
        Box(Modifier.fillMaxSize()) {
            ModalPanel(
                title = stringResource(if (won) R.string.pairs_win else R.string.pairs_lose),
                badge = if (won) Assets.item(Assets.ITEM_CHEST) else Assets.DECO_HOURGLASS,
            ) {
                Image(
                    painter = painterResource(if (won) Assets.MASCOT_C else Assets.MASCOT_B),
                    contentDescription = stringResource(if (won) R.string.cd_mascot_c else R.string.cd_mascot_b),
                    modifier = Modifier.size(140.dp),
                )
                Text(
                    text = stringResource(R.string.level_n, config.level),
                    style = MaterialTheme.typography.titleMedium,
                    color = JJColors.Lavender,
                )
                StatLine(
                    label = stringResource(R.string.pairs_result_matched, matched, config.pairs),
                    value = if (won) "✓" else "✗",
                    highlight = won,
                    icon = Assets.item(Assets.ITEM_EMERALD),
                )
                StatLine(
                    label = stringResource(R.string.best_score),
                    value = stringResource(R.string.results_level_value, best, config.pairs),
                    icon = Assets.item(Assets.ITEM_STAR),
                )
                if (won && !hasNextLevel) {
                    Text(
                        text = stringResource(R.string.pairs_all_cleared),
                        style = MaterialTheme.typography.bodyLarge,
                        color = JJColors.Gold,
                        textAlign = TextAlign.Center,
                    )
                }
                PlateButton(
                    text = stringResource(if (won && hasNextLevel) R.string.next_level else R.string.continue_label),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                )
                PlateButton(stringResource(R.string.restart), onRestart, Modifier.fillMaxWidth())
                PlateButton(stringResource(R.string.menu), onMenu, Modifier.fillMaxWidth())
            }
        }
    }
}

/** Results summary: best per quiz category + pairs cleared / 9 with per-level bests. */
@Composable
fun ResultsScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val total = GameLogic.QUIZ_QUESTIONS_PER_CATEGORY
    ScreenScaffold(title = stringResource(R.string.results_title), onBack = onBack) {
        Image(
            painter = painterResource(Assets.item(Assets.ITEM_GOBLET)),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )
        JewelCard {
            Text(
                text = stringResource(R.string.results_quiz_header),
                style = MaterialTheme.typography.titleLarge,
                color = JJColors.Gold,
                modifier = Modifier.semantics { heading() },
            )
            QuizContent.categories.forEachIndexed { i, category ->
                val best = state.quizBestFor(category.id)
                StatLine(
                    icon = Assets.item(listOf(Assets.ITEM_EMERALD, Assets.ITEM_AMETHYST, Assets.ITEM_RUBY)[i % 3]),
                    label = category.title,
                    value = if (best == null) "—" else stringResource(R.string.results_level_value, best, total),
                    highlight = best == total,
                )
            }
        }
        JewelCard {
            Image(
                painter = painterResource(Assets.DECO_MASK),
                contentDescription = null,
                modifier = Modifier.size(width = 90.dp, height = 54.dp),
            )
            Text(
                text = stringResource(R.string.results_pairs_header),
                style = MaterialTheme.typography.titleLarge,
                color = JJColors.Gold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.results_pairs_cleared, state.completedLevels, GameLogic.levelCount),
                style = MaterialTheme.typography.titleMedium,
                color = JJColors.Emerald,
            )
            HorizontalDivider(color = JJColors.Velvet)
            GameLogic.levels.forEach { config ->
                val cleared = state.isLevelCleared(config.level)
                StatLine(
                    icon = Assets.item(config.level - 1),
                    label = stringResource(R.string.results_level_line, config.level) + if (cleared) "  ★" else "",
                    value = stringResource(R.string.results_level_value, state.bestMatched(config.level), config.pairs),
                    highlight = cleared,
                )
            }
        }
    }
}

@Composable
fun RulesScreen(onBack: () -> Unit) {
    // Rules are shown inside a Compose-drawn panel over the themed background.
    GameBackground {
        ModalPanel(title = stringResource(R.string.rules), badge = Assets.DECO_BOOK) {
            Image(
                painter = painterResource(Assets.MASCOT_A),
                contentDescription = stringResource(R.string.cd_mascot_a),
                modifier = Modifier.size(120.dp),
            )
            RulesContent()
            PlateButton(stringResource(R.string.back), onBack, Modifier.fillMaxWidth())
        }
    }
}
