package xyz.qhurc.jergal.model

import kotlinx.serialization.Serializable
import xyz.qhurc.jergal.model.platform.Platform

@Serializable
data class ConfigFile(
    val platforms: ArrayList<Platform> = arrayListOf<Platform>(),
)