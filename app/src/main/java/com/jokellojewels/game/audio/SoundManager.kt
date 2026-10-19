package com.jokellojewels.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.jokellojewels.game.R
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Short SFX through SoundPool. Created once in the Application.
 * [enabled] is synced from DataStore at startup and whenever the setting changes.
 * Every call is safe: unloaded or failed sounds are silently skipped.
 */
class SoundManager(context: Context) {

    enum class Sfx(@RawRes val resId: Int) {
        CLICK(R.raw.sfx_click),
        CORRECT(R.raw.sfx_correct),
        WRONG(R.raw.sfx_wrong),
        MATCH(R.raw.sfx_match),
        WIN(R.raw.sfx_win),
        LOSE(R.raw.sfx_lose),
    }

    @Volatile
    var enabled: Boolean = true

    private val soundIds = ConcurrentHashMap<Sfx, Int>()
    private val loadedIds: MutableSet<Int> = Collections.synchronizedSet(HashSet())

    private val pool: SoundPool? = try {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    } catch (e: Exception) {
        null
    }

    init {
        val appContext = context.applicationContext
        pool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedIds.add(sampleId)
        }
        Sfx.entries.forEach { sfx ->
            try {
                pool?.load(appContext, sfx.resId, 1)?.let { id -> if (id != 0) soundIds[sfx] = id }
            } catch (e: Exception) {
                // Missing or unreadable resource: that sound just stays silent.
            }
        }
    }

    fun play(sfx: Sfx) {
        if (!enabled) return
        val p = pool ?: return
        val id = soundIds[sfx] ?: return
        if (id !in loadedIds) return
        try {
            p.play(id, VOLUME, VOLUME, 1, 0, 1f)
        } catch (e: Exception) {
            // Ignore playback failures.
        }
    }

    fun click() = play(Sfx.CLICK)
    fun correct() = play(Sfx.CORRECT)
    fun wrong() = play(Sfx.WRONG)
    fun match() = play(Sfx.MATCH)
    fun win() = play(Sfx.WIN)
    fun lose() = play(Sfx.LOSE)

    private companion object {
        const val VOLUME = 0.8f
    }
}
