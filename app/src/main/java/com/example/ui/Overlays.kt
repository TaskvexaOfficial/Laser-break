package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.DelicateCoroutinesApi
import com.example.viewmodel.GameViewModel
import com.example.data.GemDataStore
import com.example.ads.RewardedAdManager
import com.example.audio.SoundManager

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ResultOverlay(
    isWin: Boolean,
    totalGems: Int,
    roundId: String,
    gameViewModel: GameViewModel,
    gemDataStore: GemDataStore,
    rewardedAdManager: RewardedAdManager,
    soundManager: SoundManager,
    onAction: (GameAction) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    var completedAds by remember { mutableStateOf(gameViewModel.getCompleted3XAds(roundId)) }
    var isLoadingAd by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1128)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF00FFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    if (isWin) {
                        Canvas(modifier = Modifier.size(48.dp)) {
                            drawCircle(
                                color = Color(0xFF00FFFF),
                                radius = size.minDimension / 2 - 2.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawLine(
                                color = Color(0xFF00FFFF),
                                start = Offset(size.width * 0.2f, size.height * 0.2f),
                                end = Offset(size.width * 0.8f, size.height * 0.8f),
                                strokeWidth = 3.dp.toPx()
                            )
                            drawLine(
                                color = Color(0xFF00FFFF),
                                start = Offset(size.width * 0.8f, size.height * 0.2f),
                                end = Offset(size.width * 0.2f, size.height * 0.8f),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.size(48.dp)) {
                            drawCircle(
                                color = Color(0xFF00FFFF),
                                radius = size.minDimension / 2 - 2.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFFFF4444),
                                radius = size.minDimension / 4
                            )
                            drawLine(
                                color = Color(0xFFFF4444),
                                start = Offset(size.width * 0.2f, size.height * 0.2f),
                                end = Offset(size.width * 0.8f, size.height * 0.8f),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isWin) "CORE BROKEN!" else "TRY AGAIN",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    if (isWin) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text("💎", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "+3 COINS",
                                    color = Color(0xFF00FFFF),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val isClaimed = completedAds >= 3
                        OutlinedButton(
                            onClick = {
                                if (activity != null && !isClaimed && !isLoadingAd) {
                                    isLoadingAd = true
                                    soundManager.setAdActive(true)
                                    rewardedAdManager.showAd(
                                        activity = activity,
                                        onRewardEarned = {
                                            val newCount = gameViewModel.record3XAdCompletion(roundId)
                                            completedAds = newCount
                                            if (newCount >= 3 && gameViewModel.claim3XBonusReward(roundId)) {
                                                GlobalScope.launch { gemDataStore.addGems(9) }
                                                Toast.makeText(context, "3X Reward Claimed! +9 Coins", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onAdDismissed = {
                                            soundManager.setAdActive(false)
                                            isLoadingAd = false
                                        },
                                        onAdNotReady = {
                                            soundManager.setAdActive(false)
                                            isLoadingAd = false
                                            Toast.makeText(context, "Ad not ready. Try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            border = BorderStroke(1.dp, Color(0xFF00FFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isClaimed) Color(0xFF1E293B) else Color.Transparent,
                                contentColor = if (isClaimed) Color.White.copy(alpha = 0.5f) else Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isClaimed && !isLoadingAd
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isLoadingAd) "LOADING..." else if (isClaimed) "REWARD CLAIMED" else "3X REWARD", 
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FFFF)
                                )
                                if (!isClaimed && !isLoadingAd) {
                                    val remaining = 3 - completedAds
                                    val smallText = if (completedAds == 0) "Watch 3 Ads" else "$remaining Ads Left"
                                    Text(
                                        text = "$completedAds/3 - $smallText", 
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { 
                                GlobalScope.launch {
                                    if (gameViewModel.claimBaseWinReward(roundId)) {
                                        gemDataStore.addGems(3)
                                    }
                                }
                                onAction(GameAction.PlayAgain)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CONTINUE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "You hit a dark zone",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        var isLossClaimed by remember { mutableStateOf(gameViewModel.hasClaimedLossReward(roundId)) }
                        
                        OutlinedButton(
                            onClick = {
                                if (activity != null && !isLossClaimed && !isLoadingAd) {
                                    isLoadingAd = true
                                    soundManager.setAdActive(true)
                                    rewardedAdManager.showAd(
                                        activity = activity,
                                        onRewardEarned = {
                                            if (gameViewModel.claimLossAdReward(roundId)) {
                                                isLossClaimed = true
                                                GlobalScope.launch { gemDataStore.addGems(3) }
                                                Toast.makeText(context, "Reward Claimed! +3 Coins", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onAdDismissed = {
                                            soundManager.setAdActive(false)
                                            isLoadingAd = false
                                        },
                                        onAdNotReady = {
                                            soundManager.setAdActive(false)
                                            isLoadingAd = false
                                            Toast.makeText(context, "Ad not ready. Try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            border = BorderStroke(1.dp, Color(0xFF00FFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isLossClaimed) Color(0xFF1E293B) else Color.Transparent,
                                contentColor = if (isLossClaimed) Color.White.copy(alpha = 0.5f) else Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLossClaimed && !isLoadingAd
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isLoadingAd) "LOADING..." else if (isLossClaimed) "REWARD CLAIMED" else "GET 3", 
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FFFF)
                                )
                                if (!isLossClaimed && !isLoadingAd) {
                                    Text(
                                        text = "Watch Ad", 
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { onAction(GameAction.PlayAgain) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("RETRY", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { 
                             if (isWin) {
                                 GlobalScope.launch {
                                     if (gameViewModel.claimBaseWinReward(roundId)) {
                                         gemDataStore.addGems(3)
                                     }
                                 }
                             }
                             onAction(GameAction.Home)
                         }
                    ) {
                        Text("HOME", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PauseOverlay(onAction: (GameAction) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PAUSED",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onAction(GameAction.Resume) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),
                modifier = Modifier.width(200.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RESUME", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { onAction(GameAction.Home) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.width(200.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("HOME", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
