package xyz.qhurc.jergal

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import xyz.qhurc.jergal.common.ContentManager
import xyz.qhurc.jergal.model.JC
import xyz.qhurc.jergal.model.ui.navigationitem.AndroidNavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.NavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.PlatformNavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.SearchNavigationItem
import xyz.qhurc.jergal.model.ui.navigationitem.SettingsNavigationItem
import xyz.qhurc.jergal.ui.screens.createAndroidAppsScreen
import xyz.qhurc.jergal.ui.screens.createPlatformGrid
import xyz.qhurc.jergal.ui.screens.createSettingsScreen
import xyz.qhurc.jergal.ui.theme.JergalTheme

class MainActivity : AppCompatActivity() {
    lateinit var navController: NavHostController
    lateinit var selectedItem: MutableState<NavigationItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initConstants()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            // true: for prevent back and do something in handleOnBackPressed
            override fun handleOnBackPressed() {
                // Do Something
            }
        })

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        setContent {
            initComposableConstants()
            hideSystemBars()

            JergalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row {
                        createNavigationLayout()
                        createScreens()
                    }
                }
            }
        }
    }


    private fun initConstants() {
        ContentManager.getOrInitConfig(this.applicationContext)
    }

    @Composable
    private fun initComposableConstants() {
        navController = rememberNavController()
        JC.initComposableVal(context = this.applicationContext)
    }

    @Composable
    fun createNavigationLayout() {
        selectedItem = remember { mutableStateOf(AndroidNavigationItem as NavigationItem) }
        val platforms = ContentManager.getPlatforms()
        val platformNavItems =
            remember { mutableStateOf(platforms.map { it -> PlatformNavigationItem(it) }) }

        NavigationRail(
            modifier = Modifier
                .width(80.dp)
                .statusBarsPadding()
        ) {
            createNavigationItem(item = SettingsNavigationItem, selectedItem = selectedItem)
            createNavigationItem(item = SearchNavigationItem, selectedItem = selectedItem)

            LazyColumn {
                item {
                    createNavigationItem(
                        item = AndroidNavigationItem,
                        selectedItem = selectedItem
                    )
                }
                items(platformNavItems.value) { item ->
                    createPlatformNavigationItem(item = item, selectedItem = selectedItem)
                }
            }
        }
    }

    @Composable
    fun createScreens(startDestination: String = AndroidNavigationItem.getName()) {
        val graph = navController.createGraph(startDestination) {
            composable(SettingsNavigationItem.getName()) { createSettingsScreen() }
            composable(SearchNavigationItem.getName()) {}
            composable(AndroidNavigationItem.getName()) { createAndroidAppsScreen() }
            val platforms = ContentManager.getPlatforms()
            platforms.forEach { platform ->
                composable(platform.name) { createPlatformGrid(platform = platform) }
            }
        }
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
                .fillMaxSize()
        ) {
            NavHost(navController = navController, graph = graph)
        }
    }


    @Composable
    fun createNavigationItem(
        item: NavigationItem,
        selectedItem: MutableState<NavigationItem>
    ) {
        NavigationRailItem(
            icon = {
                if (item == selectedItem.value) item.getSelectedIcon()
                else item.getNormalIcon()
            },
            label = {
                Text(
                    text = item.getName(),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            },
            selected = item == selectedItem.value, onClick = {
                selectedItem.value = item
                navController.navigate(item.getName())
            },
            alwaysShowLabel = false
        )
    }

    @Composable
    fun createPlatformNavigationItem(
        item: PlatformNavigationItem,
        selectedItem: MutableState<NavigationItem>
    ) {
        val selected = item == selectedItem.value

        NavigationRailItem(
            icon = {
                if (selected) item.getSelectedIcon()
                else item.getNormalIcon()
            },
            label = {
                Text(
                    text = item.platform.shortName,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            },
            selected = selected,
            onClick = {
                selectedItem.value = item
                navController.navigate(item.getName())
            },
            alwaysShowLabel = false
        )
    }

    @Composable
    fun hideSystemBars() {
        val view = LocalView.current

        if (!view.isInEditMode) {
            val controller = WindowInsetsControllerCompat(window, view)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
