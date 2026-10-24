package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AssistantGlowBar
import com.example.ui.components.AssistantVisualState
import com.example.ui.components.MessageBubble
import com.example.ui.components.SuggestionChipRow
import com.example.ui.components.TimerWidgetCard
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleRed
import com.example.ui.theme.GoogleYellow
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    onRequestMicPermission: () -> Unit,
    hasMicPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val statusNotice by viewModel.statusNotice.collectAsState()

    val timerActive by viewModel.timerActiveInWidget.collectAsState()
    val timerRemaining by viewModel.timerRemainingSeconds.collectAsState()
    val timerTotal by viewModel.timerTotalSeconds.collectAsState()
    val timerIsRunning by viewModel.timerIsRunning.collectAsState()

    val visualState = viewModel.getVisualState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size, isThinking, isListening) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val currentLanguage by viewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Google 4-color dot emblem
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(GoogleBlue))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(GoogleRed))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(GoogleYellow))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(GoogleGreen))
                        }
                        Column {
                            Text(
                                text = if (currentLanguage == "ar") "المساعد الذكي" else "Assistant AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (visualState) {
                                    AssistantVisualState.LISTENING -> if (currentLanguage == "ar") "جارٍ الاستماع..." else "Listening..."
                                    AssistantVisualState.THINKING -> if (currentLanguage == "ar") "يفكر المساعد..." else "Thinking..."
                                    AssistantVisualState.SPEAKING -> if (currentLanguage == "ar") "يتحدث المساعد..." else "Speaking..."
                                    else -> if (currentLanguage == "ar") "جاهز للمساعدة" else "Ready"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher Button (عربي / EN)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoogleBlue.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        TextButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("language_toggle_button")
                        ) {
                            Text(
                                text = if (currentLanguage == "ar") "عربي 🇸🇦" else "EN 🇺🇸",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoogleBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.testTag("mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute voice" else "Mute voice",
                            tint = if (isMuted) GoogleRed else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear chat"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Optional Active Timer widget pin
            AnimatedVisibility(visible = timerActive && timerRemaining > 0) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    TimerWidgetCard(
                        remainingSeconds = timerRemaining,
                        totalSeconds = timerTotal,
                        isRunning = timerIsRunning,
                        onToggleRunning = { viewModel.toggleTimerRunning() },
                        onReset = { viewModel.resetTimer() }
                    )
                }
            }

            // Status error notice if any
            AnimatedVisibility(visible = statusNotice != null) {
                statusNotice?.let { notice ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = notice,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        onSpeak = { text -> viewModel.speakText(text) }
                    )
                }

                // Live Transcription indicator while user speaks
                if (isListening) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = GoogleBlue.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Live voice waveform",
                                    tint = GoogleBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (partialTranscript.isNotBlank()) partialTranscript else (if (currentLanguage == "ar") "جارٍ الاستماع إلى صوتك..." else "Listening to your voice..."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontStyle = if (partialTranscript.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                )
                            }
                        }
                    }
                }

                // Thinking / Assistant typing indicator
                if (isThinking) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generating",
                                tint = GoogleBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (currentLanguage == "ar") "يفكر المساعد..." else "Assistant is thinking...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Quick suggestion chips
            SuggestionChipRow(
                onSelectSuggestion = { cmd ->
                    viewModel.sendMessage(cmd)
                }
            )

            // Iconic Google Assistant 4-Color Glowing Light Bar
            AssistantGlowBar(visualState = visualState)

            // Input Dock (Voice Trigger & Text Input)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(if (currentLanguage == "ar") "تحدث أو اكتب للمساعد الذكي..." else "Ask Assistant or give a command...")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(26.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoogleBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )
                    )

                    if (inputText.isNotBlank()) {
                        FilledIconButton(
                            onClick = {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = GoogleBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
                        }
                    } else {
                        // Google Assistant Voice Orb FAB
                        val micBg = if (isListening) GoogleRed else GoogleBlue
                        FilledIconButton(
                            onClick = {
                                if (!hasMicPermission) {
                                    onRequestMicPermission()
                                } else {
                                    if (isListening) {
                                        viewModel.stopListening()
                                    } else {
                                        viewModel.startListening()
                                    }
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = micBg,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mic_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop voice listening" else "Start voice assistant"
                            )
                        }
                    }
                }
            }
        }
    }
}
