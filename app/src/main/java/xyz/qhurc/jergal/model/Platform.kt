package xyz.qhurc.jergal.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done

data class Platform(val name: String = ""): Comparable<Platform> {
    val icon = Icons.Default.Done
    override fun compareTo(other: Platform): Int = when {
        this.name != other.name -> this.name compareTo other.name
        else -> 0
    }
}

