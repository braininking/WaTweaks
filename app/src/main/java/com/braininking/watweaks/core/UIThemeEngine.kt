package com.braininking.watweaks.core

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.Collections
import java.util.Locale
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Integração visual do WaTweaks no menu overflow nativo do WhatsApp.
 * Nenhuma View sobreposta é adicionada à tela de conversas.
 */
class UIThemeEngine(private val hostClassLoader: ClassLoader) {
    data class Settings(
        val compactRows: Boolean = false,
        val bubbleColors: Boolean = false,
        val hideHomeTabs: Boolean = false,
        val preferOriginalQuality: Boolean = false
    )

    private data class BubbleSignature(
        val textCount: Int,
        val smallTextCount: Int,
        val imageCount: Int,
        val childCount: Int,
        val hasGradientBackground: Boolean
    )

    @Volatile private var cached = Settings()
    private val installed = AtomicBoolean(false)
    private val processedBubbles =
        Collections.newSetFromMap(WeakHashMap<View, Boolean>())
    private val observedHdControls =
        Collections.newSetFromMap(WeakHashMap<View, Boolean>())

    companion object {
        private const val MENU_ITEM_ID = 0x575457
        private const val MENU_ITEM_ORDER = 900
        private const val MAIN_ACTIVITY = "com.whatsapp.Main"
        private const val MODULE_PACKAGE = "com.braininking.watweaks"
    }

