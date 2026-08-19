package com.example.dashboardapp.presentation.ui.screens.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.presentation.ui.components.ListUsers
import com.example.dashboardapp.presentation.viewmodel.UserListViewModel
import com.example.dashboardapp.presentation.viewmodel.SleepStateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersListScreen(
    onUserClick: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val userListViewModel: UserListViewModel = hiltViewModel()
    val sleepStateViewModel: SleepStateViewModel = hiltViewModel()
    
    val users by userListViewModel.users.collectAsState()
    val isLoading by userListViewModel.isLoading.collectAsState()
    val error by userListViewModel.error.collectAsState()
    val sleepStates by sleepStateViewModel.sleepStates.collectAsState()

    val filteredUsers = if (searchQuery.isBlank()) {
        users
    } else {
        users.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar pacientes") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            error != null -> Text("Error: $error")
            filteredUsers.isEmpty() -> Text("No hay usuarios disponibles.")
            else -> {
                LazyColumn {
                    items(filteredUsers) { user ->
                        val sleepState = sleepStates[user.id]
                        val isUserConnected = sleepState != null
                        
                        ListUsers(
                            userName = user.name,
                            profilePictureURL = user.pictureUrl,
                            active = isUserConnected,
                            onClick = { onUserClick(user) },
                            sleepState = if (isUserConnected) {
                                sleepState?.sleepStateDisplay
                            } else {
                                "Desconectado"
                            }
                        )
                    }
                }
            }
        }
    }
}
