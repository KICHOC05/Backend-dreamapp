package com.example.dashboardapp.presentation.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dashboardapp.R
import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.presentation.ui.screens.users.UsersListScreen
import com.example.dashboardapp.presentation.ui.screens.map.MapScreen
import com.example.dashboardapp.presentation.viewmodel.UserListViewModel
import com.example.dashboardapp.presentation.viewmodel.auth.LogoutViewModel
import com.example.dashboardapp.presentation.viewmodel.SleepStateViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onUserClick: (User) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val userListViewModel: UserListViewModel = hiltViewModel()
    val logoutViewModel: LogoutViewModel = hiltViewModel()
    val sleepStateViewModel: SleepStateViewModel = hiltViewModel()
    
    val logoutState by logoutViewModel.uiState.collectAsState()

    // Connect to web socket
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(true) {
        userListViewModel.connectWebSocket()
        sleepStateViewModel.connectWebSocket()
        coroutineScope.launch {
            userListViewModel.notifyUpdate()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            userListViewModel.disconnectWebSocket()
            sleepStateViewModel.disconnectWebSocket()
        }
    }

    LaunchedEffect(logoutState.success) {
        if (logoutState.success) {
            onLogout()
        }
    }

    val tabItems = listOf(
        Triple("Usuarios", Icons.Default.People, 0),
        Triple("Mapa", Icons.Default.Map, 1)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.icon),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "NOX",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                        },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                modifier = Modifier.width(200.dp),
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    expanded = false
                                    showLogoutDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Cerrar sesión"
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabItems.forEachIndexed { index, (title, icon, _) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Default.Assistant,
                    contentDescription = "Abrir Chatbot"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> UsersListScreen(onUserClick = onUserClick)
                1 -> MapScreen()
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("¿Cerrar sesión?") },
                text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        logoutViewModel.logout()
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}