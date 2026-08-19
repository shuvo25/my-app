package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddStreamDialog
import com.example.ui.components.AuthDialog
import com.example.ui.components.DownloadQualityDialog
import com.example.ui.screens.*
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StreamVaultApp()
                }
            }
        }
    }
}

@Composable
fun StreamVaultApp(
    viewModel: MediaViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedDetailVideo by viewModel.selectedDetailVideo.collectAsState()
    val downloadModalVideo by viewModel.downloadModalVideo.collectAsState()
    val isAddStreamDialogVisible by viewModel.isAddStreamDialogVisible.collectAsState()
    val isAuthDialogVisible by viewModel.isAuthDialogVisible.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()


    // Handle back button when player is open
    BackHandler(enabled = selectedDetailVideo != null) {
        viewModel.closeVideoDetail()
    }

    Scaffold(
        bottomBar = {
            if (selectedDetailVideo == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AmberPrimary,
                    tonalElevation = 8.dp
                ) {
                    // Home Tab
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.HOME,
                        onClick = { viewModel.setNavTab(AppNavTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberPrimary,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary.copy(alpha = 0.15f)
                        )
                    )

                    // Search Tab
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.SEARCH,
                        onClick = { viewModel.setNavTab(AppNavTab.SEARCH) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                contentDescription = "Search"
                            )
                        },
                        label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberPrimary,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary.copy(alpha = 0.15f)
                        )
                    )

                    // Downloads Tab with badge
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.DOWNLOADS,
                        onClick = { viewModel.setNavTab(AppNavTab.DOWNLOADS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (activeDownloads.isNotEmpty()) {
                                        Badge(
                                            containerColor = AmberPrimary,
                                            contentColor = Color.Black
                                        ) {
                                            Text(
                                                text = "${activeDownloads.size}",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == AppNavTab.DOWNLOADS) Icons.Filled.DownloadForOffline else Icons.Outlined.Download,
                                    contentDescription = "Downloads"
                                )
                            }
                        },
                        label = { Text("Downloads", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberPrimary,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary.copy(alpha = 0.15f)
                        )
                    )

                    // Saved / Library Tab
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.SAVED,
                        onClick = { viewModel.setNavTab(AppNavTab.SAVED) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Library"
                            )
                        },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberPrimary,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen Tabs
            when (currentTab) {
                AppNavTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onVideoClick = { viewModel.openVideoDetail(it) },
                    onDownloadClick = { viewModel.openDownloadModal(it) }
                )
                AppNavTab.SEARCH -> SearchScreen(
                    viewModel = viewModel,
                    onVideoClick = { viewModel.openVideoDetail(it) },
                    onDownloadClick = { viewModel.openDownloadModal(it) }
                )
                AppNavTab.DOWNLOADS -> DownloadsScreen(
                    viewModel = viewModel
                )
                AppNavTab.SAVED -> SavedScreen(
                    viewModel = viewModel,
                    onVideoClick = { viewModel.openVideoDetail(it) },
                    onDownloadClick = { viewModel.openDownloadModal(it) }
                )
            }

            // Detail Player Screen Transition
            AnimatedVisibility(
                visible = selectedDetailVideo != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                selectedDetailVideo?.let { video ->
                    PlayerScreen(
                        viewModel = viewModel,
                        video = video,
                        onBack = { viewModel.closeVideoDetail() }
                    )
                }
            }

            // Download Quality Selection Modal
            downloadModalVideo?.let { video ->
                DownloadQualityDialog(
                    video = video,
                    onConfirmDownload = { v, q -> viewModel.startDownload(v, q) },
                    onDismiss = { viewModel.closeDownloadModal() }
                )
            }

            // Add Stream URL Dialog
            if (isAddStreamDialogVisible) {
                AddStreamDialog(
                    onDismiss = { viewModel.hideAddStreamDialog() },
                    onAddStream = { title, url, cat ->
                        viewModel.addCustomStream(title, url, cat)
                    }
                )
            }

            // Supabase Authentication & Profile Modal
            if (isAuthDialogVisible) {
                AuthDialog(
                    currentUser = currentUser,
                    authState = authState,
                    onDismiss = { viewModel.hideAuthDialog() },
                    onSignIn = { email, pass -> viewModel.signIn(email, pass) },
                    onSignUp = { email, pass, name -> viewModel.signUp(email, pass, name) },
                    onDemoLogin = { viewModel.loginAsDemoUser() },
                    onSignOut = { viewModel.signOut() }
                )
            }
        }
    }
}
