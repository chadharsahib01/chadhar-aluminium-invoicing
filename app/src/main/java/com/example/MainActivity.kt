package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.ClientsListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewDocumentScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.PreviewShareScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val settings by mainViewModel.settings.collectAsState()

            MyApplicationTheme(darkTheme = settings.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChadharAluminiumApp(mainViewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun ChadharAluminiumApp(mainViewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val settings by mainViewModel.settings.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250)) }
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    val nextRoute = if (settings.isPinEnabled) "pin_lock" else "home"
                    navController.navigate(nextRoute) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("pin_lock") {
            PinLockScreen(
                viewModel = mainViewModel,
                onUnlocked = {
                    navController.navigate("home") {
                        popUpTo("pin_lock") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = mainViewModel,
                onNavigateToNewDocument = { docType ->
                    navController.navigate("new_document/$docType")
                },
                onNavigateToEditDocument = { docId ->
                    navController.navigate("new_document/INVOICE")
                },
                onNavigateToPreview = {
                    navController.navigate("preview")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToClients = {
                    navController.navigate("clients_list")
                }
            )
        }

        composable("clients_list") {
            ClientsListScreen(
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectClient = { clientName ->
                    navController.navigate("client_detail/$clientName")
                }
            )
        }

        composable(
            route = "client_detail/{clientName}",
            arguments = listOf(navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            ClientDetailScreen(
                viewModel = mainViewModel,
                clientName = clientName,
                onNavigateBack = { navController.popBackStack() },
                onSelectDocument = { docId ->
                    mainViewModel.loadDocumentForEdit(docId)
                    navController.navigate("preview")
                }
            )
        }

        composable(
            route = "new_document/{docType}",
            arguments = listOf(navArgument("docType") { type = NavType.StringType })
        ) { backStackEntry ->
            val docType = backStackEntry.arguments?.getString("docType") ?: "INVOICE"
            NewDocumentScreen(
                viewModel = mainViewModel,
                docType = docType,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPreview = {
                    navController.navigate("preview")
                }
            )
        }

        composable("preview") {
            PreviewShareScreen(
                viewModel = mainViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = mainViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
