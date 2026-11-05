package com.braininking.watweaks.core

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Binder

/**
 * Read-only IPC bridge. Only exposes an explicit allowlist of UI booleans.
 * Calls from packages other than the official WhatsApp variants are rejected.
 */
class SettingsProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.braininking.watweaks.settings"
        val URI: Uri = Uri.parse("content://$AUTHORITY/preferences")
        private val ALLOWED_CALLERS = setOf("com.whatsapp", "com.whatsapp.w4b")
        private val ALLOWED_KEYS = setOf(
            "compact_chat_rows", "bubble_colors", "hide_home_tabs",
            "floating_bottom_bar", "prefer_original_quality"
        )
    }

    private fun allowedCaller(): Boolean {
        val pm = context?.packageManager ?: return false
        val packages = pm.getPackagesForUid(Binder.getCallingUid())?.toSet().orEmpty()
        return packages.any { it in ALLOWED_CALLERS }
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor {
        val ctx = context ?: return MatrixCursor(arrayOf("key", "value"))
        if (!allowedCaller()) return MatrixCursor(arrayOf("key", "value"))
        val requested = selectionArgs?.firstOrNull()
        val keys = if (requested != null && requested in ALLOWED_KEYS) listOf(requested) else ALLOWED_KEYS
        val prefs = ctx.getSharedPreferences("watweaks_settings", android.content.Context.MODE_PRIVATE)
        return MatrixCursor(arrayOf("key", "value")).apply {
            keys.forEach { key -> addRow(arrayOf(key, if (prefs.getBoolean(key, false)) 1 else 0)) }
        }
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.watweaks.preference"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun onCreate(): Boolean = true
}
