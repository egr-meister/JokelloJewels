package com.jokellojewels.game

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jokellojewels.game.ui.components.LocalSoundManager
import com.jokellojewels.game.ui.nav.AppNavHost
import com.jokellojewels.game.ui.theme.JokelloTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        val container = (application as JokelloApp).container
        setContent {
            JokelloTheme {
                CompositionLocalProvider(LocalSoundManager provides container.soundManager) {
                    AppNavHost(container = container)
                }
            }
        }
    }
}
