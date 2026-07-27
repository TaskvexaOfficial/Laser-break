package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.viewmodel.GameViewModel
import com.example.data.GemDataStore
import com.example.ads.RewardedAdManager

@Composable
fun ResultOverlay(
    isWin: Boolean,
    totalGems: Int,
    roundId: String,
    gameViewModel: GameViewModel,
    gemDataStore: GemDataStore,
    rewardedAdManager: RewardedAdManager,
    onAction: (GameAction) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // Win ad tracking
    var completedAds by remember { mutableStateOf(gameViewModel.getCompleted3XAds(roundId)) }
    var isLoadingAd by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(24.dp),
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
                Text(
                    text = if (isWin) "CORE BROKEN!" else "LASER OVERLOADED",
                    color = if (isWin) Color.White else Color(0xFFFF4444),
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
                    
                    // 3X Reward Button
                    val isClaimed = completedAds >= 3
                    Button(
                        onClick = {
                            if (activity != null && !isClaimed && !isLoadingAd) {
                                isLoadingAd = true
                                rewardedAdManager.showAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        val newCount = gameViewModel.record3XAdCompletion(roundId)
                                        completedAds = newCount
                                        isLoadingAd = false
                                        if (newCount >= 3 && gameViewModel.claim3XBonusReward(roundId)) {
                                            scope.launch { gemDataStore.addGems(6) }
                                            Toast.makeText(context, "3X Reward Claimed! +6 Coins", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onAdNotReady = {
                                        isLoadingAd = false
                                        Toast.makeText(context, "Ad not ready. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isClaimed) Color(0xFF475569) else Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isClaimed && !isLoadingAd
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isLoadingAd) "LOADING..." else if (isClaimed) "REWARD CLAIMED" else "3X REWARD", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (isClaimed) Color.White.copy(alpha = 0.5f) else Color.Black
                            )
                            if (!isClaimed && !isLoadingAd) {
                                val remaining = 3 - completedAds
                                val smallText = if (completedAds == 0) "Watch 3 Ads" else "$remaining Ads Left"
                                Text(
                                    text = smallText, 
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    // Loss Popup
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var isLossClaimed by remember { mutableStateOf(gameViewModel.hasClaimedLossReward(roundId)) }
                    
                    Button(
                        onClick = {
                            if (activity != null && !isLossClaimed && !isLoadingAd) {
                                isLoadingAd = true
                                rewardedAdManager.showAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        isLoadingAd = false
                                        if (gameViewModel.claimLossAdReward(roundId)) {
                                            isLossClaimed = true
                                            scope.launch { gemDataStore.addGems(3) }
                                            Toast.makeText(context, "Reward Claimed! +3 Coins", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onAdNotReady = {
                                        isLoadingAd = false
                                        Toast.makeText(context, "Ad not ready. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isLossClaimed) Color(0xFF475569) else Color(0xFFFFD700)),
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLossClaimed && !isLoadingAd
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isLoadingAd) "LOADING..." else if (isLossClaimed) "REWARD CLAIMED" else "GET 3", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (isLossClaimed) Color.White.copy(alpha = 0.5f) else Color.Black
                            )
                            if (!isLossClaimed && !isLoadingAd) {
                                Text(
                                    text = "Watch Ad", 
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TOTAL: $totalGems 💎",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { onAction(GameAction.PlayAgain) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isWin) "CONTINUE" else "TRY AGAIN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { onAction(GameAction.Home) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("HOME", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
