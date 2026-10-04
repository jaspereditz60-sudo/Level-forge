package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameEngine
import com.example.engine.LevelRegistry
import com.example.engine.PlayerState

@Composable
fun GameScreen(
    engine: GameEngine,
    levelNumber: Int,
    isDarkTheme: Boolean,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleTheme: () -> Unit,
    onNavigateLevelSelect: () -> Unit,
    onNavigateLevelForge: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPauseDialog by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var victoryDeaths by remember { mutableIntStateOf(0) }
    var victoryTimeMs by remember { mutableLongStateOf(0L) }
    var victoryStars by remember { mutableIntStateOf(3) }

    // Synchronize engine settings
    DisposableEffect(soundEnabled, hapticsEnabled) {
        engine.soundSynth.isEnabled = soundEnabled
        engine.haptics.isEnabled = hapticsEnabled
        onDispose {}
    }

    // Load level when levelNumber changes
    LaunchedEffect(levelNumber) {
        engine.loadLevel(levelNumber)
        showVictoryDialog = false
        showPauseDialog = false
    }

    // Intercept Back button to show pause dialog or navigate back
    BackHandler {
        if (!showPauseDialog && !showVictoryDialog) {
            showPauseDialog = true
            engine.isPaused = true
        } else if (showPauseDialog) {
            showPauseDialog = false
            engine.isPaused = false
        } else {
            onNavigateLevelSelect()
        }
    }

    // 60FPS Game Loop
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastTime != 0L) {
                    val dt = (frameNanos - lastTime) / 1_000_000_000f
                    engine.update(dt)

                    // Check for victory dialog display
                    if (engine.playerState == PlayerState.CELEBRATING && engine.celebrationTimer > 0.45f && !showVictoryDialog) {
                        showVictoryDialog = true
                        victoryDeaths = engine.levelDeaths
                        victoryTimeMs = System.currentTimeMillis() - engine.levelStartTime
                        victoryStars = when {
                            victoryDeaths <= 1 -> 3
                            victoryDeaths <= 4 -> 2
                            else -> 1
                        }
                    }
                }
                lastTime = frameNanos
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val insets = WindowInsets.safeDrawing.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                val isDown = keyEvent.type == KeyEventType.KeyDown
                when (keyEvent.key) {
                    Key.DirectionLeft, Key.A -> {
                        engine.inputLeft = isDown
                        true
                    }
                    Key.DirectionRight, Key.D -> {
                        engine.inputRight = isDown
                        true
                    }
                    Key.DirectionUp, Key.W, Key.Spacebar -> {
                        if (isDown && !engine.inputJump) {
                            engine.onJumpPressed()
                        } else if (!isDown && engine.inputJump) {
                            engine.onJumpReleased()
                        }
                        engine.inputJump = isDown
                        true
                    }
                    Key.R -> {
                        if (isDown) engine.restartLevel(isFullReset = false)
                        true
                    }
                    else -> false
                }
            }
    ) {
        // 1. Vector Game Canvas
        GameCanvas(
            engine = engine,
            isDarkTheme = isDarkTheme
        )

        // 2. Minimalist Top HUD
        GameHUD(
            levelNumber = engine.currentLevelDef.levelNumber,
            levelTitle = engine.currentLevelDef.title,
            deaths = engine.levelDeaths,
            isDarkTheme = isDarkTheme,
            onRetry = {
                engine.restartLevel(isFullReset = false)
                engine.soundSynth.playClick()
            },
            onPause = {
                showPauseDialog = true
                engine.isPaused = true
                engine.soundSynth.playClick()
            },
            modifier = Modifier.padding(top = insets.calculateTopPadding())
        )

        // 3. Floating Troll Alert / Message Banner
        AnimatedVisibility(
            visible = engine.activeMessage != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = insets.calculateTopPadding() + 64.dp)
        ) {
            engine.activeMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDarkTheme) Color(0xFFFF5252) else Color(0xFFD90429))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("troll_message_banner")
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 4. Mobile Touch Controls
        GameTouchControls(
            isDarkTheme = isDarkTheme,
            isInvertedControls = engine.isInvertedControls,
            onLeftChange = { pressed ->
                engine.inputLeft = pressed
            },
            onRightChange = { pressed ->
                engine.inputRight = pressed
            },
            onJumpChange = { pressed ->
                if (pressed && !engine.inputJump) {
                    engine.onJumpPressed()
                } else if (!pressed && engine.inputJump) {
                    engine.onJumpReleased()
                }
                engine.inputJump = pressed
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = insets.calculateBottomPadding())
        )

        // 5. Victory Dialog
        if (showVictoryDialog) {
            VictoryDialog(
                levelNumber = engine.currentLevelDef.levelNumber,
                levelTitle = engine.currentLevelDef.title,
                deaths = victoryDeaths,
                timeMs = victoryTimeMs,
                stars = victoryStars,
                hasNextLevel = engine.currentLevelDef.levelNumber < LevelRegistry.TOTAL_LEVELS,
                isDarkTheme = isDarkTheme,
                onNextLevel = {
                    showVictoryDialog = false
                    val nextNum = engine.currentLevelDef.levelNumber + 1
                    engine.loadLevel(nextNum)
                },
                onReplay = {
                    showVictoryDialog = false
                    engine.loadLevel(engine.currentLevelDef.levelNumber)
                },
                onLevelSelect = {
                    showVictoryDialog = false
                    onNavigateLevelSelect()
                }
            )
        }

        // 6. Pause Dialog
        if (showPauseDialog) {
            PauseDialog(
                levelNumber = engine.currentLevelDef.levelNumber,
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                isDarkTheme = isDarkTheme,
                onResume = {
                    showPauseDialog = false
                    engine.isPaused = false
                },
                onRestart = {
                    showPauseDialog = false
                    engine.isPaused = false
                    engine.restartLevel(isFullReset = false)
                },
                onToggleSound = onToggleSound,
                onToggleHaptics = onToggleHaptics,
                onToggleTheme = onToggleTheme,
                onLevelSelect = {
                    showPauseDialog = false
                    engine.isPaused = false
                    onNavigateLevelSelect()
                },
                onLevelForge = {
                    showPauseDialog = false
                    engine.isPaused = false
                    onNavigateLevelForge()
                }
            )
        }
    }
}

@Composable
private fun GameHUD(
    levelNumber: Int,
    levelTitle: String,
    deaths: Int,
    isDarkTheme: Boolean,
    onRetry: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barBg = if (isDarkTheme) Color(0x33000000) else Color(0x22FFFFFF)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1E2028)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pause Button
        IconButton(
            onClick = onPause,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(barBg)
                .testTag("button_pause")
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Pause",
                tint = textColor
            )
        }

        // Level Title & Skull Death Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(barBg)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "LEVEL $levelNumber",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Death Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF5252).copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "💀 $deaths",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF5252)
                )
            }
        }

        // Instant Retry Button
        IconButton(
            onClick = onRetry,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(barBg)
                .testTag("button_retry")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Restart",
                tint = textColor
            )
        }
    }
}
