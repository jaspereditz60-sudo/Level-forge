package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.engine.Door
import com.example.engine.DoorType
import com.example.engine.GameEngine
import com.example.engine.Platform
import com.example.engine.PlatformType
import com.example.engine.PlayerState
import com.example.engine.Spike
import com.example.engine.SpikeDirection
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDarkTheme) Color(0xFF1B1C23) else Color(0xFFE2D6C6)
    val platformColor = if (isDarkTheme) Color(0xFF111216) else Color(0xFF2A2B30)
    val platformHighlight = if (isDarkTheme) Color(0xFF282A33) else Color(0xFF3F414A)
    val spikeColor = if (isDarkTheme) Color(0xFF0F1014) else Color(0xFF1F2025)
    val doorFrameColor = if (isDarkTheme) Color(0xFF0D0E12) else Color(0xFF1E1F24)

    Canvas(modifier = modifier.fillMaxSize()) {
        val worldW = 800f
        val worldH = 450f
        val scaleVal = minOf(size.width / worldW, size.height / worldH)
        val offsetX = (size.width - worldW * scaleVal) / 2f
        val offsetY = (size.height - worldH * scaleVal) / 2f

        // Fill background
        drawRect(color = bgColor, size = size)

        // Draw within scaled world viewport
        scale(scaleVal, scaleVal, Offset(0f, 0f)) {
            // Apply translation offset
            val dx = offsetX / scaleVal
            val dy = offsetY / scaleVal

            // Draw World Boundary subtle guide
            drawRect(
                color = if (isDarkTheme) Color(0x11FFFFFF) else Color(0x11000000),
                topLeft = Offset(dx, dy),
                size = Size(worldW, worldH)
            )

            // 1. Draw Platforms
            for (p in engine.platforms) {
                if (!p.isVisible) continue
                drawPlatform(p, dx, dy, platformColor, platformHighlight, isDarkTheme)
            }

            // 2. Draw Spikes
            for (s in engine.spikes) {
                drawSpike(s, dx, dy, spikeColor)
            }

            // 3. Draw Exit Door
            drawDoor(engine.door, dx, dy, doorFrameColor, isDarkTheme)

            // 4. Draw Player
            if (engine.playerState != PlayerState.DEAD) {
                drawPlayer(engine, dx, dy, isDarkTheme)
            }

            // 5. Draw Death Particles (Dark obsidian shards with luminous outline)
            for (part in engine.deathParticles) {
                if (part.alpha > 0.01f) {
                    val pColor = Color(0xFF14151D).copy(alpha = part.alpha)
                    val pBorder = Color.White.copy(alpha = part.alpha * 0.8f)
                    val pCenter = Offset(dx + part.x, dy + part.y)
                    rotate(part.rotation, pCenter) {
                        drawRect(
                            color = pColor,
                            topLeft = Offset(pCenter.x - part.size / 2f, pCenter.y - part.size / 2f),
                            size = Size(part.size, part.size)
                        )
                        drawRect(
                            color = pBorder,
                            topLeft = Offset(pCenter.x - part.size / 2f, pCenter.y - part.size / 2f),
                            size = Size(part.size, part.size),
                            style = Stroke(width = 1f)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPlatform(
    p: Platform,
    dx: Float,
    dy: Float,
    baseColor: Color,
    highlightColor: Color,
    isDarkTheme: Boolean
) {
    val px = dx + p.x
    val py = dy + p.y

    when (p.type) {
        PlatformType.BOUNCE -> {
            // Base dark block
            drawRoundRect(
                color = baseColor,
                topLeft = Offset(px, py + 6f),
                size = Size(p.width, (p.height - 6f).coerceAtLeast(4f)),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Orange/Pink bouncy strip
            drawRoundRect(
                color = Color(0xFFFF5252),
                topLeft = Offset(px, py),
                size = Size(p.width, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PlatformType.CRUMBLING -> {
            val shakeX = if (p.hasTriggered && p.fallVy == 0f) sin(p.triggerTimer * 45f) * 2.5f else 0f
            drawRoundRect(
                color = if (isDarkTheme) Color(0xFF22242E) else Color(0xFF4A4542),
                topLeft = Offset(px + shakeX, py),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Crack lines
            drawLine(
                color = if (isDarkTheme) Color(0xFF14151B) else Color(0xFF2B2827),
                start = Offset(px + shakeX + p.width * 0.3f, py),
                end = Offset(px + shakeX + p.width * 0.45f, py + p.height),
                strokeWidth = 1.5f
            )
        }
        PlatformType.SINKING -> {
            drawRoundRect(
                color = if (isDarkTheme) Color(0xFF1A1C24) else Color(0xFF3B3835),
                topLeft = Offset(px, py),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Down indicator
            val arrowX = px + p.width / 2f
            val arrowY = py + p.height * 0.4f
            val path = Path().apply {
                moveTo(arrowX - 4f, arrowY - 3f)
                lineTo(arrowX + 4f, arrowY - 3f)
                lineTo(arrowX, arrowY + 4f)
                close()
            }
            drawPath(path, color = Color(0x66FFFFFF))
        }
        PlatformType.FAKE -> {
            drawRoundRect(
                color = baseColor.copy(alpha = 0.85f),
                topLeft = Offset(px, py),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
        PlatformType.DISAPPEARING -> {
            drawRoundRect(
                color = Color(0xFF6C757D),
                topLeft = Offset(px, py),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(px + 2f, py + 2f),
                size = Size(p.width - 4f, 3f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
        else -> {
            // Solid / Moving
            drawRoundRect(
                color = baseColor,
                topLeft = Offset(px, py),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Subtle top highlight
            if (p.height > 6f) {
                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(px, py),
                    size = Size(p.width, 3f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
    }
}

private fun DrawScope.drawSpike(s: Spike, dx: Float, dy: Float, color: Color) {
    if (s.popProgress <= 0.02f) return

    val sx = dx + s.x
    val sy = dy + s.y
    val w = s.width
    val h = s.height * s.popProgress

    val path = Path()
    when (s.direction) {
        SpikeDirection.UP -> {
            val base = sy + s.height
            val tip = base - h
            path.moveTo(sx, base)
            path.lineTo(sx + w / 2f, tip)
            path.lineTo(sx + w, base)
            path.close()
        }
        SpikeDirection.DOWN -> {
            val base = sy
            val tip = base + h
            path.moveTo(sx, base)
            path.lineTo(sx + w / 2f, tip)
            path.lineTo(sx + w, base)
            path.close()
        }
        SpikeDirection.LEFT -> {
            val base = sx + s.width
            val tip = base - (s.width * s.popProgress)
            path.moveTo(base, sy)
            path.lineTo(tip, sy + s.height / 2f)
            path.lineTo(base, sy + s.height)
            path.close()
        }
        SpikeDirection.RIGHT -> {
            val base = sx
            val tip = base + (s.width * s.popProgress)
            path.moveTo(base, sy)
            path.lineTo(tip, sy + s.height / 2f)
            path.lineTo(base, sy + s.height)
            path.close()
        }
    }

    drawPath(path, color = color)
}

private fun DrawScope.drawDoor(
    d: Door,
    dx: Float,
    dy: Float,
    frameColor: Color,
    isDarkTheme: Boolean
) {
    val x = dx + d.x
    val y = dy + d.y
    val w = d.width
    val h = d.height

    // Door Frame Outer
    drawRoundRect(
        color = frameColor,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(w / 2f, w / 2f)
    )

    // Inner Glowing Portal
    val glowColor1 = if (d.type == DoorType.FAKE_MIMIC) Color(0xFFFF4D4D) else Color(0xFFFFD166)
    val glowColor2 = if (d.type == DoorType.FAKE_MIMIC) Color(0xFFB30000) else Color(0xFFFF9E00)

    val innerMargin = 4f
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(glowColor1, glowColor2),
            startY = y,
            endY = y + h
        ),
        topLeft = Offset(x + innerMargin, y + innerMargin),
        size = Size(w - innerMargin * 2f, h - innerMargin),
        cornerRadius = CornerRadius((w - innerMargin * 2f) / 2f, (w - innerMargin * 2f) / 2f)
    )

    // Door Knob
    drawCircle(
        color = Color(0xFF1E2028),
        radius = 2.5f,
        center = Offset(x + w - 7f, y + h * 0.62f)
    )

    // Mimic teeth if fake
    if (d.type == DoorType.FAKE_MIMIC) {
        val teethPath = Path().apply {
            moveTo(x + 8f, y + 16f)
            lineTo(x + 12f, y + 26f)
            lineTo(x + 16f, y + 16f)
            lineTo(x + 20f, y + 26f)
            lineTo(x + 24f, y + 16f)
            close()
        }
        drawPath(teethPath, color = Color.White)
    }
}

private fun DrawScope.drawPlayer(
    engine: GameEngine,
    dx: Float,
    dy: Float,
    isDarkTheme: Boolean
) {
    val px = dx + engine.playerX
    val py = dy + engine.playerY
    val pw = engine.playerWidth
    val ph = engine.playerHeight

    val centerX = px + pw / 2f
    val centerY = py + ph / 2f

    val scaleX = engine.playerSquashX
    val scaleY = if (engine.isGravityInverted) -engine.playerSquashY else engine.playerSquashY

    // Smooth subtle running dust trail behind feet
    if (engine.playerGrounded && kotlin.math.abs(engine.playerVx) > 50f) {
        val trailX = centerX - engine.playerFacing * (pw * 0.7f)
        val trailY = py + ph - 3f
        drawCircle(
            color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000),
            radius = 3.5f,
            center = Offset(trailX, trailY)
        )
        drawCircle(
            color = if (isDarkTheme) Color(0x22FFFFFF) else Color(0x15000000),
            radius = 2f,
            center = Offset(trailX - engine.playerFacing * 6f, trailY - 1f)
        )
    }

    // Apply smooth running tilt and squash/stretch
    rotate(engine.playerTilt, Offset(centerX, py + ph)) {
        scale(scaleX, scaleY, Offset(centerX, centerY)) {
            // Elongated Dark Obsidian Body
            val bodyFill = Color(0xFF13141B)
            val bodyBorder = if (isDarkTheme) Color(0xFF4A4E61) else Color(0xFF2C2F3B)

            drawRoundRect(
                color = bodyFill,
                topLeft = Offset(px, py),
                size = Size(pw, ph),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = bodyBorder,
                topLeft = Offset(px, py),
                size = Size(pw, ph),
                cornerRadius = CornerRadius(5f, 5f),
                style = Stroke(width = 1.4f)
            )

            // Expressive Glowing White Eyes on the Dark Body
            val eyeLookOffset = engine.playerFacing * 2.5f
            val eyeBaseY = py + ph * 0.28f
            val isShocked = engine.playerEyeShock > 0.05f
            val isBlinking = engine.playerBlinkTimer < 0.12f

            if (isBlinking && !isShocked) {
                // Cute squinting blink line
                drawLine(
                    color = Color.White,
                    start = Offset(centerX - 5f + eyeLookOffset, eyeBaseY + 3f),
                    end = Offset(centerX - 1f + eyeLookOffset, eyeBaseY + 3f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(centerX + 1f + eyeLookOffset, eyeBaseY + 3f),
                    end = Offset(centerX + 5f + eyeLookOffset, eyeBaseY + 3f),
                    strokeWidth = 2f
                )
            } else if (isShocked) {
                // Comedic wide shocked white eyes with tiny pupils "O_O"
                drawCircle(
                    color = Color.White,
                    radius = 4.2f,
                    center = Offset(centerX - 3.5f + eyeLookOffset, eyeBaseY + 3f),
                    style = Fill
                )
                drawCircle(
                    color = Color(0xFF13141B),
                    radius = 1.4f,
                    center = Offset(centerX - 3.5f + eyeLookOffset, eyeBaseY + 3f)
                )

                drawCircle(
                    color = Color.White,
                    radius = 4.2f,
                    center = Offset(centerX + 3.5f + eyeLookOffset, eyeBaseY + 3f),
                    style = Fill
                )
                drawCircle(
                    color = Color(0xFF13141B),
                    radius = 1.4f,
                    center = Offset(centerX + 3.5f + eyeLookOffset, eyeBaseY + 3f)
                )
            } else {
                // Standard tall expressive luminous white capsule eyes
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(centerX - 5.5f + eyeLookOffset, eyeBaseY),
                    size = Size(3.6f, 7.5f),
                    cornerRadius = CornerRadius(1.8f, 1.8f)
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(centerX + 1.5f + eyeLookOffset, eyeBaseY),
                    size = Size(3.6f, 7.5f),
                    cornerRadius = CornerRadius(1.8f, 1.8f)
                )

                // Black pupils looking in direction of motion
                val pupilDx = if (engine.playerFacing > 0) 1.2f else -0.4f
                drawCircle(
                    color = Color(0xFF13141B),
                    radius = 1.1f,
                    center = Offset(centerX - 3.7f + eyeLookOffset + pupilDx, eyeBaseY + 4f)
                )
                drawCircle(
                    color = Color(0xFF13141B),
                    radius = 1.1f,
                    center = Offset(centerX + 3.3f + eyeLookOffset + pupilDx, eyeBaseY + 4f)
                )
            }
        }
    }
}
