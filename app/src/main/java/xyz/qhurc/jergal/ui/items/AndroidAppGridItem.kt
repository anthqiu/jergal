package xyz.qhurc.jergal.ui.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import xyz.qhurc.jergal.model.AppInfo
import xyz.qhurc.jergal.model.JC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun createAndroidAppGridItem(app: AppInfo) {
    val interactionSource = remember { MutableInteractionSource() }
    interactionSource.collectIsHoveredAsState()

//    val isHovered = remember { mutableStateOf(false) }
//
//    LaunchedEffect(interactionSource) {
//        interactionSource.interactions.collect { interaction ->
//            when (interaction) {
//                is FocusInteraction.Focus -> isHovered.value = true
//                is FocusInteraction.Unfocus -> isHovered.value = false
//            }
//        }
//    }

    Card(
        onClick = {},
        interactionSource = interactionSource,
        colors = JC.DEFAULT_CARD_COLORS
    ) {
        Column(
            modifier = Modifier
                .height(JC.ANDROID_APP_TILE_HEIGHT)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                modifier = Modifier
                    .size(JC.ANDROID_APP_ICON_SIZE)
            )
            Box(modifier = Modifier.height(JC.ANDROID_APP_ICON_LABEL_INTERVAL))
            Text(
                text = app.label,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}