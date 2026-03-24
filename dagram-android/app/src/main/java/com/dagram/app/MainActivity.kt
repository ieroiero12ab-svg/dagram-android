package com.dagram.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.dagram.app.ui.auth.*
import com.dagram.app.ui.chats.ChatsScreen
import com.dagram.app.ui.channels.CreateChannelScreen
import com.dagram.app.ui.groups.CreateGroupScreen
import com.dagram.app.ui.messages.MessagesScreen
import com.dagram.app.ui.profile.ProfileScreen
import com.dagram.app.ui.theme.DagramTheme
import com.dagram.app.utils.WebSocketManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DagramTheme {
                DagramApp(webSocketManager)
            }
        }
    }
}

@Composable
fun DagramApp(wsManager: WebSocketManager) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(0) }

    val showBottomNav = authState.isLoggedIn &&
            navController.currentBackStackEntryAsState().value?.destination?.route in listOf("chats", "profile")

    if (!authState.isLoggedIn && !authState.isLoading) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        authState.currentUser?.let { user ->
                            wsManager.connect("https://YOUR_VPS_IP_OR_DOMAIN/api/")
                        }
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
        }
    } else if (authState.isLoggedIn) {
        Scaffold(
            containerColor = Color(0xFF0F1923),
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                if (currentRoute in listOf("chats", "profile")) {
                    NavigationBar(
                        containerColor = Color(0xFF162635),
                        contentColor = Color.White
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == "chats",
                            onClick = { navController.navigate("chats") { launchSingleTop = true } },
                            icon = { Icon(Icons.Filled.Forum, null) },
                            label = { Text("Chats") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2196F3),
                                selectedTextColor = Color(0xFF2196F3),
                                unselectedIconColor = Color(0xFF78909C),
                                unselectedTextColor = Color(0xFF78909C),
                                indicatorColor = Color(0xFF0D1B2A)
                            )
                        )
                        NavigationBarItem(
                            selected = currentRoute == "profile",
                            onClick = { navController.navigate("profile") { launchSingleTop = true } },
                            icon = { Icon(Icons.Filled.Person, null) },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2196F3),
                                selectedTextColor = Color(0xFF2196F3),
                                unselectedIconColor = Color(0xFF78909C),
                                unselectedTextColor = Color(0xFF78909C),
                                indicatorColor = Color(0xFF0D1B2A)
                            )
                        )
                    }
                }
            }
        ) { _ ->
            NavHost(navController = navController, startDestination = "chats") {
                composable("chats") {
                    ChatsScreen(
                        onChatClick = { chatId, chatName ->
                            navController.navigate("messages/$chatId?chatName=${chatName}")
                        },
                        onCreateGroup = { navController.navigate("create_group") },
                        onCreateChannel = { navController.navigate("create_channel") }
                    )
                }
                composable(
                    "messages/{chatId}?chatName={chatName}",
                    arguments = listOf(
                        navArgument("chatId") { type = NavType.IntType },
                        navArgument("chatName") { type = NavType.StringType; defaultValue = "Chat" }
                    )
                ) { backStackEntry ->
                    val chatName = backStackEntry.arguments?.getString("chatName") ?: "Chat"
                    MessagesScreen(
                        chatName = chatName,
                        currentUserId = authState.currentUser?.id ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("create_group") {
                    CreateGroupScreen(
                        onBack = { navController.popBackStack() },
                        onGroupCreated = { chatId, name ->
                            navController.navigate("messages/$chatId?chatName=$name") {
                                popUpTo("chats")
                            }
                        }
                    )
                }
                composable("create_channel") {
                    CreateChannelScreen(
                        onBack = { navController.popBackStack() },
                        onChannelCreated = { chatId, name ->
                            navController.navigate("messages/$chatId?chatName=$name") {
                                popUpTo("chats")
                            }
                        }
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        user = authState.currentUser,
                        onLogout = {
                            wsManager.disconnect()
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
