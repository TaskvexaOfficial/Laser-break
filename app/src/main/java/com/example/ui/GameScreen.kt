package com.example.ui

import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.GemDataStore
import com.example.model.GameStatus
import com.example.model.GameState
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    gameState: GameState,
    gemCount: Int,
    gemDataStore: GemDataStore,
    onAction: (GameAction) -> Unit
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var showInitialInstruction by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                onAction(GameAction.Pause)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.WON) {
            scope.launch {
                gemDataStore.addGems(10)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        } else if (gameState.status == GameStatus.LOST) {
             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50, 200), -1)
            }
        }
    }
    
    // Vibration during firing
    LaunchedEffect(gameState.isFiring) {
        if (gameState.isFiring) {
            showInitialInstruction = false
        }
    }

    GameBackground(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (gameState.status == GameStatus.PLAYING) {
                            onAction(GameAction.SetFiring(true))
                            tryAwaitRelease()
                            onAction(GameAction.SetFiring(false))
                        }
                    }
                )
            }
    ) {
        // Game Canvas
        GameRenderer(gameState = gameState, isMenu = false)

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .border(1.dp, Color(0xFF00FFFF).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiamondIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = gemCount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            IconButton(
                onClick = { onAction(GameAction.Pause) },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f), CircleShape)
                    .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.5f), CircleShape)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White
                )
            }
        }

        if (showInitialInstruction && gameState.status == GameStatus.PLAYING) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Text(
                    text = "HOLD TO BREAK",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }

        // Overlays
        when (gameState.status) {
            GameStatus.WON -> ResultOverlay(true, gemCount, onAction)
            GameStatus.LOST -> ResultOverlay(false, gemCount, onAction)
            GameStatus.PAUSED -> PauseOverlay(onAction)
            else -> {}
        }
    }
}
