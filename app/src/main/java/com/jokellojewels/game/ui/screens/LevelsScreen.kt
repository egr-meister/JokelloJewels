package com.jokellojewels.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jokellojewels.game.R
import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.LevelConfig
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.LoadingScreen
import com.jokellojewels.game.ui.components.LocalSoundManager
import com.jokellojewels.game.ui.components.MenuPlateButton
import com.jokellojewels.game.ui.components.ModalPanel
import com.jokellojewels.game.ui.components.TextGlow
import com.jokellojewels.game.ui.components.PlateButton
import com.jokellojewels.game.ui.components.ScreenScaffold
import com.jokellojewels.game.ui.components.cappedSp
import com.jokellojewels.game.ui.theme.JJColors
import com.jokellojewels.game.viewmodel.ProgressViewModel

@Composable
fun LevelsScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit,
    onLevel: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showRules by rememberSaveable { mutableStateOf(false) }

    if (!state.loaded) {
        LoadingScreen()
        return
    }

    Box(Modifier.fillMaxSize()) {
        ScreenScaffold(title = stringResource(R.string.levels_title), onBack = onBack) {
            Text(
                text = stringResource(R.string.results_pairs_cleared, state.completedLevels, GameLogic.levelCount),
                style = MaterialTheme.typography.titleMedium,
                color = JJColors.Gold,
            )
            Column(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GameLogic.levels.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        row.forEach { config ->
                            LevelBubble(
                                config = config,
                                unlocked = state.isLevelUnlocked(config.level),
                                cleared = state.isLevelCleared(config.level),
                                onClick = { onLevel(config.level) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            MenuPlateButton(
                text = stringResource(R.string.rules),
                onClick = { showRules = true },
                widthFraction = 0.9f,
                icon = Assets.DECO_BOOK,
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Image(painterResource(Assets.DECO_HOURGLASS), contentDescription = null, modifier = Modifier.height(90.dp))
                Image(painterResource(Assets.MASCOT_C), contentDescription = null, modifier = Modifier.height(150.dp))
            }
        }
        if (showRules) {
            BackHandler { showRules = false }
            ModalPanel(title = stringResource(R.string.rules), badge = Assets.DECO_BOOK) {
                RulesContent(showQuiz = false)
                PlateButton(stringResource(R.string.continue_label), { showRules = false }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LevelBubble(
    config: LevelConfig,
    unlocked: Boolean,
    cleared: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sound = LocalSoundManager.current
    val cd = when {
        !unlocked -> stringResource(R.string.level_locked_cd, config.level)
        cleared -> stringResource(R.string.level_cleared_cd, config.level)
        else -> stringResource(R.string.level_open_cd, config.level, config.pairs, config.seconds)
    }
    Column(
        modifier = modifier
            .clip(CircleShape)
            .semantics(mergeDescendants = true) { contentDescription = cd }
            .clickable(enabled = unlocked, role = Role.Button) {
                sound?.click()
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).alpha(if (unlocked) 1f else 0.55f),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(Assets.PLATE_ROUND), contentDescription = null, modifier = Modifier.fillMaxSize())
            if (unlocked) {
                Text(
                    text = config.level.toString(),
                    color = JJColors.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = cappedSp(30f),
                    style = TextStyle(shadow = TextGlow),
                )
            } else {
                LockGlyph(Modifier.size(34.dp))
            }
        }
        Text(
            text = when {
                !unlocked -> stringResource(R.string.level_locked)
                cleared -> "★ " + stringResource(R.string.level_cleared)
                else -> stringResource(R.string.level_info, config.pairs, config.seconds)
            },
            color = if (cleared) JJColors.Gold else JJColors.Cream,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = cappedSp(13f),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

/** Padlock drawn in Compose (shape, not just color, signals "locked"). */
@Composable
private fun LockGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.12f
        drawArc(
            color = JJColors.Cream,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.08f),
            size = Size(w * 0.5f, h * 0.55f),
            style = Stroke(width = stroke),
        )
        drawRoundRect(
            color = JJColors.Gold,
            topLeft = Offset(w * 0.12f, h * 0.4f),
            size = Size(w * 0.76f, h * 0.56f),
            cornerRadius = CornerRadius(w * 0.12f),
        )
        drawCircle(color = JJColors.Night, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.66f))
    }
}

/** Shared rules text, used by the Rules screen and the Levels "Game Rules" panel. */
@Composable
fun RulesContent(showQuiz: Boolean = true, showPairs: Boolean = true) {
    if (showQuiz) {
        RuleSection(stringResource(R.string.rules_quiz_title), stringResource(R.string.rules_quiz_text))
    }
    if (showPairs) {
        RuleSection(stringResource(R.string.rules_pairs_title), stringResource(R.string.rules_pairs_text))
    }
    RuleSection(stringResource(R.string.rules_pause_title), stringResource(R.string.rules_pause_text))
}

@Composable
private fun RuleSection(title: String, body: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = JJColors.Gold, fontSize = 18.sp)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = JJColors.Cream)
    }
}
