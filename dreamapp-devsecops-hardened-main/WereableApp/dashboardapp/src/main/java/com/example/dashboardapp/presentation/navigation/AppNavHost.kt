package com.example.dashboardapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dashboardapp.data.local.session.SessionManager
import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.presentation.ui.screens.login.LoginScreen
import com.example.dashboardapp.presentation.ui.screens.dashboard.DashboardScreen
import com.example.dashboardapp.presentation.ui.screens.register.RegisterScreen
import com.example.dashboardapp.presentation.ui.screens.stats.StatsScreen
import com.example.dashboardapp.presentation.viewmodel.auth.LoginViewModel
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val REGISTER = "register"
    const val STATS = "stats"
    fun statsWithUser(userJson: String): String = "$STATS/$userJson"
}

@Composable
fun AppNavHost(navController: NavHostController, sessionManager: SessionManager) {
    val isLoggedIn = remember { sessionManager.isLoggedIn() }
    val startDestination = if (isLoggedIn) Routes.DASHBOARD else Routes.LOGIN
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { navController.navigate(Routes.DASHBOARD) { popUpTo(0) } },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(0)
                    }
                },
                onLoginClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onLogout = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0)
                }
            },
            onUserClick = { user ->
                val userJson = URLEncoder.encode(Gson().toJson(user), StandardCharsets.UTF_8.name())
                navController.navigate(Routes.statsWithUser(userJson))
            })
        }

        composable(
            "${Routes.STATS}/{user}",
            arguments = listOf(navArgument("user") { type = NavType.StringType })
        ) { backStackEntry ->
            val userJson = backStackEntry.arguments?.getString("user")
            val user = Gson().fromJson(userJson, User::class.java)
            StatsScreen(user = user, navController = navController)
        }
    }
}
