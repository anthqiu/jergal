package me.qsx.jergal.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.Display
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import me.qsx.jergal.dualscreen.RetroGame
import me.qsx.jergal.ui.theme.color
import me.qsx.jergal.ui.theme.onColor
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Renders the top-screen showcase for the currently highlighted game.
 */
@Composable
fun LauncherTopScreen(game: RetroGame) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            val isWideLayout = maxWidth >= 720.dp

            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    TopHeroCard(
                        game = game,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                    )
                    TopDetailsCard(
                        game = game,
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight(),
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TopHeroCard(
                        game = game,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    TopDetailsCard(
                        game = game,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopHeroCard(
    game: RetroGame,
    modifier: Modifier = Modifier,
) {
    val accentColor = game.accentRole.color()
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Hero",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Hero preview",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = game.logo,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = game.tagline,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopDetailsCard(
    game: RetroGame,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Selected game",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = game.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = game.tagline,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(text = game.genre)
                MetaPill(text = game.platformLabel)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
            Text(
                text = "Overview",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = game.heroSummary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Renders the bottom-screen library grid and the Android-style app drawer interaction.
 */
@Composable
fun LauncherBottomScreen(
    activity: Activity,
    games: List<RetroGame>,
    selectedGame: RetroGame,
    onSelect: (RetroGame) -> Unit,
) {
    val installedApps = remember(activity) { loadLauncherApps(activity.packageManager) }
    val gridState = rememberLazyGridState()

    LaunchedEffect(selectedGame.id) {
        val selectedIndex = games.indexOfFirst { it.id == selectedGame.id }
        if (selectedIndex >= 0) {
            gridState.animateScrollToItem(selectedIndex)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val closedDrawerOffsetPx = constraints.maxHeight.toFloat()
        val animationScope = rememberCoroutineScope()
        val animatedDrawerOffsetPx = remember(closedDrawerOffsetPx) {
            androidx.compose.animation.core.Animatable(closedDrawerOffsetPx)
        }
        var drawerOffsetPx by remember(closedDrawerOffsetPx) { mutableFloatStateOf(closedDrawerOffsetPx) }
        var isDrawerDragging by remember { mutableStateOf(false) }
        var isDrawerAnimating by remember { mutableStateOf(false) }
        val visibleDrawerOffsetPx = if (isDrawerAnimating) {
            animatedDrawerOffsetPx.value
        } else {
            drawerOffsetPx
        }
        val isAppDrawerVisible = isDrawerDragging || isDrawerAnimating || visibleDrawerOffsetPx < closedDrawerOffsetPx
        val isLibraryInteractionLocked = isAppDrawerVisible

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(vertical = 20.dp)
                    .pointerInput(Unit) {
                        var velocityTracker = VelocityTracker()
                        var dragStarted = false
                        detectVerticalDragGestures(
                            onDragStart = {
                                velocityTracker = VelocityTracker()
                                dragStarted = false
                            },
                            onVerticalDrag = { change, dragAmount ->
                                if (isAppDrawerVisible && !isDrawerDragging) {
                                    return@detectVerticalDragGestures
                                }
                                if (!dragStarted) {
                                    if (dragAmount >= 0f) {
                                        return@detectVerticalDragGestures
                                    }
                                    dragStarted = true
                                    isDrawerDragging = true
                                    drawerOffsetPx = closedDrawerOffsetPx
                                }
                                change.consume()
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                drawerOffsetPx = (drawerOffsetPx + dragAmount)
                                    .coerceIn(0f, closedDrawerOffsetPx)
                            },
                            onDragEnd = {
                                if (!isDrawerDragging) {
                                    return@detectVerticalDragGestures
                                }
                                val velocityY = velocityTracker.calculateVelocity().y
                                val shouldOpen =
                                    velocityY < -1400f || drawerOffsetPx < closedDrawerOffsetPx * 0.55f
                                val targetOffset = if (shouldOpen) 0f else closedDrawerOffsetPx
                                isDrawerDragging = false
                                isDrawerAnimating = true
                                animationScope.launch {
                                    animatedDrawerOffsetPx.snapTo(drawerOffsetPx)
                                    animatedDrawerOffsetPx.animateTo(
                                        targetValue = targetOffset,
                                        animationSpec = spring(
                                            dampingRatio = 0.92f,
                                            stiffness = Spring.StiffnessMediumLow,
                                        ),
                                    )
                                    drawerOffsetPx = targetOffset
                                    isDrawerAnimating = false
                                }
                            },
                            onDragCancel = {
                                if (!isDrawerDragging) {
                                    return@detectVerticalDragGestures
                                }
                                isDrawerDragging = false
                                isDrawerAnimating = true
                                animationScope.launch {
                                    animatedDrawerOffsetPx.snapTo(drawerOffsetPx)
                                    animatedDrawerOffsetPx.animateTo(
                                        targetValue = closedDrawerOffsetPx,
                                        animationSpec = spring(
                                            dampingRatio = 0.92f,
                                            stiffness = Spring.StiffnessMediumLow,
                                        ),
                                    )
                                    drawerOffsetPx = closedDrawerOffsetPx
                                    isDrawerAnimating = false
                                }
                            },
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    val gridHorizontalPadding = 24.dp
                    val gridVerticalPadding = 4.dp
                    val gridSpacing = 16.dp
                    val cardWidth = maxOf(
                        120.dp,
                        (maxHeight - (gridVerticalPadding * 2) - gridSpacing) / 2f,
                    )

                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        state = gridState,
                        userScrollEnabled = !isLibraryInteractionLocked,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = gridHorizontalPadding,
                            vertical = gridVerticalPadding,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(gridSpacing),
                    ) {
                        items(
                            items = games,
                            key = { it.id },
                        ) { game ->
                            GameShelfCard(
                                game = game,
                                isSelected = game.id == selectedGame.id,
                                cardWidth = cardWidth,
                                onSelect = onSelect,
                            )
                        }
                    }
                }

                SwipeHintBar(modifier = Modifier.padding(horizontal = 24.dp))
            }
        }

        if (isAppDrawerVisible) {
            BackHandler {
                isDrawerDragging = false
                isDrawerAnimating = true
                animationScope.launch {
                    animatedDrawerOffsetPx.snapTo(visibleDrawerOffsetPx)
                    animatedDrawerOffsetPx.animateTo(
                        targetValue = closedDrawerOffsetPx,
                        animationSpec = spring(
                            dampingRatio = 0.92f,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                    drawerOffsetPx = closedDrawerOffsetPx
                    isDrawerAnimating = false
                }
            }
            FullScreenAppDrawer(
                apps = installedApps,
                drawerOffsetPx = visibleDrawerOffsetPx,
                onDragStart = {
                    if (isDrawerAnimating) {
                        animationScope.launch {
                            animatedDrawerOffsetPx.stop()
                        }
                        isDrawerAnimating = false
                    }
                    isDrawerDragging = true
                    drawerOffsetPx = visibleDrawerOffsetPx
                },
                onDrag = { dragAmount ->
                    val updatedOffset = (drawerOffsetPx + dragAmount)
                        .coerceIn(0f, closedDrawerOffsetPx)
                    val consumed = updatedOffset - drawerOffsetPx
                    drawerOffsetPx = updatedOffset
                    consumed
                },
                onDragEnd = { velocityY ->
                    val shouldDismiss = velocityY > 1400f || drawerOffsetPx > closedDrawerOffsetPx * 0.3f
                    val targetOffset = if (shouldDismiss) closedDrawerOffsetPx else 0f
                    isDrawerDragging = false
                    isDrawerAnimating = true
                    animationScope.launch {
                        animatedDrawerOffsetPx.snapTo(drawerOffsetPx)
                        animatedDrawerOffsetPx.animateTo(
                            targetValue = targetOffset,
                            animationSpec = spring(
                                dampingRatio = 0.92f,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        )
                        drawerOffsetPx = targetOffset
                        isDrawerAnimating = false
                    }
                },
                onLaunchApp = { app ->
                    launchAppOnCurrentDisplay(activity, app)
                    isDrawerDragging = false
                    isDrawerAnimating = true
                    animationScope.launch {
                        animatedDrawerOffsetPx.snapTo(visibleDrawerOffsetPx)
                        animatedDrawerOffsetPx.animateTo(
                            targetValue = closedDrawerOffsetPx,
                            animationSpec = spring(
                                dampingRatio = 0.92f,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        )
                        drawerOffsetPx = closedDrawerOffsetPx
                        isDrawerAnimating = false
                    }
                },
            )
        }
    }
}

@Composable
private fun FullScreenAppDrawer(
    apps: List<LauncherApp>,
    drawerOffsetPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Float,
    onDragEnd: (Float) -> Unit,
    onLaunchApp: (LauncherApp) -> Unit,
) {
    val listState = rememberLazyListState()
    val isListAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    var listGestureStartedAtTop by remember { mutableStateOf(false) }
    var listCloseDragInProgress by remember { mutableStateOf(false) }
    val listNestedScrollConnection = remember(drawerOffsetPx, isListAtTop) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }
                if (listCloseDragInProgress) {
                    if (available.y < 0 && drawerOffsetPx <= 0f) {
                        listCloseDragInProgress = false
                        return Offset.Zero
                    }
                    val consumedY = onDrag(available.y)
                    if (consumedY == 0f && available.y < 0f) {
                        listCloseDragInProgress = false
                    }
                    return Offset(x = 0f, y = consumedY)
                }
                if (available.y > 0 && isListAtTop && listGestureStartedAtTop) {
                    listCloseDragInProgress = true
                    onDragStart()
                    val consumedY = onDrag(available.y)
                    return Offset(x = 0f, y = consumedY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }
                if (!listCloseDragInProgress && available.y > 0 && isListAtTop && listGestureStartedAtTop) {
                    listCloseDragInProgress = true
                    onDragStart()
                    val consumedY = onDrag(available.y)
                    return Offset(x = 0f, y = consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!listCloseDragInProgress) {
                    return Velocity.Zero
                }
                listCloseDragInProgress = false
                onDragEnd(available.y)
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!listCloseDragInProgress) {
                    return Velocity.Zero
                }
                listCloseDragInProgress = false
                onDragEnd(available.y)
                return Velocity.Zero
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(x = 0, y = drawerOffsetPx.roundToInt()) },
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        var velocityTracker = VelocityTracker()
                        detectVerticalDragGestures(
                            onDragStart = {
                                velocityTracker = VelocityTracker()
                                onDragStart()
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                velocityTracker.addPosition(change.uptimeMillis, change.position)
                                onDrag(dragAmount)
                            },
                            onDragEnd = {
                                onDragEnd(velocityTracker.calculateVelocity().y)
                            },
                            onDragCancel = {
                                onDragEnd(0f)
                            },
                        )
                    }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
                Text(
                    text = "App Drawer",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Swipe down on the handle or press back to return.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            listGestureStartedAtTop = isListAtTop
                            waitForUpOrCancellation()
                        }
                    }
                    .nestedScroll(listNestedScrollConnection),
                state = listState,
                contentPadding = PaddingValues(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = apps,
                    key = { "${it.packageName}/${it.activityName}" },
                ) { app ->
                    AppDrawerRow(
                        app = app,
                        onClick = {
                            onLaunchApp(app)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun GameShelfCard(
    game: RetroGame,
    isSelected: Boolean,
    cardWidth: androidx.compose.ui.unit.Dp,
    onSelect: (RetroGame) -> Unit,
) {
    val accentColor = game.accentRole.color()
    val accentOnColor = game.accentRole.onColor()
    val secondaryAccentColor = game.secondaryAccentRole.color()
    val secondaryAccentOnColor = game.secondaryAccentRole.onColor()
    val cardContentColor = MaterialTheme.colorScheme.onSurface
    val coverColor = if (isSelected) accentColor.copy(alpha = 0.9f) else secondaryAccentColor.copy(alpha = 0.88f)
    val coverContentColor = if (isSelected) accentOnColor else secondaryAccentOnColor

    Card(
        modifier = Modifier
            .width(cardWidth)
            .aspectRatio(1f)
            .focusable()
            .onFocusChanged { focusState ->
                if (focusState.hasFocus) {
                    onSelect(game)
                }
            }
            .clickable { onSelect(game) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = coverColor,
            contentColor = cardContentColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp,
        ),
        border = if (isSelected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
        } else {
            null
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(coverColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(secondaryAccentColor.copy(alpha = 0.22f))
            ) {
                Text(
                    text = game.logo,
                    style = MaterialTheme.typography.titleSmall,
                    color = coverContentColor.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp),
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                contentColor = cardContentColor,
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun SwipeHintBar(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )
        Text(
            text = "Swipe up for apps",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MetaPill(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun AppDrawerRow(
    app: LauncherApp,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            AppIcon(app = app)
        },
        headlineContent = {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
private fun AppIcon(app: LauncherApp) {
    val iconBitmap = remember(app.packageName, app.activityName) {
        app.icon?.toBitmap(width = 96, height = 96)?.asImageBitmap()
    }
    if (iconBitmap != null) {
        LauncherAppImage(bitmap = iconBitmap, label = app.label)
    } else {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = app.label.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun LauncherAppImage(bitmap: ImageBitmap, label: String) {
    Image(
        bitmap = bitmap,
        contentDescription = label,
        modifier = Modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop,
    )
}

private data class LauncherApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable?,
)

private fun loadLauncherApps(packageManager: PackageManager): List<LauncherApp> {
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
        .asSequence()
        .map { resolveInfo ->
            LauncherApp(
                label = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                activityName = resolveInfo.activityInfo.name,
                icon = resolveInfo.loadIcon(packageManager),
            )
        }
        .distinctBy { "${it.packageName}/${it.activityName}" }
        .sortedBy { it.label.lowercase() }
        .toList()
}

private fun launchAppOnCurrentDisplay(activity: Activity, app: LauncherApp) {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setClassName(app.packageName, app.activityName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val options = ActivityOptions.makeBasic().apply {
        setLaunchDisplayId(activity.display?.displayId ?: Display.DEFAULT_DISPLAY)
    }
    activity.startActivity(intent, options.toBundle())
}
