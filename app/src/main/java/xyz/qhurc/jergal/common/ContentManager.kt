package xyz.qhurc.jergal.common

import android.content.Context
import xyz.qhurc.jergal.model.Title
import xyz.qhurc.jergal.model.platform.Platform

object ContentManager {
    fun getOrInitConfig(context: Context) {
        ConfigManager.loadJson(context)
    }

    fun getPlatforms(): ArrayList<Platform> {
        return ConfigManager.mainConfig.platforms
    }

    fun getPlatformContents(platform: Platform): ArrayList<Title> {
        val ret = arrayListOf<Title>()
        for (i in 1 .. 197) {
            ret.add(Title(platform.name + ": " + i))
        }

        return ret
    }

    fun refreshPlatform(platform: Platform) {

    }
}