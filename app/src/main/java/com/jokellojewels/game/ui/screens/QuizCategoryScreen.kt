package com.jokellojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jokellojewels.game.R
import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.QuizContent
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.GameBackground
import com.jokellojewels.game.ui.components.GameTopBar
import com.jokellojewels.game.ui.components.JewelCard
import com.jokellojewels.game.ui.components.PlateButton
import com.jokellojewels.game.ui.components.TextGlow
import com.jokellojewels.game.ui.theme.JJColors
import com.jokellojewels.game.viewmodel.ProgressViewModel

/** Jewel shown for each difficulty: emerald = easy, amethyst = medium, ruby = hard. */
private val difficultyJewels = listOf(Assets.ITEM_EMERALD, Assets.ITEM_AMETHYST, Assets.ITEM_RUBY)

@Composable
fun QuizCategoryScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit,
    onCategory: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GameBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            GameTopBar(title = stringResource(R.string.quiz_categories_title), onBack = onBack)
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                Image(
                    painter = painterResource(Assets.MASCOT_A),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(0.36f),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd,
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.quiz_theme),
                        style = MaterialTheme.typography.titleMedium.copy(shadow = TextGlow),
                        color = JJColors.Cream,
                    )
                    QuizContent.categories.forEachIndexed { i, category ->
                        val best = state.quizBestFor(category.id)
                        val bestText = if (best == null) {
                            stringResource(R.string.quiz_not_played)
                        } else {
                            stringResource(R.string.quiz_best, best, GameLogic.QUIZ_QUESTIONS_PER_CATEGORY)
                        }
                        JewelCard(contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(Assets.item(difficultyJewels[i % difficultyJewels.size])),
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp).padding(end = 10.dp),
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = category.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JJColors.Lavender,
                                    )
                                    Text(
                                        text = bestText,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = JJColors.Gold,
                                    )
                                }
                                // ★ count shows the difficulty without relying on color.
                                Text(
                                    text = "★".repeat(i + 1),
                                    color = JJColors.Gold,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }
                            PlateButton(
                                text = category.title,
                                onClick = { onCategory(category.id) },
                                modifier = Modifier.fillMaxWidth(),
                                semanticsLabel = "${category.title}. ${category.subtitle}. $bestText",
                            )
                        }
                    }
                    Box(Modifier.height(this@BoxWithConstraints.maxHeight * 0.25f))
                }
            }
        }
    }
}
