package com.jokellojewels.game.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jokellojewels.game.R
import com.jokellojewels.game.audio.SoundManager
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.theme.JJColors

/** Provided at the root in MainActivity; null in previews/tests → sounds are skipped. */
val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }

/** Soft dark drop shadow so white / gold text stays readable on busy art. */
val TextGlow = Shadow(color = Color(0xCC000000), blurRadius = 6f)

/**
 * Respects the user's font scale but caps it, so labels on fixed-shape plates stay readable
 * and inside the art at very large system font sizes. Free-flowing text uses normal sp.
 */
@Composable
fun cappedSp(base: Float, maxScale: Float = 1.3f): TextUnit {
    val fontScale = LocalDensity.current.fontScale
    val effective = fontScale.coerceAtMost(maxScale)
    return (base * effective / fontScale).sp
}

/** Full-screen stage background (bg_main, opaque) + a light vignette for text contrast. */
@Composable
fun GameBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(JJColors.Night)) {
        Image(
            painter = painterResource(Assets.BG_MAIN),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            JJColors.Night.copy(alpha = 0.10f),
                            JJColors.Night.copy(alpha = 0.25f),
                            JJColors.Night.copy(alpha = 0.55f),
                        )
                    )
                )
        )
        content()
    }
}

private fun aspectFor(@DrawableRes plate: Int): Float? = when (plate) {
    Assets.BTN_PLATE -> Assets.BTN_PLATE_ASPECT
    Assets.BTN_MENU -> Assets.BTN_MENU_ASPECT
    else -> null
}

/**
 * Image-plate button: the plate art is drawn underneath at its own proportions, the WHITE BOLD
 * label is drawn on top in code. One plate image is reused by every button.
 */
@Composable
fun PlateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes plate: Int = Assets.BTN_PLATE,
    fontSize: TextUnit = cappedSp(18f, maxScale = 1.25f),
    overlayColor: Color? = null,
    semanticsLabel: String? = null,
    playClick: Boolean = true,
    labelPadding: Dp = 26.dp,
) {
    val sound = LocalSoundManager.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "plate-press")
    val aspect = aspectFor(plate)
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .then(if (aspect != null) Modifier.aspectRatio(aspect) else Modifier)
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.55f
            }
            .clip(shape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = JJColors.Gold),
                enabled = enabled,
                role = Role.Button,
            ) {
                if (playClick) sound?.click()
                onClick()
            }
            .then(
                if (semanticsLabel != null) Modifier.semantics { contentDescription = semanticsLabel } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(plate),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        if (overlayColor != null) {
            // Tint sits inside the gold rim so the plate still reads as the same button.
            Box(
                Modifier
                    .matchParentSize()
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .background(overlayColor.copy(alpha = 0.88f), shape)
            )
        }
        Text(
            text = text,
            color = JJColors.White,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            maxLines = 2,
            style = TextStyle(shadow = TextGlow),
            modifier = Modifier.padding(horizontal = labelPadding, vertical = 2.dp),
        )
    }
}

/** Ornate jester-scroll plate (btn_menu) with an optional art icon on its left. */
@Composable
fun MenuPlateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.84f,
    @DrawableRes icon: Int? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(widthFraction),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(54.dp).padding(end = 4.dp),
            )
        }
        PlateButton(
            text = text,
            onClick = onClick,
            modifier = Modifier.weight(1f),
            plate = Assets.BTN_MENU,
            fontSize = cappedSp(19f, maxScale = 1.2f),
            labelPadding = 44.dp,
        )
    }
}

/** Round icon button using an image asset; 56dp touch target and a content description. */
@Composable
fun IconImageButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sound = LocalSoundManager.current
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button) {
                sound?.click()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(52.dp))
    }
}

@Composable
fun GameTopBar(
    title: String,
    onBack: (() -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconImageButton(Assets.ICON_BACK, stringResource(R.string.cd_back), onBack)
        } else {
            Spacer(Modifier.size(56.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { heading() },
            style = MaterialTheme.typography.headlineSmall.copy(shadow = TextGlow),
            color = JJColors.Gold,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (trailing != null) trailing() else Spacer(Modifier.size(56.dp))
    }
}

/** Standard screen: background, safe insets, top bar, scrollable centered column. */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    GameBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            GameTopBar(title = title, onBack = onBack, trailing = trailing)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content,
            )
        }
    }
}

