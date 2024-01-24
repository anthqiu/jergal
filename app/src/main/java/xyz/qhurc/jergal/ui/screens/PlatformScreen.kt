package xyz.qhurc.jergal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.qhurc.jergal.common.ContentManager
import xyz.qhurc.jergal.model.JC
import xyz.qhurc.jergal.model.platform.Platform
import xyz.qhurc.jergal.ui.items.createPlatformGridItem

@Composable
fun createPlatformGrid(platform: Platform) {
    val titles = ContentManager.getPlatformContents(platform)

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = JC.PLATFORM_ITEM_WIDTH),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize(),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            itemsIndexed(titles) { index, item ->
                createPlatformGridItem(
                    it = item
                )
            }
        }
    )
}