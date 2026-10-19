package com.jokellojewels.game

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.jokellojewels.game.audio.SoundManager
import com.jokellojewels.game.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "jokello_game",
    // A corrupted preferences file is replaced with an empty one instead of crashing.
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

/** Manual dependency container (no DI framework). */
class AppContainer(
    val repository: GameRepository,
    val soundManager: SoundManager,
)

class JokelloApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        val repository = GameRepository(gameDataStore)
        val soundManager = SoundManager(this)
        container = AppContainer(repository, soundManager)

        // Sync the persisted sound toggle into SoundManager at startup and live afterwards.
        appScope.launch {
            repository.data
                .map { it.settings.soundEnabled }
                .distinctUntilChanged()
                .collect { soundManager.enabled = it }
        }
    }
}
