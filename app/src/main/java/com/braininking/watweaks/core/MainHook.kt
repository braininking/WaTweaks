package com.braininking.watweaks.core

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (!CompatibilityManager.isSupportedPackage(param.packageName)) return
        XposedBridge.log("WaTweaks: módulo carregado em " + param.packageName)
        XposedBridge.log("WaTweaks: versão de referência = " + CompatibilityManager.TARGET_WHATSAPP_VERSION)
        UIThemeEngine(param.classLoader).install()
    }
}
