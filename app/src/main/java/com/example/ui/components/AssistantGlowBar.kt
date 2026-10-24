package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleRed
import com.example.ui.theme.GoogleYellow

enum class AssistantVisualState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun AssistantGlowBar(
    visualState: AssistantVisualState,
    modifier: Modifier = Modifier
) {
    val colors = listOf(GoogleBlue, GoogleRed, GoogleYellow, GoogleGreen)

    val transition = rememberInfiniteTransition(label = "assistant_anim")

    val pulsePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bottom ambient glow gradient when active
        if (visualState != AssistantVisualState.IDLE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                GoogleBlue.copy(alpha = 0.8f),
                                GoogleRed.copy(alpha = 0.8f),
                                GoogleYellow.copy(alpha = 0.8f),
                                GoogleGreen.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }

        // 4 Dynamic Pulsing Equalizer Bars / Dots
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEachIndexed { index, color ->
                val barHeight = when (visualState) {
                    AssistantVisualState.IDLE -> 10.dp
                    AssistantVisualState.LISTENING -> {
                        val factor = kotlin.math.sin((pulsePhase * 2 * Math.PI + index * 0.8).toFloat())
                        (14 + (factor + 1) * 12).dp
                    }
                    AssistantVisualState.THINKING -> {
                        val factor = kotlin.math.sin((pulsePhase * 2 * Math.PI + index * 1.5).toFloat())
                        (12 + (factor + 1) * 8).dp
                    }
                    AssistantVisualState.SPEAKING -> {
                        val factor = kotlin.math.abs(kotlin.math.sin((pulsePhase * 4 * Math.PI + index * 1.2).toFloat()))
                        (12 + factor * 22).dp
                    }
                }

                val barWidth = when (visualState) {
                    AssistantVisualState.IDLE -> 10.dp
                    else -> 9.dp
                }

                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(barHeight)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(color)
                )
            }
        }
    }
}
