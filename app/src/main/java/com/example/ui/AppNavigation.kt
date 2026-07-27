package com.example.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.GemDataStore
import com.example.model.GameStatus
import com.example.viewmodel.GameViewModel
import com.example.ads.RewardedAdManager
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel()
    val context = LocalContext.current
    val gemDataStore = remember { GemDataStore(context) }
    val rewardedAdManager = remember { RewardedAdManager(context).apply { loadAd() } }
    
    val gemCount by gemDataStore.gemCount.collectAsState(initial = 0)
    val gameState by gameViewModel.gameState.collectAsState()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onFinish = {
                navController.navigate("menu") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        
        composable("menu") {
            MainMenuScreen(
                gemCount = gemCount,
                onPlayClick = {
                    gameViewModel.startNewGame()
                    navController.navigate("game")
                }
            )
        }
        
        composable("game") {
            GameScreen(
                gameViewModel = gameViewModel,
                gameState = gameState,
                gemCount = gemCount,
                gemDataStore = gemDataStore,
                rewardedAdManager = rewardedAdManager,
                onAction = { action ->
                    when (action) {
                        is GameAction.SetFiring -> gameViewModel.setFiring(action.isFiring)
                        is GameAction.Pause -> gameViewModel.pauseGame()
                        is GameAction.Resume -> gameViewModel.resumeGame()
                        is GameAction.Home -> {
                            gameViewModel.resetToMenu()
                            navController.navigate("menu") {
                                popUpTo("menu") { inclusive = true }
                            }
                        }
                        is GameAction.PlayAgain -> gameViewModel.startNewGame()
                    }
                }
            )
        }
    }
}

sealed class GameAction {
    data class SetFiring(val isFiring: Boolean) : GameAction()
    object Pause : GameAction()
    object Resume : GameAction()
    object Home : GameAction()
    object PlayAgain : GameAction()
}
