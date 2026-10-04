package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.LevelProgressEntity
import com.example.engine.LevelRegistry

@Composable
fun LevelSelectScreen(
    progressList: List<LevelProgressEntity>,
    totalDeaths: Int,
    isDarkTheme: Boolean,
    onSelectLevel: (Int) -> Unit,
    onOpenForge: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val bgColor = if (isDarkTheme) Color(0xFF16171E) else Color(0xFFEDE5DA)
    val cardBg = if (isDarkTheme) Color(0xFF1F212C) else Color(0xFFFAF7F2)
    val cardLockedBg = if (isDarkTheme) Color(0xFF14151B) else Color(0xFFDDD5C8)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isDarkTheme) Color(0xFF9094A6) else Color(0xFF75716A)

    val progressMap = progressList.associateBy { it.levelNumber }
    val clearedCount = progressList.count { it.completed }
    val totalStars = progressList.sumOf { it.stars }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(
                top = insets.calculateTopPadding(),
                bottom = insets.calculateBottomPadding()
            )
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LEVEL FORGE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Survive the deceptive trials",
                    fontSize = 13.sp,
                    color = subTextColor,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0x22FFFFFF) else Color(0x22000000))
                    .testTag("button_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = textColor
                )
            }
        }

        // Stats Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "CLEARED", value = "$clearedCount / ${LevelRegistry.TOTAL_LEVELS}", color = Color(0xFF06D6A0))
            StatItem(label = "STARS", value = "$totalStars ★", color = Color(0xFFFFD166))
            StatItem(label = "TOTAL DEATHS", value = "💀 $totalDeaths", color = Color(0xFFFF5252))
        }

        // Level Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 78.dp),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(LevelRegistry.TOTAL_LEVELS) { index ->
                val levelNum = index + 1
                val progress = progressMap[levelNum]
                val isUnlocked = progress?.unlocked ?: (levelNum == 1)
                val isCompleted = progress?.completed ?: false
                val stars = progress?.stars ?: 0

                LevelGridCard(
                    levelNumber = levelNum,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    stars = stars,
                    deaths = progress?.deaths ?: 0,
                    cardBg = if (isUnlocked) cardBg else cardLockedBg,
                    textColor = textColor,
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        if (isUnlocked) onSelectLevel(levelNum)
                    }
                )
            }
        }

        // Bottom CTA: Level Forge (Editor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onOpenForge,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD166),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_open_forge")
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "LEVEL FORGE: CREATE CUSTOM LEVEL",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
private fun LevelGridCard(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    deaths: Int,
    cardBg: Color,
    textColor: Color,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = if (isCompleted) 1.5.dp else 1.dp,
                color = when {
                    isCompleted -> Color(0xFF06D6A0)
                    isUnlocked -> if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_card_$levelNumber"),
        contentAlignment = Alignment.Center
    ) {
        if (!isUnlocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = if (isDarkTheme) Color(0x44FFFFFF) else Color(0x33000000),
                modifier = Modifier.size(28.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$levelNumber",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )

                if (isCompleted) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= stars) Color(0xFFFFD166) else Color(0x33888888),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "NEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06D6A0)
                    )
                }
            }
        }
    }
}
