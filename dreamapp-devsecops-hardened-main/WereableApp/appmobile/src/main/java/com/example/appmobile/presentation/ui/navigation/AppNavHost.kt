package com.example.appmobile.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.appmobile.presentation.ui.screens.SignInScreen
import com.example.appmobile.presentation.ui.screens.UserScreen
import com.example.appmobile.presentation.ui.screens.ProfileScreen
import com.example.appmobile.presentation.ui.screens.SleepMonitorScreen
import com.example.appmobile.presentation.ui.screens.SleepHistoryScreen
import com.example.appmobile.presentation.viewmodel.SignInViewModel
import com.example.appmobile.presentation.viewmodel.SignInViewModelFactory
import com.example.appmobile.presentation.viewmodel.SleepMonitorViewModel
import com.example.appmobile.domain.usecase.GoogleAuthUiClient
import com.example.appmobile.domain.model.UserData
import com.example.appmobile.presentation.navigation.Routes

@Composable
fun AppNavHost(
    navController: NavHostController,
    googleAuthUiClient: GoogleAuthUiClient,
    signInViewModel: SignInViewModel,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
    onSignInClick: () -> Unit
) {
    val state = signInViewModel.state.collectAsState().value

    // Manejar el launcher en AppNavHost
    LaunchedEffect(state.signInIntentSender) {
        state.signInIntentSender?.let { intentSender ->
            launcher.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SIGN_IN
    ) {
        composable(Routes.SIGN_IN) {
            SignInScreen(
                state = state,
                onSignInClick = onSignInClick
            )

            LaunchedEffect(state.isSignInSuccessful, state.isLoading) {
                if (state.isSignInSuccessful && !state.isLoading) {
                    // Solo navega cuando termine la verificación del usuario
                    if (state.isNewUser) {
                        // Usuario no registrado - ir a UserScreen para registro
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.SIGN_IN) { inclusive = true }
                        }
                    } else {
                        // Usuario ya registrado - ir a ProfileScreen
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(Routes.SIGN_IN) { inclusive = true }
                        }
                    }
                }
            }
        }

        composable(Routes.REGISTER) {
            UserScreen(
                onUserRegistered = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                uid = state.uid  // Pasar el UID del estado de SignIn
            )
        }

        composable(Routes.PROFILE) {
            // Get current user data from Google Auth client and state
            val currentUser = googleAuthUiClient.getSignedInUser()
            val userData = if (currentUser != null) {
                UserData(
                    userId = currentUser.userId,
                    username = currentUser.username,
                    profilePictureUrl = currentUser.profilePictureUrl
                )
            } else {
                null
            }
            
            ProfileScreen(
                userData = userData,
                onSignOut = {
                    signInViewModel.resetState()
                    // Limpiar bases de datos al cerrar sesión
                    kotlinx.coroutines.runBlocking {
                        googleAuthUiClient.signOut()
                    }
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0)
                    }
                },
                onStartMonitoring = {
                    navController.navigate(Routes.MONITOR)
                },
                onNavigateToUserScreen = {
                    navController.navigate(Routes.USER_SCREEN)
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }
        
        composable(Routes.MONITOR) {
            val viewModel = viewModel<SleepMonitorViewModel>()
            SleepMonitorScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.HISTORY) {
            val viewModel = viewModel<SleepMonitorViewModel>()
            SleepHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.USER_SCREEN) {
            UserScreen(
                onUserRegistered = {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(Routes.USER_SCREEN) { inclusive = true }
                    }
                },
                uid = state.uid  // Pasar el UID del estado de SignIn
            )
        }
    }
}