    fun install() {
        if (!installed.compareAndSet(false, true)) return
        try {
            XposedHelpers.findAndHookMethod(
                Instrumentation::class.java,
                "callActivityOnResume",
                Activity::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.args.firstOrNull() as? Activity ?: return
                        if (!isSupportedTarget(activity)) return
                        cached = readSettings(activity)
                        activity.window?.decorView?.post {
                            val settings = cached
                            val root = activity.window?.decorView ?: return@post
                            if (settings.bubbleColors) {
                                scanForMessageBubbleCandidates(root, applyColor = true)
                            }
                            if (settings.preferOriginalQuality) {
                                inspectMediaQualityControls(activity, root)
                            }
                        }
                    }
                }
            )
            installOverflowMenuEntry()
            installInflaterObserver()
            XposedBridge.log("WaTweaks: hooks instalados; entrada nativa do menu em validação.")
        } catch (t: Throwable) {
            installed.set(false)
            XposedBridge.log("WaTweaks: falha ao instalar UIThemeEngine: ${t.javaClass.simpleName}")
        }
    }

    /**
     * Na versão analisada, Main herda de X.1f5 -> X.0J6.
     * X.0J6 declara onCreateOptionsMenu(Menu), que é o ponto real para
     * acrescentar um item ao menu sem desenhar um botão sobre a interface.
     */
    private fun installOverflowMenuEntry() {
        try {
            val menuHost = hostClassLoader.loadClass("X.0J6")
            XposedHelpers.findAndHookMethod(
                menuHost,
                "onCreateOptionsMenu",
                Menu::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as? Activity ?: return
                        if (activity.javaClass.name != MAIN_ACTIVITY) return
                        if (!isSupportedTarget(activity)) return
                        val menu = param.args.firstOrNull() as? Menu ?: return
                        if (menu.findItem(MENU_ITEM_ID) != null) return

                        val item = menu.add(
                            Menu.NONE,
                            MENU_ITEM_ID,
                            MENU_ITEM_ORDER,
                            "WaTweaks"
                        )
                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                        item.setOnMenuItemClickListener {
                            openWaTweaks(activity)
                            true
                        }
                        XposedBridge.log("WaTweaks: item adicionado ao menu de overflow da tela principal.")
                    }
                }
            )
            XposedBridge.log("WaTweaks: hook de X.0J6.onCreateOptionsMenu instalado.")
        } catch (t: Throwable) {
            XposedBridge.log(
                "WaTweaks: não foi possível integrar o menu nativo: " +
                    t.javaClass.simpleName
            )
        }
    }

    private fun openWaTweaks(activity: Activity) {
        try {
            val intent = activity.packageManager
                .getLaunchIntentForPackage(MODULE_PACKAGE) ?: run {
                    XposedBridge.log("WaTweaks: Activity principal do módulo não encontrada.")
                    return
                }
            activity.startActivity(intent)
        } catch (t: Throwable) {
            XposedBridge.log("WaTweaks: falha ao abrir painel: ${t.javaClass.simpleName}")
        }
    }

    private fun isSupportedTarget(activity: Activity): Boolean {
        if (!CompatibilityManager.isSupportedPackage(activity.packageName)) return false
        return try {
            val version = activity.packageManager
                .getPackageInfo(activity.packageName, 0).versionName
            val exact = CompatibilityManager.isTargetVersion(version)
            if (!exact) {
                XposedBridge.log(
                    "WaTweaks: versão não validada; ignorando hooks: " +
                        activity.packageName + " " + version
                )
            }
            exact
        } catch (t: Throwable) {
            XposedBridge.log("WaTweaks: não foi possível validar versão do host.")
            false
        }
    }

    private fun readSettings(activity: Activity): Settings {
        val values = HashMap<String, Boolean>()
        var cursor: Cursor? = null
        try {
            cursor = activity.contentResolver.query(
                SettingsProvider.URI, null, null, null, null
            )
            cursor?.use {
                val keyCol = it.getColumnIndex("key")
                val valueCol = it.getColumnIndex("value")
                while (it.moveToNext()) {
                    if (keyCol >= 0 && valueCol >= 0) {
                        values[it.getString(keyCol)] = it.getInt(valueCol) == 1
                    }
                }
            }
        } catch (t: Throwable) {
            XposedBridge.log("WaTweaks: leitura IPC indisponível: ${t.javaClass.simpleName}")
        }
        return Settings(
            compactRows = values["compact_chat_rows"] ?: false,
            bubbleColors = values["bubble_colors"] ?: false,
            hideHomeTabs = values["hide_home_tabs"] ?: false,
            preferOriginalQuality = values["prefer_original_quality"] ?: false
        )
    }

    private fun installInflaterObserver() {
        try {
            XposedHelpers.findAndHookMethod(
                LayoutInflater::class.java,
                "inflate",
                Int::class.javaPrimitiveType,
                ViewGroup::class.java,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val root = param.result as? View ?: return
                        if (!CompatibilityManager.isSupportedPackage(root.context.packageName)) return
                        root.post {
                            val settings = cached
                            if (settings.bubbleColors) {
                                scanForMessageBubbleCandidates(root, applyColor = true)
                            }
                            if (settings.preferOriginalQuality) inspectHdControls(root)
                        }
                    }
                }
            )
        } catch (t: Throwable) {
            XposedBridge.log(
                "WaTweaks: observador LayoutInflater indisponível: " +
                    t.javaClass.simpleName
            )
        }
    }

    private fun inspectSignature(root: View): BubbleSignature {
        var textCount = 0
        var smallTextCount = 0
        var imageCount = 0
        var childCount = 0
        fun walk(view: View, depth: Int) {
            if (depth > 7) return
            if (view is TextView) {
                textCount++
                val density = view.resources.displayMetrics.scaledDensity
                if (view.textSize / density < 13.5f) smallTextCount++
            }
            if (view is ImageView) imageCount++
            if (view is ViewGroup) {
                childCount += view.childCount
                for (i in 0 until view.childCount) walk(view.getChildAt(i), depth + 1)
            }
        }
        walk(root, 0)
        return BubbleSignature(
            textCount, smallTextCount, imageCount, childCount,
            root.background is GradientDrawable
        )
    }

    private fun scanForMessageBubbleCandidates(root: View, applyColor: Boolean) {
        if (root !is ViewGroup ||
            !CompatibilityManager.isSupportedPackage(root.context.packageName)
        ) return
        for (i in 0 until root.childCount) {
            scanForMessageBubbleCandidates(root.getChildAt(i), applyColor)
        }
        val signature = inspectSignature(root)
        val plausible = signature.hasGradientBackground &&
            signature.textCount in 2..4 &&
            signature.smallTextCount >= 1 &&
            signature.childCount in 2..12 &&
            root !is EditText
        if (!plausible) return
        synchronized(processedBubbles) {
            if (!processedBubbles.add(root)) return
        }
        XposedBridge.log(
            "WaTweaks: candidato de balão: ${root.javaClass.name}; " +
                "text=${signature.textCount}; children=${signature.childCount}; " +
                "images=${signature.imageCount}"
        )
        if (applyColor) tintBackground(root, Color.rgb(42, 76, 112))
    }

    private fun tintBackground(view: View, color: Int) {
        val original = view.background ?: return
        val drawable = original.constantState?.newDrawable()?.mutate() ?: original.mutate()
        if (drawable is GradientDrawable) {
            drawable.setColor(color)
            view.background = drawable
        }
    }

    private fun inspectMediaQualityControls(activity: Activity, root: View) {
        val controls = inspectHdControls(root)
        if (controls.isNotEmpty()) {
            XposedBridge.log(
                "WaTweaks: possíveis controles HD em ${activity.javaClass.name}; " +
                    "candidatos=${controls.size}. Estado não alterado."
            )
        }
    }

    private fun inspectHdControls(root: View): List<View> {
        val found = mutableListOf<View>()
        fun walk(view: View, depth: Int) {
            if (depth > 12) return
            if (isHdControl(view)) found.add(view)
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) walk(view.getChildAt(i), depth + 1)
            }
        }
        walk(root, 0)
        for (view in found) {
            synchronized(observedHdControls) {
                if (observedHdControls.add(view)) {
                    val state = when (view) {
                        is CompoundButton -> "checked=${view.isChecked}"
                        else -> "selected=${view.isSelected}, activated=${view.isActivated}"
                    }
                    XposedBridge.log(
                        "WaTweaks: possível controle HD: ${view.javaClass.name}; $state"
                    )
                }
            }
        }
        return found
    }

    private fun isHdControl(view: View): Boolean {
        val description = view.contentDescription?.toString()
            ?.lowercase(Locale.ROOT).orEmpty()
        val text = (view as? TextView)?.text?.toString()
            ?.lowercase(Locale.ROOT).orEmpty()
        val label = "$description $text"
        return listOf(
            "hd", "alta qualidade", "alta definição",
            "high quality", "high definition"
        ).any { label.contains(it) }
    }
}
