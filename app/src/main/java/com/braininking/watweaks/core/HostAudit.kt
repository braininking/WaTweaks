package com.braininking.watweaks.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import de.robv.android.xposed.XposedBridge
import java.security.MessageDigest

/** Audita apenas metadados do pacote; nunca lê mensagens, contatos ou chaves. */
object HostAudit {
    fun report(context: Context, packageName: String) {
        try {
            val pm = context.packageManager
            val info = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            }
            val version = info.versionName ?: "desconhecida"
            val code = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
            val installer = try {
                if (Build.VERSION.SDK_INT >= 30) pm.getInstallSourceInfo(packageName).installingPackageName
                else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(packageName)
                }
            } catch (_: Exception) { null }
            val signers = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else null
            val fingerprints = signers?.map { signature ->
                MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
                    .joinToString("") { "%02X".format(it) }
            } ?: emptyList()
            XposedBridge.log("WaTweaks audit: package=$packageName version=$version code=$code")
            XposedBridge.log("WaTweaks audit: installer=" + (installer ?: "indisponivel"))
            XposedBridge.log("WaTweaks audit: signerSha256=" + fingerprints.joinToString(",").ifBlank { "indisponivel" })
            XposedBridge.log("WaTweaks audit: sdk=" + Build.VERSION.SDK_INT + " release=" + Build.VERSION.RELEASE)
        } catch (e: Exception) {
            XposedBridge.log("WaTweaks audit: falha segura (" + e.javaClass.simpleName + ")")
        }
    }
}
