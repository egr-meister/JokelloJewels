package com.jokellojewels.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jokellojewels.game.R
import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.QuizCategory
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.GameBackground
import com.jokellojewels.game.ui.components.GameTopBar
import com.jokellojewels.game.ui.components.HudPill
import com.jokellojewels.game.ui.components.IconImageButton
import com.jokellojewels.game.ui.components.ModalPanel
import com.jokellojewels.game.ui.components.PauseOverlay
import com.jokellojewels.game.ui.components.PlateButton
import com.jokellojewels.game.ui.components.StatLine
import com.jokellojewels.game.ui.components.TextGlow
import com.jokellojewels.game.ui.components.cappedSp
import com.jokellojewels.game.ui.theme.JJColors
import com.jokellojewels.game.viewmodel.ProgressViewModel
import com.jokellojewels.game.viewmodel.QuizViewModel

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onFinished: (Int) -> Unit,
    onMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finishedScore) {
        state.finishedScore?.let(onFinished)
    }
    // App sent to background → pause, so the auto-advance never runs unseen.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.pause() }
    // Back opens the pause panel; Back on the panel continues.
    BackHandler(enabled = state.finishedScore == null) {
        if (state.paused) viewModel.resume() else viewModel.pause()
    }

    val category = state.category

    GameBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            GameTopBar(
                title = category?.title.orEmpty(),
                onBack = { viewModel.pause() },
                trailing = {
                    IconImageButton(Assets.ICON_PAUSE, stringResource(R.string.cd_pause), { viewModel.pause() })
                },
            )
            if (category != null && state.question != null) {
                QuizBody(
                    category = category,
                    index = state.index,
                    total = state.total,
                    score = state.score,
                    progress = state.progress,
                    selectedIndex = state.selectedIndex,
                    onAnswer = viewModel::answer,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }
        if (state.paused) {
            PauseOverlay(
                onContinue = viewModel::resume,
                onRestart = viewModel::restart,
                onMenu = onMenu,
            )
        }
    }
}

/**
 * Fixed layout without scrolling on normal phones: progress → question panel (panel art at its
 * own proportions) → 4 answer plates. Whatever height is left at the bottom is filled by the
 * jesters. On very short screens / huge fonts the column falls back to scrolling.
 */
@Composable
private fun QuizBody(
    category: QuizCategory,
    index: Int,
    total: Int,
    score: Int,
    progress: Float,
    selectedIndex: Int?,
    onAnswer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val question = category.questions[index]
    BoxWithConstraints(modifier) {
        // Predicted height of progress + panel + 4 plates + gaps for this width.
        val contentWidth = (maxWidth - 32.dp).coerceAtMost(500.dp)
        val needed = 34.dp + contentWidth / Assets.PANEL_QUESTION_ASPECT +
            (contentWidth / Assets.BTN_PLATE_ASPECT) * 4 + 60.dp
        val compact = maxHeight < needed + 24.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val progressLabel = stringResource(R.string.quiz_question_cd, index + 1, total)
            Row(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) { contentDescription = progressLabel },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = JJColors.Gold,
                    trackColor = JJColors.VelvetDeep,
                )
                HudPill(
                    text = stringResource(R.string.quiz_progress, index + 1, total),
                    modifier = Modifier.padding(start = 10.dp),
                )
                HudPill(
                    text = "★ $score",
                    modifier = Modifier.padding(start = 6.dp),
                    borderColor = JJColors.Emerald,
                )
            }

            // Question on the ornate panel art (kept at its own aspect ratio, never stretched).
            AnimatedContent(
                targetState = index,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "question",
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
            ) { shownIndex ->
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(Assets.PANEL_QUESTION_ASPECT),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Assets.PANEL_QUESTION),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                    Text(
                        text = category.questions[shownIndex].text,
                        style = MaterialTheme.typography.titleLarge.copy(shadow = TextGlow),
                        fontSize = cappedSp(19f, maxScale = 1.2f),
                        color = JJColors.Cream,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 22.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }

            question.options.forEachIndexed { i, option ->
                val answered = selectedIndex != null
                val isCorrect = i == question.correctIndex
                val isSelected = i == selectedIndex
                // State is shown by color AND by a ✓ / ✗ mark AND by the content description.
                val (label, overlay, cd) = when {
                    answered && isCorrect -> Triple(
                        "✓  $option", JJColors.CorrectGreen, stringResource(R.string.quiz_answer_correct_cd, option),
                    )
                    answered && isSelected -> Triple(
                        "✗  $option", JJColors.WrongRed, stringResource(R.string.quiz_answer_wrong_cd, option),
                    )
                    else -> Triple(option, null, stringResource(R.string.quiz_answer_cd, option))
                }
                PlateButton(
                    text = label,
                    onClick = { onAnswer(i) },
                    modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
                    enabled = !answered || isCorrect || isSelected,
                    overlayColor = overlay,
                    semanticsLabel = cd,
                    playClick = false, // correct / wrong SFX are played instead
                    fontSize = cappedSp(17f, maxScale = 1.2f),
                )
            }

            if (!compact) {
                // Free space under the answers: our jesters cheer the player on.
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Image(
                        painter = painterResource(Assets.MASCOT_A),
                        contentDescription = null,
                        modifier = Modifier.fillMaxHeight().weight(1f),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.BottomStart,
                    )
                    Image(
                        painter = painterResource(Assets.item(Assets.ITEM_BELL)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxHeight(0.4f).weight(0.6f),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.BottomCenter,
                    )
                    Image(
                        painter = painterResource(Assets.MASCOT_B),
                        contentDescription = null,
                        modifier = Modifier.fillMaxHeight().weight(1f),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.BottomEnd,
                    )
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(
    viewModel: ProgressViewModel,
    category: QuizCategory,
    score: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val total = GameLogic.QUIZ_QUESTIONS_PER_CATEGORY
    val best = (state.quizBestFor(category.id) ?: score).coerceAtLeast(score)
    val great = score >= 7
    GameBackground {
        Box(Modifier.fillMaxSize()) {
            ModalPanel(
                title = stringResource(R.string.quiz_result_title),
                badge = Assets.item(if (great) Assets.ITEM_GOBLET else Assets.ITEM_BELL),
            ) {
                Image(
                    painter = painterResource(if (great) Assets.MASCOT_C else Assets.MASCOT_B),
                    contentDescription = stringResource(if (great) R.string.cd_mascot_c else R.string.cd_mascot_b),
                    modifier = Modifier.size(130.dp),
                )
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = JJColors.Lavender,
                )
                Text(
                    text = stringResource(R.string.quiz_score, score, total),
                    style = MaterialTheme.typography.displaySmall.copy(shadow = TextGlow),
                    color = JJColors.Cream,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                StatLine(
                    label = stringResource(R.string.best_score),
                    value = stringResource(R.string.results_level_value, best, total),
                    highlight = score == total,
                    icon = Assets.item(Assets.ITEM_STAR),
                )
                PlateButton(stringResource(R.string.continue_label), onContinue, Modifier.fillMaxWidth())
                PlateButton(stringResource(R.string.restart), onRestart, Modifier.fillMaxWidth())
                PlateButton(stringResource(R.string.menu), onMenu, Modifier.fillMaxWidth())
            }
        }
    }
}
