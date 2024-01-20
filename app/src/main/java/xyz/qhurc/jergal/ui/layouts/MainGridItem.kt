package xyz.qhurc.jergal.ui.layouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.qhurc.jergal.model.Title

@Composable
fun createItem(it: Title, topPadding: Boolean = false, bottomPadding: Boolean = false) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if (bottomPadding) Modifier.navigationBarsPadding()
                else Modifier
            )
            .then(
                if (topPadding) Modifier.padding(top = 8.dp)
                else Modifier
            )
            .clickable {

            },
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row {
            Text(
                text = it.name,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}