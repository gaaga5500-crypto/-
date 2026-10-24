package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceType
import com.example.data.model.SmartDevice
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleYellow

@Composable
fun SmartDeviceCard(
    device: SmartDevice,
    onToggle: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isOn) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (device.type) {
                        DeviceType.LIGHT -> Icons.Default.Lightbulb
                        DeviceType.THERMOSTAT -> Icons.Default.Thermostat
                        DeviceType.SPEAKER -> Icons.Default.Speaker
                        DeviceType.PLUG -> Icons.Default.Power
                    }
                    val iconTint = if (device.isOn) {
                        when (device.type) {
                            DeviceType.LIGHT -> GoogleYellow
                            DeviceType.THERMOSTAT -> GoogleGreen
                            DeviceType.SPEAKER -> GoogleBlue
                            DeviceType.PLUG -> GoogleGreen
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = device.name,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${device.room} • ${if (device.isOn) "يعمل" else "متوقف"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = device.isOn,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoogleBlue,
                        checkedTrackColor = GoogleBlue.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("switch_${device.id}")
                )
            }

            if (device.isOn) {
                Spacer(modifier = Modifier.height(12.dp))

                when (device.type) {
                    DeviceType.LIGHT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "مستوى الإضاءة: ${device.value}٪",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Slider(
                            value = device.value.toFloat(),
                            onValueChange = { onValueChange(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = GoogleYellow,
                                activeTrackColor = GoogleYellow
                            ),
                            modifier = Modifier.testTag("slider_${device.id}")
                        )
                    }

                    DeviceType.THERMOSTAT -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المكيف: ${device.value}° م",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoogleGreen
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalIconButton(
                                    onClick = { onValueChange(device.value - 1) },
                                    modifier = Modifier.testTag("temp_down_${device.id}")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease temp")
                                }

                                FilledTonalIconButton(
                                    onClick = { onValueChange(device.value + 1) },
                                    modifier = Modifier.testTag("temp_up_${device.id}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase temp")
                                }
                            }
                        }
                    }

                    DeviceType.SPEAKER -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "مستوى الصوت: ${device.value}٪",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Slider(
                            value = device.value.toFloat(),
                            onValueChange = { onValueChange(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = GoogleBlue,
                                activeTrackColor = GoogleBlue
                            ),
                            modifier = Modifier.testTag("volume_${device.id}")
                        )
                    }

                    DeviceType.PLUG -> {
                        Text(
                            text = if (device.isOn) "استهلاك 45 واط • يعمل بنشاط" else "وضع الاستعداد",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
