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
import com.dagram.app.ui.profile.ProfileEditScreen
import com.dagram.app.ui.contacts.ContactsScreen
import com.dagram.app.ui.settings.SettingsScreen
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

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            wsManager.connect("http://134.255.234.123:3000/api/")
        }
    }

    if (!authState.isLoggedIn && !authState.isLoading) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {},
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {},
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
        }
    } else if (authState.isLoggedIn) {
        if (authState.isNewUser) {
            ProfileEditScreen(
                user = authState.currentUser,
                isFirstSetup = true,
                isLoading = authState.isLoading,
                error = authState.error,
                usernameAvailable = authState.usernameAvailable,
                isCheckingUsername = authState.isCheckingUsername,
                onCheckUsername = { authViewModel.checkUsernameAvailability(it) },
                onSave = { username, displayName, bio ->
                    authViewModel.updateProfile(username, displayName, bio)
                },
                onSkip = { authViewModel.completeProfileSetup() },
                onClearError = { authViewModel.clearError() }
            )
        } else {
            Scaffold(
                containerColor = Color(0xFF0A1628),
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val mainRoutes = listOf("chats", "contacts", "settings", "profile")
                    if (currentRoute in mainRoutes) {
                        NavigationBar(
                            containerColor = Color(0xFF0D1B2A),
                            contentColor = Color.White
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == "chats",
                                onClick = { navController.navigate("chats") { launchSingleTop = true } },
                                icon = {
                                    BadgedBox(badge = {}) {
                                        Icon(Icons.Filled.Forum, null)
                                    }
                                },
                                label = { Text("المحادثات") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2196F3),
                                    selectedTextColor = Color(0xFF2196F3),
                                    unselectedIconColor = Color(0xFF78909C),
                                    unselectedTextColor = Color(0xFF78909C),
                                    indicatorColor = Color(0xFF1565C0).copy(alpha = 0.3f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == "contacts",
                                onClick = { navController.navigate("contacts") { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Contacts, null) },
                                label = { Text("جهات الاتصال") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2196F3),
                                    selectedTextColor = Color(0xFF2196F3),
                                    unselectedIconColor = Color(0xFF78909C),
                                    unselectedTextColor = Color(0xFF78909C),
                                    indicatorColor = Color(0xFF1565C0).copy(alpha = 0.3f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == "settings",
                                onClick = { navController.navigate("settings") { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Settings, null) },
                                label = { Text("الإعدادات") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2196F3),
                                    selectedTextColor = Color(0xFF2196F3),
                                    unselectedIconColor = Color(0xFF78909C),
                                    unselectedTextColor = Color(0xFF78909C),
                                    indicatorColor = Color(0xFF1565C0).copy(alpha = 0.3f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == "profile",
                                onClick = { navController.navigate("profile") { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Person, null) },
                                label = { Text("الملف الشخصي") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2196F3),
                                    selectedTextColor = Color(0xFF2196F3),
                                    unselectedIconColor = Color(0xFF78909C),
                                    unselectedTextColor = Color(0xFF78909C),
                                    indicatorColor = Color(0xFF1565C0).copy(alpha = 0.3f)
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
                                navController.navigate("messages/$chatId?chatName=${java.net.URLEncoder.encode(chatName, "UTF-8")}")
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
                        val chatName = try {
                            java.net.URLDecoder.decode(
                                backStackEntry.arguments?.getString("chatName") ?: "Chat", "UTF-8"
                            )
                        } catch (e: Exception) {
                            backStackEntry.arguments?.getString("chatName") ?: "Chat"
                        }
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
                                navController.navigate("messages/$chatId?chatName=${java.net.URLEncoder.encode(name, "UTF-8")}") {
                                    popUpTo("chats")
                                }
                            }
                        )
                    }
                    composable("create_channel") {
                        CreateChannelScreen(
                            onBack = { navController.popBackStack() },
                            onChannelCreated = { chatId, name ->
                                navController.navigate("messages/$chatId?chatName=${java.net.URLEncoder.encode(name, "UTF-8")}") {
                                    popUpTo("chats")
                                }
                            }
                        )
                    }
                    composable("contacts") {
                        ContactsScreen(
                            onUserClick = { userId, name ->
                                navController.navigate("messages/$userId?chatName=${java.net.URLEncoder.encode(name, "UTF-8")}")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen()
                    }
                    composable("profile") {
                        ProfileScreen(
                            user = authState.currentUser,
                            isLoading = authState.isLoading,
                            error = authState.error,
                            usernameAvailable = authState.usernameAvailable,
                            isCheckingUsername = authState.isCheckingUsername,
                            profileUpdateSuccess = authState.profileUpdateSuccess,
                            onCheckUsername = { authViewModel.checkUsernameAvailability(it) },
                            onUpdateProfile = { username, displayName, bio ->
                                authViewModel.updateProfile(username, displayName, bio)
                            },
                            onClearError = { authViewModel.clearError() },
                            onClearSuccess = { authViewModel.clearProfileUpdateSuccess() },
                            onLogout = {
                                wsManager.disconnect()
                                authViewModel.logout()
                            }
                        )
                    }
                }
            }
        }
    }
}
