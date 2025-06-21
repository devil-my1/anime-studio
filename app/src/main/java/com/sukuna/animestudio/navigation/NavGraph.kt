package com.sukuna.animestudio.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sukuna.animestudio.domain.RoleManager
import com.sukuna.animestudio.presentation.admin.AdminPanelScreen
import com.sukuna.animestudio.presentation.auth.AuthScreen
import com.sukuna.animestudio.presentation.detail.AnimeDetailScreen
import com.sukuna.animestudio.presentation.home.HomeScreen
import com.sukuna.animestudio.presentation.profile.ProfileScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object AdminPanel : Screen("admin_panel")
    object AnimeDetail : Screen("anime_detail/{animeId}") {
        fun createRoute(animeId: String) = "anime_detail/$animeId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route
) {
    val roleManager = remember { RoleManager() }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToAdminPanel = {
                    navController.navigate(Screen.AdminPanel.route)
                },
                onNavigateToAnimeDetail = { animeId ->
                    navController.navigate(Screen.AnimeDetail.createRoute(animeId))
                },
                roleManager = roleManager
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                roleManager = roleManager
            )
        }

        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AnimeDetail.route,
            arguments = listOf(
                navArgument("animeId") {
                    type = NavType.StringType
                }
            )
        ) {
            AnimeDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
} 