package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleRed
import com.example.ui.theme.GoogleYellow

data class SuggestionItem(
    val label: String,
    val command: String,
    val icon: ImageVector
)

val defaultArabicSuggestions = listOf(
    SuggestionItem("صباح الخير", "صباح الخير", Icons.Default.WbSunny),
    SuggestionItem("شغّل الإضاءة", "شغل إضاءة غرفة المعيشة", Icons.Default.Lightbulb),
    SuggestionItem("مؤقت 5 دقائق", "اضبط مؤقت 5 دقائق", Icons.Default.HourglassBottom),
    SuggestionItem("طقس اليوم", "كيف حالة الطقس اليوم؟", Icons.Default.WbSunny),
    SuggestionItem("أخبرني نكتة", "أخبرني نكتة مضحكة", Icons.Default.SentimentSatisfiedAlt),
    SuggestionItem("صانعة القهوة", "شغل صانعة القهوة في المطبخ", Icons.Default.Coffee),
    SuggestionItem("حالة المرور", "كيف هي حالة المرور اليوم؟", Icons.Default.DirectionsCar),
    SuggestionItem("روتين النوم", "روتين وقت النوم", Icons.Default.Bedtime)
)

@Composable
fun SuggestionChipRow(
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<SuggestionItem> = defaultArabicSuggestions
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEachIndexed { index, item ->
            val iconTint = when (index % 4) {
                0 -> GoogleBlue
                1 -> GoogleRed
                2 -> GoogleYellow
                else -> GoogleGreen
            }

            SuggestionChip(
                onClick = { onSelectSuggestion(item.command) },
                label = { Text(item.label) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier.testTag("suggestion_chip_${index}")
            )
        }
    }
}
