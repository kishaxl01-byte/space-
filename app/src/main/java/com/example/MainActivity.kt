package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CelestialBody
import com.example.model.CosmosData
import com.example.service.GeminiService
import com.example.ui.components.ScaleComparisonView
import com.example.ui.components.StarfieldCanvas
import com.example.ui.screens.AstroGuideChatScreen
import com.example.ui.screens.BookHomeScreen
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.NasaTimelineScreen
import com.example.ui.screens.PlanetaryOrreryScreen
import com.example.ui.theme.MyApplicationTheme

enum class CosmosTab(val title: String) {
    BOOK("Book"),
    ORRERY("3D Orrery"),
    SCALE("Scale"),
    NASA("NASA"),
    CHAT("AI Guide")
}

class MainActivity : ComponentActivity() {
    private val geminiService by lazy { GeminiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CosmosApp(geminiService = geminiService)
            }
        }
    }
}

@Composable
fun CosmosApp(geminiService: GeminiService) {
    var currentTab by remember { mutableStateOf(CosmosTab.BOOK) }
    var selectedBody by remember { mutableStateOf<CelestialBody?>(null) }
    var bookmarkedIds by remember { mutableStateOf(setOf("earth", "saturn", "jwst", "sagittarius_a")) }
    var chatInitialPrompt by remember { mutableStateOf<String?>(null) }

    // If detail is open, back button returns to tab
    BackHandler(enabled = selectedBody != null) {
        selectedBody = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF070913),
        bottomBar = {
            if (selectedBody == null) {
                NavigationBar(
                    containerColor = Color(0xEE0F172A),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == CosmosTab.BOOK,
                        onClick = { currentTab = CosmosTab.BOOK },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Book Chapters",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(CosmosTab.BOOK.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_book")
                    )

                    NavigationBarItem(
                        selected = currentTab == CosmosTab.ORRERY,
                        onClick = { currentTab = CosmosTab.ORRERY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "3D Orrery",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(CosmosTab.ORRERY.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_orrery")
                    )

                    NavigationBarItem(
                        selected = currentTab == CosmosTab.SCALE,
                        onClick = { currentTab = CosmosTab.SCALE },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Scale Comparison",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(CosmosTab.SCALE.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_scale")
                    )

                    NavigationBarItem(
                        selected = currentTab == CosmosTab.NASA,
                        onClick = { currentTab = CosmosTab.NASA },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "NASA Timeline",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(CosmosTab.NASA.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_nasa")
                    )

                    NavigationBarItem(
                        selected = currentTab == CosmosTab.CHAT,
                        onClick = { currentTab = CosmosTab.CHAT },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AstroGuide AI",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(CosmosTab.CHAT.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_chat")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedBody,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "bodyDetailNavigation"
            ) { targetBody ->
                if (targetBody != null) {
                    ChapterDetailScreen(
                        body = targetBody,
                        isBookmarked = bookmarkedIds.contains(targetBody.id),
                        onToggleBookmark = {
                            bookmarkedIds = if (bookmarkedIds.contains(targetBody.id)) {
                                bookmarkedIds - targetBody.id
                            } else {
                                bookmarkedIds + targetBody.id
                            }
                        },
                        onBack = { selectedBody = null },
                        onAskAI = { prompt ->
                            selectedBody = null
                            chatInitialPrompt = prompt
                            currentTab = CosmosTab.CHAT
                        }
                    )
                } else {
                    when (currentTab) {
                        CosmosTab.BOOK -> BookHomeScreen(
                            bookmarkedIds = bookmarkedIds,
                            onToggleBookmark = { id ->
                                bookmarkedIds = if (bookmarkedIds.contains(id)) {
                                    bookmarkedIds - id
                                } else {
                                    bookmarkedIds + id
                                }
                            },
                            onSelectBody = { selectedBody = it },
                            onOpenChatWithPrompt = { prompt ->
                                chatInitialPrompt = prompt
                                currentTab = CosmosTab.CHAT
                            }
                        )
                        CosmosTab.ORRERY -> PlanetaryOrreryScreen(
                            onSelectBody = { selectedBody = it }
                        )
                        CosmosTab.SCALE -> ScaleComparisonView()
                        CosmosTab.NASA -> NasaTimelineScreen()
                        CosmosTab.CHAT -> AstroGuideChatScreen(
                            geminiService = geminiService,
                            initialPrompt = chatInitialPrompt
                        )
                    }
                }
            }
        }
    }
}

