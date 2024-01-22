package xyz.qhurc.jergal.model.platform

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val name: String = "",
    val packageName: String = "",
    val activity: String = "",
    val arguments: String = "",
    val acceptedFilenameRegex: String = ""
): Comparable<Player> {
    override fun compareTo(other: Player): Int {
        return this.packageName compareTo other.packageName
    }
}