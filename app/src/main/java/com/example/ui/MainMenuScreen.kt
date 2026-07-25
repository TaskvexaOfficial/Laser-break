package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameStatus
import com.example.model.GameState
import com.example.model.Layer
import com.example.model.Segment
import com.example.model.Structure
import com.example.model.StructureType
import com.example.model.ThemeBlue

@Composable
fun MainMenuScreen(
    gemCount: Int,
    onPlayClick: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    
    // Dummy structure for the menu logo
    val dummyStructure = remember {
        Structure(
            type = StructureType.SEGMENTED_CIRCLE,
            layers = listOf(
                Layer(
                    index = 0, radius = 150f, thickness = 45f, currentRotation = 0f, rotationSpeed = 10f, isClockwise = true,
                    segments = listOf(
                        Segment(0, 0, 10f, 70f, false),
                        Segment(1, 0, 90f, 150f, true),
                        Segment(2, 0, 250f, 100f, false)
                    )
                ),
                Layer(
                    index = 1, radius = 90f, thickness = 45f, currentRotation = -45f, rotationSpeed = 15f, isClockwise = false,
                    segments = listOf(
                        Segment(3, 1, 0f, 80f, true),
                        Segment(4, 1, 90f, 160f, false),
                        Segment(5, 1, 260f, 80f, false)
                    )
                )
            ),
            colorTheme = ThemeBlue
        )
    }

    GameBackground {
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
                onClick = { soundEnabled = !soundEnabled },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f), CircleShape)
                    .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.5f), CircleShape)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Toggle Sound",
                    tint = Color.White
                )
            }
        }

        // Center Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                 GameRenderer(
                    gameState = GameState(
                        status = GameStatus.IDLE,
                        structure = dummyStructure,
                        isFiring = true,
                        laserTipY = 30f // stop at core
                    ),
                    isMenu = true // Custom flag to adjust drawing positions if needed
                )
            }

            Text(
                text = "LASER",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFF000080),
                        offset = androidx.compose.ui.geometry.Offset(0f, 8f),
                        blurRadius = 8f
                    )
                )
            )
            Text(
                text = "BREAK",
                color = Color(0xFF00FFFF),
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.offset(y = (-16).dp),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0xFF000080),
                        offset = androidx.compose.ui.geometry.Offset(0f, 8f),
                        blurRadius = 8f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .width(240.dp)
                    .height(72.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF00FFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF00FFFF), Color(0xFF0055FF))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(2.dp, Color(0xFFE0FFFF), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PLAY",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Hold to break. Release to stay safe.",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == 1) 12.dp else 10.dp)
                            .background(
                                if (i == 1) Color(0xFFFF4444) else Color.White.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .shadow(if (i == 1) 8.dp else 0.dp, spotColor = Color.Red)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "A TaskVexa Game",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}
