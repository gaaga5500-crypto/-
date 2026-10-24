package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.screens.SnapshotScreen
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssistantViewModel

enum class AppScreen {
    ASSISTANT,
    ROUTINES,
    DEVICES,
    SNAPSHOT
}

class MainActivity : ComponentActivity() {

    private val assistantViewModel: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = assistantViewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: AssistantViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.ASSISTANT) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    // Handle system back navigation from sub-screens to Home (Assistant)
    if (currentScreen != AppScreen.ASSISTANT) {
        BackHandler {
            currentScreen = AppScreen.ASSISTANT
        }
    }

    val currentLanguage by viewModel.currentLanguage.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.ASSISTANT,
                    onClick = { currentScreen = AppScreen.ASSISTANT },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.ASSISTANT) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = if (currentLanguage == "ar") "المساعد" else "Assistant"
                        )
                    },
                    label = { Text(if (currentLanguage == "ar") "المساعد" else "Assistant") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoogleBlue,
                        indicatorColor = GoogleBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_assistant")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.ROUTINES,
                    onClick = { currentScreen = AppScreen.ROUTINES },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.ROUTINES) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                            contentDescription = if (currentLanguage == "ar") "الروتينات" else "Routines"
                        )
                    },
                    label = { Text(if (currentLanguage == "ar") "الروتينات" else "Routines") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoogleBlue,
                        indicatorColor = GoogleBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_routines")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.DEVICES,
                    onClick = { currentScreen = AppScreen.DEVICES },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.DEVICES) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = if (currentLanguage == "ar") "المنزل" else "Home"
                        )
                    },
                    label = { Text(if (currentLanguage == "ar") "المنزل" else "Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoogleBlue,
                        indicatorColor = GoogleBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_devices")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SNAPSHOT,
                    onClick = { currentScreen = AppScreen.SNAPSHOT },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.SNAPSHOT) Icons.Filled.Today else Icons.Outlined.Today,
                            contentDescription = if (currentLanguage == "ar") "اليوم" else "Today"
                        )
                    },
                    label = { Text(if (currentLanguage == "ar") "اليوم" else "Today") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoogleBlue,
                        indicatorColor = GoogleBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_snapshot")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.ASSISTANT -> {
                    AssistantScreen(
                        viewModel = viewModel,
                        hasMicPermission = hasMicPermission,
                        onRequestMicPermission = {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }
                AppScreen.ROUTINES -> {
                    RoutinesScreen(
                        viewModel = viewModel,
                        onNavigateToChat = {
                            currentScreen = AppScreen.ASSISTANT
                        }
                    )
                }
                AppScreen.DEVICES -> {
                    DevicesScreen(
                        viewModel = viewModel
                    )
                }
                AppScreen.SNAPSHOT -> {
                    SnapshotScreen(
                        viewModel = viewModel,
                        onNavigateToChatWithPrompt = { prompt ->
                            currentScreen = AppScreen.ASSISTANT
                            viewModel.sendMessage(prompt)
                        }
                    )
                }
            }
        }
    }
}
