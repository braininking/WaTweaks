package com.braininking.watweaks

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Switch
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("watweaks_settings", MODE_PRIVATE) }
    private lateinit var content: LinearLayout
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
            setBackgroundColor(Color.rgb(18, 20, 24))
        }
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, ViewGroup.LayoutParams(-1, -1))
        setContentView(root)
        showHome()
    }

    override fun onBackPressed() {
        showHome()
    }

    private fun showHome() {
        content.removeAllViews()
        content.addView(label("WaTweaks", 28, Color.WHITE, true))
        content.addView(label("Configurações extras", 15, 0xFFB8C0CC.toInt(), false))
        content.addView(spacer(18))
        category("Privacidade e segurança", "Opções locais e compatibilidade", "privacy") 
        category("Temas", "Cores e aparência", "themes")
        category("Tela inicial", "Lista de conversas e abas", "home")
        category("Tela de conversas", "Personalização dos balões", "chat")
        category("Mídia", "Preferências de qualidade", "media")
        content.addView(spacer(18))
        content.addView(label(
            "Acesso pelo menu dos três pontinhos do WhatsApp. Nenhum botão flutuante é criado.",
            13, 0xFFB8C0CC.toInt(), false
        ))
        content.addView(spacer(12))
        content.addView(label(
            "Compatibilidade de referência: WhatsApp 2.26.41.77. A integração ainda precisa de teste em aparelho.",
            12, 0xFFFFC66D.toInt(), false
        ))
    }

    private fun showCategory(title: String, subtitle: String, keys: List<Triple<String, String, String>>) {
        content.removeAllViews()
        val back = label("‹  Configurações", 16, 0xFF83B6FF.toInt(), true)
        back.setOnClickListener { showHome() }
        content.addView(back)
        content.addView(spacer(14))
        content.addView(label(title, 25, Color.WHITE, true))
        content.addView(label(subtitle, 14, 0xFFB8C0CC.toInt(), false))
        content.addView(spacer(18))
        keys.forEach { (key, name, description) -> addToggle(content, key, name, description) }
    }

    private fun category(title: String, subtitle: String, page: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(0xFF242830.toInt())
            isClickable = true
            isFocusable = true
            setOnClickListener {
                when (page) {
                    "privacy" -> showCategory(title, subtitle, emptyList())
                    "themes" -> showCategory(title, subtitle, listOf(
                        Triple("bubble_colors", "Cores personalizadas dos balões", "Altera a aparência dos balões compatíveis; experimental.")
                    ))
                    "home" -> showCategory(title, subtitle, listOf(
                        Triple("compact_chat_rows", "Lista de conversas compacta", "Ajuste visual experimental."),
                        Triple("hide_home_tabs", "Ocultar abas da tela inicial", "Ajuste visual experimental.")
                    ))
                    "chat" -> showCategory(title, subtitle, listOf(
                        Triple("bubble_colors", "Cores personalizadas dos balões", "Ajuste visual experimental.")
                    ))
                    "media" -> showCategory(title, subtitle, listOf(
                        Triple("prefer_original_quality", "Preferir qualidade original", "Preferência local; não remove limites nem garante envio sem compressão.")
                    ))
                }
            }
        }
        card.addView(label(title, 17, Color.WHITE, true))
        card.addView(label(subtitle, 13, 0xFFB8C0CC.toInt(), false))
        card.addView(label("›", 22, 0xFF83B6FF.toInt(), true).apply { gravity = Gravity.END })
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)
        content.addView(card, lp)
    }

    private fun addToggle(parent: LinearLayout, key: String, title: String, description: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(0xFF242830.toInt())
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val textColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        textColumn.addView(label(title, 16, Color.WHITE, true))
        textColumn.addView(label(description, 12, 0xFFB8C0CC.toInt(), false))
        row.addView(textColumn, LinearLayout.LayoutParams(0, -2, 1f))
        val toggle = Switch(this).apply {
            isChecked = prefs.getBoolean(key, false)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() }
        }
        row.addView(toggle)
        card.addView(row)
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)
        parent.addView(card, lp)
    }

    private fun label(value: String, size: Int, color: Int, bold: Boolean) =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
        }

    private fun spacer(height: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(height))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
