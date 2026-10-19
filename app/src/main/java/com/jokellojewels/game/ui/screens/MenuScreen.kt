package com.jokellojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jokellojewels.game.R
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.GameBackground
import com.jokellojewels.game.ui.components.GameTopBar
import com.jokellojewels.game.ui.components.MenuPlateButton

@Composable
fun MenuScreen(
    onBack: () -> Unit,
    onQuiz: () -> Unit,
    onPairs: () -> Unit,
    onResults: () -> Unit,
    onRules: () -> Unit,
    onSettings: () -> Unit,
) {
    GameBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            GameTopBar(title = stringResource(R.string.menu_title), onBack = onBack)
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                // Jester leaning in from the bottom-left corner, behind the buttons.
                Image(
                    painter = painterResource(Assets.MASCOT_B),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxHeight(0.42f)
                        .padding(start = 4.dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomStart,
                )
                Image(
                    painter = painterResource(Assets.item(Assets.ITEM_CHEST)),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 18.dp, bottom = 18.dp)
                        .size(maxWidth * 0.26f),
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(Assets.DECO_HAT),
                        contentDescription = null,
                        modifier = Modifier.height(64.dp),
                        contentScale = ContentScale.Fit,
                    )
                    MenuPlateButton(stringResource(R.string.mode_quiz), onQuiz, icon = Assets.DECO_BOOK)
                    MenuPlateButton(stringResource(R.string.mode_pairs), onPairs, icon = Assets.DECO_MASK)
                    MenuPlateButton(stringResource(R.string.results), onResults, icon = Assets.item(Assets.ITEM_GOBLET))
                    MenuPlateButton(stringResource(R.string.rules), onRules, icon = Assets.DECO_TICKET)
                    MenuPlateButton(stringResource(R.string.settings), onSettings, icon = Assets.DECO_SCEPTER)
                    Box(Modifier.height(this@BoxWithConstraints.maxHeight * 0.3f))
                }
            }
        }
    }
}
