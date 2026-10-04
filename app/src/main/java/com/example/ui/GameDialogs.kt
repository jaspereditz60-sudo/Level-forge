package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun VictoryDialog(
    levelNumber: Int,
    levelTitle: String,
    deaths: Int,
    timeMs: Long,
    stars: Int,
    hasNextLevel: Boolean,
    isDarkTheme: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelSelect: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDarkTheme) Color(0xFF1E2028) else Color(0xFFF7F2EA),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("dialog_victory")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LEVEL $levelNumber CLEARED!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkTheme) Color.White else Color(0xFF1A1A1A),
                    letterSpacing = 1.sp
                )

                Text(
                    text = levelTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFFB0B3C0) else Color(0xFF6B6860),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Star Rating
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    for (i in 1..3) {
                        val isFilled = i <= stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (isFilled) Color(0xFFFFD166) else Color(0x44888888),
                            modifier = Modifier
                                .size(36.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDarkTheme) Color(0xFF15161C) else Color(0xFFEBE3D5))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DEATHS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFF8E92A4) else Color(0xFF88847C)
                        )
                        Text(
                            text = "💀 $deaths",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (deaths == 0) Color(0xFF06D6A0) else if (isDarkTheme) Color.White else Color(0xFF222222)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TIME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFF8E92A4) else Color(0xFF88847C)
                        )
                        val seconds = timeMs / 1000f
                        Text(
                            text = String.format("%.2fs", seconds),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDarkTheme) Color.White else Color(0xFF222222)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                if (hasNextLevel) {
                    Button(
                        onClick = onNextLevel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF06D6A0),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("button_next_level")
                    ) {
                        Text("NEXT LEVEL", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReplay,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("button_replay")
                    ) {
                        Text("RETRY", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onLevelSelect,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("button_levels")
                    ) {
                        Text("LEVELS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    levelNumber: Int,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    isDarkTheme: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleTheme: () -> Unit,
    onLevelSelect: () -> Unit,
    onLevelForge: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDarkTheme) Color(0xFF1E2028) else Color(0xFFF7F2EA),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("dialog_pause")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkTheme) Color.White else Color(0xFF1A1A1A),
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Level $levelNumber",
                    fontSize = 14.sp,
                    color = if (isDarkTheme) Color(0xFF8E92A4) else Color(0xFF88847C),
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // Settings toggles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound",
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sound Effects",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound,
                        modifier = Modifier.testTag("switch_sound")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptics",
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Haptic Feedback",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = onToggleHaptics,
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Resume
                Button(
                    onClick = onResume,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color.White else Color(0xFF222222),
                        contentColor = if (isDarkTheme) Color.Black else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_resume")
                ) {
                    Text("RESUME", fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Restart
                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("button_pause_restart")
                ) {
                    Text("RESTART LEVEL", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Level Select & Level Forge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onLevelSelect,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("button_pause_levels")
                    ) {
                        Text("LEVELS", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onLevelForge,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("button_pause_forge")
                    ) {
                        Text("FORGE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
