package xyz.qhurc.jergal.common

import android.content.Context
import xyz.qhurc.jergal.model.platform.Platform

class ContentManager(context: Context) {
    init {
        getOrInitConfig(context)
    }

    private fun getOrInitConfig(context: Context) {
        ConfigManager.loadJson(context)
    }

    fun getPlatforms(): ArrayList<Platform> {
        return ConfigManager.mainConfig.platforms
    }

    fun getPlatformContents(platform: Platform) {

    }

    fun refreshPlatform(platform: Platform) {

    }
}