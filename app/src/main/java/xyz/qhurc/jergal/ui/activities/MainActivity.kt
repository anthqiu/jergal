package xyz.qhurc.jergal.ui.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import xyz.qhurc.jergal.model.Title
import xyz.qhurc.jergal.model.ui.navigationitem.AppsNavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.NavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.PlatformNavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.SearchNavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.SettingsNavigationItem
import xyz.qhurc.jergal.ui.layouts.createItem
import xyz.qhurc.jergal.ui.theme.JergalTheme

class MainActivity : AppCompatActivity() {
    val navigationItems = listOf<NavigationItem>(
        PlatformNavigationItem("3DS"),
        PlatformNavigationItem("PSP"),
        PlatformNavigationItem("NS"),
        PlatformNavigationItem("NDS"),
        PlatformNavigationItem("3DS1"),
        PlatformNavigationItem("PSP1"),
        PlatformNavigationItem("NS1"),
        PlatformNavigationItem("NDS1"),
        PlatformNavigationItem("3DS2"),
        PlatformNavigationItem("PSP2"),
        PlatformNavigationItem("NS2"),
        PlatformNavigationItem("NDS2"),
    ).sorted()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val titles = (1..199).map { Title(it.toString()) }

        setContent {
            JergalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row {
                        createNavigationLayout(navigationItems)
                        createMainGrid(titles = titles)
                    }
                }
            }
        }
    }

}

@Composable
fun createNavigationLayout(items: List<NavigationItem>) {
    val settingsItem = SettingsNavigationItem()
    val searchItem = SearchNavigationItem()
    val appsItem = AppsNavigationItem()
    val selectedItem = remember { mutableStateOf(appsItem as NavigationItem) }

    NavigationRail(
        modifier = Modifier.width(80.dp)
    ) {
        createNavigationItem(item = settingsItem, selectedItem = selectedItem)
        createNavigationItem(item = searchItem, selectedItem = selectedItem)
        createNavigationItem(item = appsItem, selectedItem = selectedItem)
    }
}


@Composable
fun createMainGrid(titles: List<Title>) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize(),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            itemsIndexed(titles) { index, item ->
                createItem(
                    it = item,
                    topPadding = index < 2,
                    bottomPadding = index >= (titles.size - 2)
                )
            }
        }
    )
}




@Composable
fun createNavigationItem(item: NavigationItem, selectedItem: MutableState<NavigationItem>) {
    NavigationRailItem(
        icon = {
            Icon(
                if (item == selectedItem.value) item.getSelectedIcon() else item.getNormalIcon(),
                contentDescription = null
            )
        },
        label = {
            Text(
                text = item.getName(),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        },
        selected = item == selectedItem.value,
        onClick = {
            selectedItem.value = item
            item.onClick()
        },
        alwaysShowLabel = false
    )
}

@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
fun mainPreview() {
    val navigationItems = listOf<NavigationItem>(
        PlatformNavigationItem("3DS"),
        PlatformNavigationItem("PSP"),
        PlatformNavigationItem("NS"),
        PlatformNavigationItem("NDS"),
        PlatformNavigationItem("3DS1"),
        PlatformNavigationItem("PSP1"),
        PlatformNavigationItem("NS1"),
        PlatformNavigationItem("NDS1"),
        PlatformNavigationItem("3DS2"),
        PlatformNavigationItem("PSP2"),
        PlatformNavigationItem("NS2"),
        PlatformNavigationItem("NDS2"),
    ).sorted()

    val titles = (1..200).map { Title(it.toString()) }

    JergalTheme {
        Row {
            createNavigationLayout(navigationItems)
            createMainGrid(titles = titles)
        }
    }
}