package com.braininking.watweaks.core

object CompatibilityManager {
    val SUPPORTED_PACKAGES = setOf("com.whatsapp", "com.whatsapp.w4b")
    const val TARGET_WHATSAPP_VERSION = "2.26.41.77"

    fun isSupportedPackage(packageName: String): Boolean =
        packageName in SUPPORTED_PACKAGES

    fun isTargetVersion(versionName: String?): Boolean =
        versionName == TARGET_WHATSAPP_VERSION
}
