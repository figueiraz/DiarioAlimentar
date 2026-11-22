package com.example.diarioalimentar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.diarioalimentar.ui.HomeScreen
import com.example.diarioalimentar.ui.MainViewModel
import com.example.diarioalimentar.ui.navigation.AppDrawerContent
import com.example.diarioalimentar.ui.navigation.AppScreen
import com.example.diarioalimentar.ui.screens.MetricsScreen
import com.example.diarioalimentar.ui.screens.ProfileScreen
import com.example.diarioalimentar.ui.theme.DiarioAlimentarTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiarioAlimentarTheme {
                MainAppNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(AppScreen.DAILY_MEALS) }
    val uiState by viewModel.uiState.collectAsState()

    // Volta para o Diário se estiver em outra tela ou fecha o Drawer com o botão voltar
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.DAILY_MEALS) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentScreen = AppScreen.DAILY_MEALS
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                userProfile = uiState.userProfile,
                onScreenSelected = { screen ->
                    currentScreen = screen
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        when (currentScreen) {
            AppScreen.DAILY_MEALS -> HomeScreen(
                viewModel = viewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onNavigateToProfile = { currentScreen = AppScreen.PROFILE }
            )

            AppScreen.PROFILE -> ProfileScreen(
                viewModel = viewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )

            AppScreen.METRICS -> MetricsScreen(
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
    }
}
