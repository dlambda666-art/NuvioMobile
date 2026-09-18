package com.nuvio.app.features.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.home.MetaPreview

@Composable
internal fun FrenchPulseBadges(
    item: MetaPreview,
    modifier: Modifier = Modifier,
) {
    val status = item.frenchPulseStatus
    val showVf = item.frenchPulseVf && status == null
    val showQuality = !item.frenchPulseQuality.isNullOrBlank()
    if (status == null && !showVf && !showQuality) return

    Row(
        modifier = modifier.padding(start = 4.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (status) {
            "nouveaute_vf" -> PulseBadge(
                text = "Nouveauté VF",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            "a_surveiller" -> PulseBadge(
                text = "À surveiller",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        if (showVf) {
            PulseBadge(
                text = "VF",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        if (showQuality) {
            PulseBadge(
                text = item.frenchPulseQuality!!,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PulseBadge(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
    }
}
