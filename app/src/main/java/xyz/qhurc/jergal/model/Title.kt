package xyz.qhurc.jergal.model

import kotlinx.serialization.Serializable

@Serializable
data class Title(
    val name: String = "",
    val tags: List<String> = listOf(),
    val publisher: String = ""
) : Comparable<Title> {
    override fun compareTo(other: Title): Int {
        return this.name compareTo other.name
    }
}
