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
import com.example.ads.ConsentManager
import com.example.audio.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(soundManager: SoundManager, consentManager: ConsentManager) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val gameViewModel: GameViewModel = viewModel()
    val context = LocalContext.current
    val gemDataStore = remember { GemDataStore(context) }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    
    val canRequestAds by consentManager.canRequestAds.collectAsState()
    
    val isPrivacyOptionsRequired by consentManager.isPrivacyOptionsRequired.collectAsState()
    
    
    val gemCount by gemDataStore.gemCount.collectAsState(initial = 0)
    val soundEnabled by gemDataStore.soundEnabled.collectAsState(initial = true)
    
    LaunchedEffect(soundEnabled) {
        soundManager.setSoundEnabled(soundEnabled)
    }

    val gameState by gameViewModel.gameState.collectAsState()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onFinish = {
                soundManager.startBgm()
                navController.navigate("menu") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        
        composable("menu") {
            MainMenuScreen(
                gemCount = gemCount,
                soundEnabled = soundEnabled,
                onSoundToggle = { 
                    soundManager.setSoundEnabled(it); scope.launch { gemDataStore.setSoundEnabled(it) }
                },
                onPlayClick = {
                    gameViewModel.startNewGame()
                    navController.navigate("game")
                },
                isPrivacyOptionsRequired = isPrivacyOptionsRequired,
                onPrivacyOptionsClick = {
                    consentManager.showPrivacyOptionsForm {
                        // Optional: Handle dismiss
                    }
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
                soundManager = soundManager,
                onAction = { action ->
                    when (action) {
                        is GameAction.SetFiring -> {
                            gameViewModel.setFiring(action.isFiring)
                            soundManager.setLaserActive(action.isFiring)
                        }
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
