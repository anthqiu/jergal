package xyz.qhurc.jergal.model.platform

import kotlinx.serialization.Serializable

@Serializable
data class Platform(
    val name: String = "",
    val shortName: String = "",
    val vendor: String = "",
    val boxArtWidthRatio: Int = 1,
    val boxArtHeightRatio: Int = 1,
    val players: ArrayList<Player> = arrayListOf()
): Comparable<Platform> {
    override fun compareTo(other: Platform): Int = when {
        this.vendor != other.vendor -> this.vendor compareTo other.vendor
        this.shortName != other.shortName -> this.shortName compareTo other.shortName
        this.name != other.name -> this.name compareTo other.name
        else -> 0
    }
}
