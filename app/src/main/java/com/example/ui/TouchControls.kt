package com.example.ui

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameTouchControls(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    isInvertedControls: Boolean = false,
    onLeftChange: (Boolean) -> Unit,
    onRightChange: (Boolean) -> Unit,
    onJumpChange: (Boolean) -> Unit
) {
    val leftPressed = remember { mutableStateOf(false) }
    val rightPressed = remember { mutableStateOf(false) }
    val jumpPressed = remember { mutableStateOf(false) }

    val btnBg = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000)
    val btnPressedBg = if (isDarkTheme) Color(0x66FFFFFF) else Color(0x55000000)
    val iconColor = if (isDarkTheme) Color.White else Color(0xFF222222)
    val borderColor = if (isDarkTheme) Color(0x22FFFFFF) else Color(0x22000000)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left & Right cluster
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT Button
            TouchButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Move Left",
                testTag = "button_left",
                isPressed = leftPressed.value,
                backgroundColor = if (leftPressed.value) btnPressedBg else btnBg,
                borderColor = borderColor,
                iconColor = iconColor,
                onTouch = { pressed ->
                    leftPressed.value = pressed
                    onLeftChange(pressed)
                }
            )

            // RIGHT Button
            TouchButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Move Right",
                testTag = "button_right",
                isPressed = rightPressed.value,
                backgroundColor = if (rightPressed.value) btnPressedBg else btnBg,
                borderColor = borderColor,
                iconColor = iconColor,
                onTouch = { pressed ->
                    rightPressed.value = pressed
                    onRightChange(pressed)
                }
            )
        }

        // JUMP Button
        TouchButton(
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Jump",
            testTag = "button_jump",
            isPressed = jumpPressed.value,
            backgroundColor = if (jumpPressed.value) btnPressedBg else btnBg,
            borderColor = borderColor,
            iconColor = iconColor,
            isJump = true,
            onTouch = { pressed ->
                jumpPressed.value = pressed
                onJumpChange(pressed)
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TouchButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    isPressed: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    iconColor: Color,
    isJump: Boolean = false,
    onTouch: (Boolean) -> Unit
) {
    val size = if (isJump) 72.dp else 64.dp
    val iconSize = if (isJump) 42.dp else 36.dp

    Box(
        modifier = Modifier
            .size(size)
            .scale(if (isPressed) 0.92f else 1.0f)
            .clip(if (isJump) CircleShape else RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = if (isJump) CircleShape else RoundedCornerShape(18.dp)
            )
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onTouch(true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        onTouch(false)
                        true
                    }
                    else -> false
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
