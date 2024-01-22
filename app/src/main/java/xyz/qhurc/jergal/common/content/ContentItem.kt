package xyz.qhurc.jergal.common.content

import android.graphics.drawable.Icon

abstract class ContentItem {
    abstract fun getTitle(): String
    abstract fun getCategory(): String
    abstract fun getProperty(): String
    abstract fun getIcon(): Icon?
}