package com.jokellojewels.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jokellojewels.game.R
import com.jokellojewels.game.logic.GameLogic
import com.jokellojewels.game.logic.LevelConfig
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.FallbackScreen
import com.jokellojewels.game.ui.components.GameBackground
import com.jokellojewels.game.ui.components.GameTopBar
import com.jokellojewels.game.ui.components.HudPill
import com.jokellojewels.game.ui.components.IconImageButton
import com.jokellojewels.game.ui.components.LoadingScreen
import com.jokellojewels.game.ui.components.PauseOverlay
import com.jokellojewels.game.ui.components.cappedSp
import com.jokellojewels.game.ui.theme.JJColors
import com.jokellojewels.game.viewmodel.PairsCardUi
import com.jokellojewels.game.viewmodel.PairsOutcome
import com.jokellojewels.game.viewmodel.PairsStatus
import com.jokellojewels.game.viewmodel.PairsViewModel

@Composable
fun PairsScreen(
    viewModel: PairsViewModel,
    onBack: () -> Unit,
    onFinished: (PairsOutcome) -> Unit,
    onMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.outcome) {
        state.outcome?.let(onFinished)
    }
    // Timer pauses automatically when the app leaves the foreground.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.pause() }

    when (state.status) {
        PairsStatus.LOADING -> {
            LoadingScreen()
            return
        }
        PairsStatus.INVALID -> {
            FallbackScreen(stringResource(R.string.fallback_message), onBack)
            return
        }
        PairsStatus.LOCKED -> {
            FallbackScreen(stringResource(R.string.fallback_level_locked), onBack)
            return
        }
        PairsStatus.PLAYING, PairsStatus.FINISHED -> Unit
    }

    val config = state.config ?: run {
        FallbackScreen(stringResource(R.string.fallback_message), onBack)
        return
    }

    BackHandler(enabled = state.status == PairsStatus.PLAYING) {
        if (state.paused) viewModel.resume() else viewModel.pause()
    }

    GameBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            GameTopBar(
                title = stringResource(R.string.level_n, config.level),
                onBack = { viewModel.pause() },
                trailing = {
                    IconImageButton(Assets.ICON_PAUSE, stringResource(R.string.cd_pause), { viewModel.pause() })
                },
            )
            PairsHud(
                secondsLeft = state.secondsLeft,
                matched = state.matchedPairs,
                totalPairs = config.pairs,
            )
            CardGrid(
                config = config,
                cards = state.cards,
                onTap = viewModel::onCardTapped,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            )
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

@Composable
private fun PairsHud(secondsLeft: Int, matched: Int, totalPairs: Int) {
    val low = secondsLeft <= 10
    val timeCd = stringResource(R.string.pairs_time_cd, secondsLeft)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudPill(
            // "!" also marks low time, so the warning is not color-only.
            text = GameLogic.formatTime(secondsLeft) + if (low) " !" else "",
            icon = Assets.DECO_HOURGLASS,
            fill = if (low) JJColors.WrongRed else JJColors.VelvetDeep,
            modifier = Modifier.semantics { contentDescription = timeCd },
        )
        HudPill(
            text = stringResource(R.string.pairs_matched, matched, totalPairs),
            icon = Assets.item(Assets.ITEM_EMERALD),
            borderColor = JJColors.Emerald,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

private const val CARD_ASPECT = 0.76f // width / height, portrait cards like the reference

/**
 * Grid sized to fit BOTH the available width and height, so every level (up to 24 cards)
 * is visible at once without scrolling, with portrait cards as large as possible.
 */
@Composable
private fun CardGrid(
    config: LevelConfig,
    cards: List<PairsCardUi>,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val columns = config.columns
        val rows = config.rows
        val gap = if (rows >= 6) 8.dp else 12.dp
        val widthByCols: Dp = (maxWidth - gap * (columns - 1)) / columns
        val heightByRows: Dp = (maxHeight - gap * (rows - 1)) / rows
        val cardWidth = min(min(widthByCols, heightByRows * CARD_ASPECT), 170.dp).coerceAtLeast(44.dp)
        val cardHeight = cardWidth / CARD_ASPECT
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            cards.chunked(columns).forEachIndexed { rowIndex, rowCards ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    rowCards.forEachIndexed { colIndex, card ->
                        MemoryCard(
                            card = card,
                            position = rowIndex * columns + colIndex + 1,
                            onTap = { onTap(card.id) },
                            modifier = Modifier.size(width = cardWidth, height = cardHeight),
                        )
                    }
                }
            }
        }
    }
}

private val cardBack = Brush.verticalGradient(listOf(JJColors.Velvet, JJColors.VelvetDeep))
private val cardFace = Brush.verticalGradient(listOf(Color(0xFF6B2DB8), JJColors.Velvet, JJColors.VelvetDeep))
private val cardMatched = Brush.verticalGradient(listOf(Color(0xFF2E7D55), Color(0xFF1B4D36), JJColors.VelvetDeep))

@Composable
private fun MemoryCard(
    card: PairsCardUi,
    position: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.faceUp || card.matched) 180f else 0f,
        animationSpec = tween(320),
        label = "flip",
    )
    val name = Assets.itemName(card.itemIndex)
    val cd = when {
        card.matched -> stringResource(R.string.card_matched_cd, position, name)
        card.faceUp -> stringResource(R.string.card_face_cd, position, name)
        else -> stringResource(R.string.card_hidden_cd, position)
    }
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .semantics { contentDescription = cd }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(shape)
            .clickable(enabled = !card.faceUp && !card.matched, role = Role.Button, onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        if (rotation <= 90f) {
            // Card back: royal-purple gradient, gold rim, jester-hat emblem from the art pack.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(cardBack, shape)
                    .border(2.dp, JJColors.Gold, shape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Assets.DECO_HAT),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.62f),
                    contentScale = ContentScale.Fit,
                )
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f } // un-mirror the face
                    .background(if (card.matched) cardMatched else cardFace, shape)
                    .border(if (card.matched) 3.dp else 2.dp, if (card.matched) JJColors.Emerald else JJColors.Gold, shape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Assets.item(card.itemIndex)),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.74f),
                    contentScale = ContentScale.Fit,
                )
                if (card.matched) {
                    // ✓ badge: matched state is not signalled by color alone.
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(JJColors.CorrectGreen, CircleShape)
                            .border(1.dp, JJColors.Gold, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓", color = JJColors.White, fontWeight = FontWeight.Bold, fontSize = cappedSp(12f, 1f))
                    }
                }
            }
        }
    }
}
