// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.overlay.editor

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.dolphinemu.dolphinemu.NativeLibrary
import java.io.File

object OverlayStorage {
    private const val DIR = "overlay-layouts"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "kind"
    }

    private fun baseDir(context: Context): File = File(context.filesDir, DIR)

    private fun fileFor(context: Context, gameId: String?, isGlobal: Boolean): File {
        val dir = baseDir(context)
        if (!dir.exists()) dir.mkdirs()
        val name = if (isGlobal || gameId.isNullOrEmpty()) "global_wii.json" else "${gameId}_wii.json"
        return File(dir, name)
    }

    fun load(context: Context, preferGameSpecific: Boolean = true): OverlayLayout? {
        val gameId = try { NativeLibrary.GetCurrentGameID() } catch (_: Exception) { null }
        val files = buildList {
            if (preferGameSpecific && !gameId.isNullOrEmpty()) add(fileFor(context, gameId, false))
            add(fileFor(context, null, true))
        }
        val f = files.firstOrNull { it.exists() } ?: return null
        return runCatching { json.decodeFromString<OverlayLayout>(f.readText()) }.getOrNull()
    }

    fun save(context: Context, layout: OverlayLayout, gameSpecific: Boolean) {
        val gameId = try { NativeLibrary.GetCurrentGameID() } catch (_: Exception) { null }
        val f = fileFor(context, gameId, !gameSpecific)
        f.writeText(json.encodeToString(layout))
    }
}
