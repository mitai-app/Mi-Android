package io.vonley.mi.ui.screens.home.presentation.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import io.vonley.mi.di.network.JbLog
import io.vonley.mi.models.bg

@Composable
fun LogCard(log: JbLog, modifier: Modifier = Modifier) {
    val background = log.cmd?.bg ?: MaterialTheme.colorScheme.surfaceVariant
    val content = if (background.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = background, contentColor = content),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            val device = log.device
            if (log.cmd != null && device != null) {
                Text(
                    "${device.device} (${device.version}): ${device.ip}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text(log.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
