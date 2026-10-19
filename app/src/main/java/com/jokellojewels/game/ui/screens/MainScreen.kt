package com.jokellojewels.game.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.jokellojewels.game.R
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.GameBackground
import com.jokellojewels.game.ui.components.MenuPlateButton
import com.jokellojewels.game.ui.components.TextGlow
import com.jokellojewels.game.ui.theme.JJColors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Unique home composition — "Royal Jester Stage":
 * the logo hangs under the curtain valance and gently floats; a crown-shaped arc of jewels
 * twinkles beneath it; two jesters stand on the stage floor at either side, and the ornate
 * Start plate sits between them like a stage sign. No stats card, no stacked button column.
 * Everything is sized from the available height, so nothing scrolls on any phone.
 */
@Composable
fun MainScreen(onStart: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "home")
    val float = transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_200), RepeatMode.Reverse),
        label = "float",
    )
    val twinkle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_600), RepeatMode.Reverse),
        label = "twinkle",
    )

    GameBackground {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val h = maxHeight
            val w = maxWidth
            val logoWidth = min(w * 0.92f, h * 0.55f)
            val gemSize = min(w * 0.13f, 64.dp)

            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Assets.LOGO),
                    contentDescription = stringResource(R.string.cd_logo),
                    modifier = Modifier
                        .padding(top = h * 0.02f)
                        .widthIn(max = 520.dp)
                        .size(width = logoWidth, height = logoWidth / 1.445f)
                        .graphicsLayer { translationY = float.value * 6.dp.toPx() },
                    contentScale = ContentScale.Fit,
                )
                // Crown arc of jewels (centre one highest), twinkling in turn.
                val arc = listOf(Assets.ITEM_RUBY, Assets.ITEM_EMERALD, Assets.ITEM_STAR, Assets.ITEM_AMETHYST, Assets.ITEM_RUBY)
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    arc.forEachIndexed { i, item ->
                        val lift = sin(PI * i / (arc.size - 1)).toFloat()
                        Image(
                            painter = painterResource(Assets.item(item)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(gemSize)
                                .offset(y = -(lift * 14).dp)
                                .graphicsLayer {
                                    val phase = ((twinkle.value + i * 0.2f) % 1f)
                                    val s = 0.9f + 0.12f * sin(phase * PI).toFloat()
                                    scaleX = s
                                    scaleY = s
                                },
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 28.dp, end = 28.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(JJColors.Velvet.copy(alpha = 0f), JJColors.Velvet, JJColors.Velvet.copy(alpha = 0f))
                            ),
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 26.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.tagline),
                        style = MaterialTheme.typography.titleMedium.copy(shadow = TextGlow),
                        color = JJColors.Cream,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // --- Stage floor: two jesters flank the Start sign ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(h * 0.46f),
            ) {
                Image(
                    painter = painterResource(Assets.MASCOT_A),
                    contentDescription = stringResource(R.string.cd_mascot_a),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxHeight()
                        .widthIn(max = w * 0.46f)
                        .graphicsLayer { translationY = float.value * 4.dp.toPx() },
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomStart,
                )
                Image(
                    painter = painterResource(Assets.MASCOT_C),
                    contentDescription = stringResource(R.string.cd_mascot_c),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxHeight(0.94f)
                        .widthIn(max = w * 0.5f)
                        .graphicsLayer { translationY = -float.value * 4.dp.toPx() },
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd,
                )
            }
            MenuPlateButton(
                text = stringResource(R.string.start),
                onClick = onStart,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = h * 0.12f),
                widthFraction = 0.62f,
            )
        }
    }
}