private val panelShape = RoundedCornerShape(26.dp)
private val panelFill = Brush.verticalGradient(listOf(JJColors.Velvet, JJColors.VelvetDeep, JJColors.Night))
private val panelBorder = Brush.linearGradient(listOf(JJColors.Gold, JJColors.GoldDeep, JJColors.Gold, JJColors.GoldDeep))

/** Non-modal content card, drawn in Compose (gradient + gold border) — grows with content. */
@Composable
fun JewelCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .clip(panelShape)
            .background(panelFill)
            .border(2.dp, panelBorder, panelShape)
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

/**
 * Modal panel (pause / results / rules): scrim that blocks touches + a Compose-drawn rounded
 * panel with gradient fill and gold border. Never stretches an image frame; scrolls if tall.
 * An optional art badge (jewel, mascot…) sits on top of the panel's border.
 */
@Composable
fun ModalPanel(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes badge: Int? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JJColors.Night.copy(alpha = 0.72f))
            .pointerInput(Unit) { detectTapGestures { } }
            .safeDrawingPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (badge != null) {
                Image(
                    painter = painterResource(badge),
                    contentDescription = null,
                    modifier = Modifier.size(84.dp).padding(bottom = 0.dp),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(18.dp, panelShape)
                    .clip(panelShape)
                    .background(panelFill)
                    .border(3.dp, panelBorder, panelShape)
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(shadow = TextGlow),
                    color = JJColors.Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                content()
            }
        }
    }
}

/** Continue / Restart / Menu — shared by quiz and pairs pause overlays. */
@Composable
fun PauseOverlay(
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
) {
    ModalPanel(title = stringResource(R.string.paused), badge = Assets.DECO_HOURGLASS) {
        PlateButton(stringResource(R.string.continue_label), onContinue, Modifier.fillMaxWidth())
        PlateButton(stringResource(R.string.restart), onRestart, Modifier.fillMaxWidth())
        PlateButton(stringResource(R.string.menu), onMenu, Modifier.fillMaxWidth())
    }
}

/** Friendly fallback for invalid args, locked content or anything unexpected. Never crashes. */
@Composable
fun FallbackScreen(
    message: String,
    onBack: () -> Unit,
) {
    ScreenScaffold(title = stringResource(R.string.fallback_title), onBack = onBack) {
        Spacer(Modifier.size(12.dp))
        Image(
            painter = painterResource(Assets.MASCOT_B),
            contentDescription = stringResource(R.string.cd_mascot_b),
            modifier = Modifier.size(220.dp),
        )
        JewelCard {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = JJColors.Cream,
                textAlign = TextAlign.Center,
            )
        }
        PlateButton(stringResource(R.string.back), onBack, Modifier.fillMaxWidth())
    }
}

@Composable
fun LoadingScreen() {
    GameBackground {
        val label = stringResource(R.string.loading)
        CircularProgressIndicator(
            color = JJColors.Gold,
            modifier = Modifier.align(Alignment.Center).semantics { contentDescription = label },
        )
    }
}

/** Label + value line used on result/summary panels. */
@Composable
fun StatLine(
    label: String,
    value: String,
    highlight: Boolean = false,
    @DrawableRes icon: Int? = null,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(30.dp).padding(end = 6.dp))
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = JJColors.Cream,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (highlight) JJColors.Emerald else JJColors.Gold,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Gold-rimmed pill used for HUD values (timer, pairs, progress). */
@Composable
fun HudPill(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    fill: Color = JJColors.VelvetDeep,
    borderColor: Color = JJColors.Gold,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fill.copy(alpha = 0.9f))
            .border(2.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp).padding(end = 4.dp))
        }
        Text(
            text = text,
            color = JJColors.Gold,
            fontWeight = FontWeight.Bold,
            fontSize = cappedSp(16f),
        )
    }
}
