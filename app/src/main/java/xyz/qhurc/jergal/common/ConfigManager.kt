package xyz.qhurc.jergal.common

import xyz.qhurc.jergal.BuildConfig
import android.content.Context
import xyz.qhurc.jergal.model.ConfigFile
import xyz.qhurc.jergal.model.platform.Platform
import xyz.qhurc.jergal.model.platform.Player
import xyz.qhurc.jergal.util.GsonHelper
import xyz.qhurc.jergal.util.JergalLog
import java.io.File

object ConfigManager {
    const val TAG = "ConfigManager"

    lateinit var mainConfig: ConfigFile

    fun saveJson(context: Context) {
        val file = File(context.getExternalFilesDir(null), "config.json")
        val json = GsonHelper.toJson(mainConfig)
        file.writeText(json)
    }

    fun loadJson(context: Context) {
        val file = File(context.getExternalFilesDir(null), "config.json")
        if (!file.isFile) {
            mainConfig = ConfigFile()
            if (BuildConfig.DEBUG) {
                JergalLog.debug(TAG, "Adding debug platforms")
                mainConfig.platforms.add(Platform("Nintendo 3DS", "3DS", "Nintendo"))
                mainConfig.platforms.add(Platform("Nintendo DS", "NDS", "Nintendo"))
                mainConfig.platforms.add(Platform("Nintendo Switch", "NS", "Nintendo"))
                mainConfig.platforms.add(Platform("Sony PlayStation", "PSX", "Sony"))
                mainConfig.platforms.add(Platform("Sony PlayStation 2", "PS2", "Sony"))
                mainConfig.platforms.add(Platform("Sony PlayStation Portable", "PSP", "Sony"))
                mainConfig.platforms[0].players.add(Player("Citra"))
                mainConfig.platforms.sort()
            }
            saveJson(context)
        }
        val json = file.readText()
        mainConfig = GsonHelper.fromJson(json)
    }
}