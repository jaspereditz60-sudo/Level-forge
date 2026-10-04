package com.example.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    isDarkTheme: Boolean,
    totalDeaths: Int,
    totalClears: Int,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var showConfirmReset by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDarkTheme) Color(0xFF1E202B) else Color(0xFFFAF7F2),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("dialog_settings")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDarkTheme) Color.White else Color(0xFF1A1A1A),
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_settings_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
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
                        modifier = Modifier.testTag("settings_switch_sound")
                    )
                }

                // Haptics Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Vibrations",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = onToggleHaptics,
                        modifier = Modifier.testTag("settings_switch_haptics")
                    )
                }

                // Theme Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Brightness4,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Dark Mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onToggleTheme,
                        modifier = Modifier.testTag("settings_switch_theme")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDarkTheme) Color(0xFF14151C) else Color(0xFFEDE5DA),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "TOTAL DEATHS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(text = "💀 $totalDeaths", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF5252))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "LEVELS CLEARED", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(text = "$totalClears", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF06D6A0))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reset Progress
                OutlinedButton(
                    onClick = { showConfirmReset = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("button_reset_progress")
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESET ALL PROGRESS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showConfirmReset) {
        AlertDialog(
            onDismissRequest = { showConfirmReset = false },
            title = { Text("Reset Progress?", fontWeight = FontWeight.Black) },
            text = { Text("This will lock levels 2-20 and reset your death counts. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmReset = false
                        onResetProgress()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("RESET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmReset = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
