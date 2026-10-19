package com.jokellojewels.game.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jokellojewels.game.R
import com.jokellojewels.game.ui.Assets
import com.jokellojewels.game.ui.components.JewelCard
import com.jokellojewels.game.ui.components.LocalSoundManager
import com.jokellojewels.game.ui.components.MenuPlateButton
import com.jokellojewels.game.ui.components.PlateButton
import com.jokellojewels.game.ui.components.ScreenScaffold
import com.jokellojewels.game.ui.theme.JJColors
import com.jokellojewels.game.viewmodel.ProgressViewModel

/**
 * Hosted privacy policy page — REPLACE with your real URL before publishing.
 * Opening it hands off to the user's browser; the app itself has no INTERNET permission.
 */
const val PRIVACY_POLICY_URL = "https://example.com/jokello-jewels/privacy"

@Composable
fun SettingsScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sound = LocalSoundManager.current
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val linkUnavailable = stringResource(R.string.link_unavailable)
    val resetDone = stringResource(R.string.reset_done)

    ScreenScaffold(title = stringResource(R.string.settings), onBack = onBack) {
        JewelCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .toggleable(
                        value = state.soundEnabled,
                        role = Role.Switch,
                        onValueChange = { enabled ->
                            viewModel.setSoundEnabled(enabled)
                            if (enabled) {
                                sound?.enabled = true
                                sound?.click()
                            }
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Assets.item(Assets.ITEM_BELL)),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).padding(end = 10.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_sound),
                        style = MaterialTheme.typography.titleMedium,
                        color = JJColors.Cream,
                    )
                    Text(
                        text = stringResource(if (state.soundEnabled) R.string.settings_on else R.string.settings_off),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JJColors.Lavender,
                    )
                }
                Switch(
                    checked = state.soundEnabled,
                    onCheckedChange = null, // the whole row is the toggle target
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JJColors.White,
                        checkedTrackColor = JJColors.Ruby,
                        uncheckedThumbColor = JJColors.Lavender,
                        uncheckedTrackColor = JJColors.Night,
                        uncheckedBorderColor = JJColors.Lavender,
                    ),
                )
            }
        }

        JewelCard {
            Image(
                painter = painterResource(Assets.DECO_BOOK),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
            Text(
                text = stringResource(R.string.settings_privacy),
                style = MaterialTheme.typography.titleMedium,
                color = JJColors.Gold,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.settings_privacy_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = JJColors.Cream,
            )
            PlateButton(
                text = stringResource(R.string.settings_privacy),
                onClick = { openUri(context, PRIVACY_POLICY_URL, null, linkUnavailable) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MenuPlateButton(
                text = stringResource(R.string.settings_rate),
                widthFraction = 1f,
                icon = Assets.DECO_TICKET,
                onClick = {
                    val pkg = context.packageName
                    openUri(
                        context,
                        "market://details?id=$pkg",
                        "https://play.google.com/store/apps/details?id=$pkg",
                        linkUnavailable,
                    )
                },
            )
            PlateButton(
                text = stringResource(R.string.settings_reset),
                onClick = { confirmReset = true },
                modifier = Modifier.fillMaxWidth(),
            )
            Image(
                painter = painterResource(Assets.MASCOT_C),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally).height(160.dp),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = JJColors.Plum,
            titleContentColor = JJColors.Gold,
            textContentColor = JJColors.Cream,
            title = { Text(stringResource(R.string.reset_dialog_title)) },
            text = { Text(stringResource(R.string.reset_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        sound?.click()
                        confirmReset = false
                        viewModel.resetAll()
                        Toast.makeText(context, resetDone, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.reset_confirm), color = JJColors.Ruby, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        sound?.click()
                        confirmReset = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.cancel), color = JJColors.Cream) }
            },
        )
    }
}

/** Opens a link in another app; tries a fallback; never crashes if nothing can handle it. */
private fun openUri(context: Context, primary: String, fallback: String?, errorMessage: String) {
    fun tryOpen(uri: String): Boolean = try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }
    if (tryOpen(primary)) return
    if (fallback != null && tryOpen(fallback)) return
    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
}
