package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultOverlay(
    isWin: Boolean,
    totalGems: Int,
    onAction: (GameAction) -> Unit
) {
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
                                text = "+10 GEMS",
                                color = Color(0xFF00FFFF),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "TOTAL GEMS: $totalGems",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { onAction(GameAction.PlayAgain) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isWin) "PLAY AGAIN" else "TRY AGAIN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
