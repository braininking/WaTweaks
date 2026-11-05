package com.braininking.watweaks.privacy

/** Preferências locais do painel. Não registra hooks em protocolo, recibos ou criptografia. */
class PrivacyEngine {
    enum class FeatureState { CONFIGURABLE, PLANNED, BLOCKED }
    data class Feature(
        val id: String,
        val title: String,
        val description: String,
        val state: FeatureState
    )

    val catalog = listOf(
        Feature("compact_chat_rows", "Lista compacta", "Ajuste visual da lista de conversas.", FeatureState.CONFIGURABLE),
        Feature("bubble_colors", "Cores dos balões", "Personalização visual local.", FeatureState.CONFIGURABLE),
        Feature("hide_home_tabs", "Abas da tela inicial", "Ocultar elementos decorativos.", FeatureState.PLANNED),
        Feature("floating_bottom_bar", "Barra inferior", "Personalização visual da navegação.", FeatureState.PLANNED),
        Feature("anti_view_once", "Ignorar visualização única", "Contorna uma proteção de privacidade.", FeatureState.BLOCKED),
        Feature("anti_revoke", "Impedir revogação", "Impede que o remetente retire uma mensagem.", FeatureState.BLOCKED),
        Feature("hide_read_receipts", "Ocultar recibos", "Altera a transparência de leitura.", FeatureState.BLOCKED),
        Feature("anonymous_status", "Visualização anônima", "Suprime recibos de visualização.", FeatureState.BLOCKED)
    )

    data class UiSettings(
        val compactChatRows: Boolean = false,
        val customBubbleColors: Boolean = false,
        val hideDecorativeHomeTabs: Boolean = false,
        val floatingBottomBar: Boolean = false,
        val preferOriginalQuality: Boolean = false
    )

    fun validateSettings(settings: UiSettings): UiSettings = settings
}
