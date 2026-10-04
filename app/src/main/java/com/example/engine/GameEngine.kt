package com.example.engine

import com.example.audio.HapticsHelper
import com.example.audio.SoundSynth
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

class GameEngine(
    val soundSynth: SoundSynth,
    val haptics: HapticsHelper,
    val onLevelCompleted: (levelNumber: Int, deaths: Int, elapsedMs: Long) -> Unit,
    val onDeathRecorded: (levelNumber: Int) -> Unit
) {
    // Current level
    var currentLevelDef: LevelDef = LevelRegistry.getLevel(1)
        private set

    // Player state
    var playerX: Float = 0f
    var playerY: Float = 0f
    var playerVx: Float = 0f
    var playerVy: Float = 0f
    val playerWidth: Float = 18f
    val playerHeight: Float = 36f
    var playerGrounded: Boolean = false
    var playerFacing: Float = 1f // 1 = right, -1 = left
    var playerEyeShock: Float = 0f
    var playerSquashX: Float = 1f
    var playerSquashY: Float = 1f
    var playerBlinkTimer: Float = 0f
    var playerTilt: Float = 0f
    var walkAnimTimer: Float = 0f

    var playerState: PlayerState = PlayerState.ALIVE
    var deathTimer: Float = 0f
    var celebrationTimer: Float = 0f
    val deathParticles = mutableListOf<DeathParticle>()

    // Inputs
    var inputLeft: Boolean = false
    var inputRight: Boolean = false
    var inputJump: Boolean = false
    private var lastJumpPressed: Boolean = false

    // Physics parameters
    private val gravity: Float = 1150f
    private val moveSpeed: Float = 230f
    private val jumpImpulse: Float = -470f
    private var coyoteTimer: Float = 0f
    private var jumpBufferTimer: Float = 0f

    // Troll statuses
    var isInvertedControls: Boolean = false
    var isGravityInverted: Boolean = false
    var activeMessage: String? = null
    var messageTimer: Float = 0f

    // Stats for current level attempt
    var levelDeaths: Int = 0
    var levelStartTime: Long = System.currentTimeMillis()
    var isPaused: Boolean = false

    // Dynamic level objects copies
    var door: Door = Door(x = 0f, y = 0f)
    val platforms = mutableListOf<Platform>()
    val spikes = mutableListOf<Spike>()
    val triggers = mutableListOf<LevelTrigger>()

    // Timer for disappearing platforms
    private var disappearingTimer: Float = 0f

    init {
        loadLevel(1)
    }

    fun loadLevel(levelNumber: Int) {
        val def = LevelRegistry.getLevel(levelNumber)
        loadCustomLevel(def)
    }

    fun loadCustomLevel(def: LevelDef) {
        currentLevelDef = def
        levelDeaths = 0
        levelStartTime = System.currentTimeMillis()
        restartLevel(isFullReset = true)
    }

    fun restartLevel(isFullReset: Boolean = false) {
        if (isFullReset) {
            levelStartTime = System.currentTimeMillis()
        }
        playerState = PlayerState.ALIVE
        deathTimer = 0f
        celebrationTimer = 0f
        deathParticles.clear()

        playerX = currentLevelDef.spawnX
        playerY = currentLevelDef.spawnY
        playerVx = 0f
        playerVy = 0f
        playerGrounded = false
        playerFacing = 1f
        playerEyeShock = 0f
        playerSquashX = 1f
        playerSquashY = 1f
        playerTilt = 0f
        walkAnimTimer = 0f

        coyoteTimer = 0f
        jumpBufferTimer = 0f

        isInvertedControls = currentLevelDef.initialInvertControls
        isGravityInverted = currentLevelDef.initialGravityInverted
        activeMessage = currentLevelDef.trollMessage.ifEmpty { null }
        messageTimer = if (activeMessage != null) 3.5f else 0f

        // Deep copy door
        val d = currentLevelDef.door
        door = d.copy(
            x = d.x,
            y = d.y,
            isTriggered = false,
            isReal = d.isReal,
            openProgress = 0f
        )

        // Deep copy platforms
        platforms.clear()
        currentLevelDef.platforms.forEach { p ->
            platforms.add(
                p.copy(
                    x = p.x,
                    y = p.y,
                    triggerTimer = 0f,
                    hasTriggered = false,
                    fallVy = 0f,
                    isVisible = true,
                    isSolid = p.type != PlatformType.FAKE
                )
            )
        }

        // Deep copy spikes
        spikes.clear()
        currentLevelDef.spikes.forEach { s ->
            spikes.add(
                s.copy(
                    x = s.x,
                    y = s.y,
                    popProgress = if (s.isSneaky) 0f else 1f,
                    isTriggered = false,
                    triggerDelay = 0f
                )
            )
        }

        // Deep copy triggers
        triggers.clear()
        currentLevelDef.triggers.forEach { t ->
            triggers.add(t.copy(isTriggered = false))
        }
    }

    fun onJumpPressed() {
        jumpBufferTimer = 0.13f
        // Check if door jumps when player jumps
        if (door.type == DoorType.JUMPER && playerState == PlayerState.ALIVE) {
            door.jumpVy = -400f
            door.isGrounded = false
        }
    }

    fun onJumpReleased() {
        if (playerState != PlayerState.ALIVE) return
        if (!isGravityInverted && playerVy < -50f) {
            playerVy *= 0.5f
        } else if (isGravityInverted && playerVy > 50f) {
            playerVy *= 0.5f
        }
    }

    fun update(dt: Float) {
        if (isPaused) return
        val clampedDt = dt.coerceIn(0f, 0.05f)

        // Message timer
        if (messageTimer > 0f) {
            messageTimer -= clampedDt
            if (messageTimer <= 0f) {
                activeMessage = null
            }
        }

        // Blinking platforms
        disappearingTimer += clampedDt
        val isDisappearingActive = (disappearingTimer % 2.4f) < 1.4f
        for (p in platforms) {
            if (p.type == PlatformType.DISAPPEARING) {
                p.isVisible = isDisappearingActive
                p.isSolid = isDisappearingActive
            }
        }

        // Eye blink
        playerBlinkTimer += clampedDt
        if (playerBlinkTimer > 3.0f) {
            playerBlinkTimer = 0f
        }

        // Decay eye shock
        if (playerEyeShock > 0f) {
            playerEyeShock = (playerEyeShock - clampedDt * 2f).coerceAtLeast(0f)
        }

        // Squash stretch decay
        playerSquashX += (1f - playerSquashX) * (clampedDt * 12f)
        playerSquashY += (1f - playerSquashY) * (clampedDt * 12f)

        // Handle states
        when (playerState) {
            PlayerState.DEAD -> {
                updateDead(clampedDt)
                return
            }
            PlayerState.CELEBRATING -> {
                updateCelebrating(clampedDt)
                return
            }
            PlayerState.ALIVE -> {
                updateAlive(clampedDt)
            }
        }
    }

    private fun updateAlive(dt: Float) {
        val playerBoundsBefore = RectF(playerX, playerY, playerX + playerWidth, playerY + playerHeight)

        // Coyote and buffer timers (generous and forgiving)
        if (playerGrounded) {
            coyoteTimer = 0.15f
        } else {
            coyoteTimer = (coyoteTimer - dt).coerceAtLeast(0f)
        }
        jumpBufferTimer = (jumpBufferTimer - dt).coerceAtLeast(0f)

        // Compute input direction with inverted controls
        var moveDir = 0f
        val effLeft = if (isInvertedControls) inputRight else inputLeft
        val effRight = if (isInvertedControls) inputLeft else inputRight

        if (effLeft && !effRight) {
            moveDir = -1f
            playerFacing = -1f
        } else if (effRight && !effLeft) {
            moveDir = 1f
            playerFacing = 1f
        }

        // Smooth horizontal acceleration & deceleration (momentum / silkiness)
        val targetVx = moveDir * moveSpeed
        val accel = if (moveDir != 0f) 18f else 15f
        playerVx += (targetVx - playerVx) * (dt * accel)

        // Smooth tilt lean in direction of movement
        val targetTilt = (playerVx / moveSpeed) * 8f
        playerTilt += (targetTilt - playerTilt) * (dt * 14f)

        // Walk animation timer
        if (playerGrounded && kotlin.math.abs(playerVx) > 15f) {
            walkAnimTimer += dt * 14f
        } else {
            walkAnimTimer = 0f
        }

        // Jump execution
        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            val impulse = if (isGravityInverted) -jumpImpulse else jumpImpulse
            playerVy = impulse
            coyoteTimer = 0f
            jumpBufferTimer = 0f
            playerGrounded = false
            playerSquashX = 0.82f
            playerSquashY = 1.25f
            soundSynth.playJump()
            haptics.vibrateJump()
        }

        // Apply gravity
        val currentGrav = if (isGravityInverted) -gravity else gravity
        playerVy += currentGrav * dt
        // Terminal velocity clamp
        playerVy = playerVy.coerceIn(-800f, 800f)

        // Update Door physics/logic
        updateDoor(dt)

        // Update Dynamic Platforms
        updatePlatforms(dt)

        // Update Spikes
        updateSpikes(dt)

        // Move player X and resolve collisions
        playerX += playerVx * dt
        resolveCollisionsX()

        // Move player Y and resolve collisions
        playerY += playerVy * dt
        resolveCollisionsY()

        // Screen boundary checks
        if (playerX < 0f) playerX = 0f
        if (playerX + playerWidth > 800f) playerX = 800f - playerWidth

        // Fall out of world death check
        if (playerY > 480f || playerY < -60f) {
            killPlayer("Fell into the abyss!")
            return
        }

        // Check Triggers
        checkTriggers()

        // Check Spike Collisions
        val currentBounds = RectF(playerX, playerY, playerX + playerWidth, playerY + playerHeight)
        for (spike in spikes) {
            val hitbox = spike.getHitbox()
            if (hitbox.intersects(currentBounds)) {
                killPlayer("Ouch! Spiked!")
                return
            }
        }

        // Check Door Intersection
        if (door.bounds.intersects(currentBounds)) {
            if (door.type == DoorType.FAKE_MIMIC) {
                // Troll mimic door!
                soundSynth.playTroll()
                haptics.vibrateTrap()
                activeMessage = "MIMIC! IT'S A TRAP!"
                messageTimer = 3f
                playerEyeShock = 1f
                killPlayer("The door was a hungry mimic!")
            } else {
                // Real door win!
                playerState = PlayerState.CELEBRATING
                celebrationTimer = 0f
                soundSynth.playWin()
                haptics.vibrateWin()
                val elapsed = System.currentTimeMillis() - levelStartTime
                onLevelCompleted(currentLevelDef.levelNumber, levelDeaths, elapsed)
            }
        }
    }

    private fun updateDoor(dt: Float) {
        val playerCenterX = playerX + playerWidth / 2f
        val playerCenterY = playerY + playerHeight / 2f
        val doorCenterX = door.x + door.width / 2f
        val doorCenterY = door.y + door.height / 2f
        val dist = hypot(playerCenterX - doorCenterX, playerCenterY - doorCenterY)

        when (door.type) {
            DoorType.RUNAWAY -> {
                if (dist < door.triggerDistance && !door.isTriggered) {
                    door.isTriggered = true
                    soundSynth.playTrapTrigger()
                    haptics.vibrateTrap()
                    playerEyeShock = 1f
                }
                if (door.isTriggered) {
                    val dx = door.runawayTargetX - door.x
                    val dy = door.runawayTargetY - door.y
                    door.x += dx * (dt * 7f)
                    door.y += dy * (dt * 7f)
                }
            }
            DoorType.JUMPER -> {
                if (!door.isGrounded) {
                    door.jumpVy += gravity * dt
                    door.y += door.jumpVy * dt
                    if (door.y >= 302f) {
                        door.y = 302f
                        door.jumpVy = 0f
                        door.isGrounded = true
                    }
                }
            }
            DoorType.TELEPORT -> {
                if (dist < door.triggerDistance && !door.isTriggered) {
                    door.isTriggered = true
                    door.x = door.runawayTargetX
                    door.y = door.runawayTargetY
                    soundSynth.playTrapTrigger()
                    haptics.vibrateTrap()
                    playerEyeShock = 1f
                    activeMessage = "WHOOSH!"
                    messageTimer = 1.5f
                }
            }
            else -> {}
        }

        // Door glow oscillation
        door.glowPhase = (door.glowPhase + dt * 4f) % (2f * Math.PI.toFloat())
    }

    private fun updatePlatforms(dt: Float) {
        val playerBounds = RectF(playerX, playerY, playerX + playerWidth, playerY + playerHeight)

        for (p in platforms) {
            when (p.type) {
                PlatformType.MOVING_H -> {
                    p.x += p.moveSpeed * p.moveDir * dt
                    if (p.x <= p.minX) {
                        p.x = p.minX
                        p.moveDir = 1f
                    } else if (p.x >= p.maxX) {
                        p.x = p.maxX
                        p.moveDir = -1f
                    }
                }
                PlatformType.MOVING_V -> {
                    p.y += p.moveSpeed * p.moveDir * dt
                    if (p.y <= p.minY) {
                        p.y = p.minY
                        p.moveDir = 1f
                    } else if (p.y >= p.maxY) {
                        p.y = p.maxY
                        p.moveDir = -1f
                    }
                }
                PlatformType.CRUMBLING -> {
                    if (p.hasTriggered) {
                        p.triggerTimer += dt
                        if (p.triggerTimer > 0.42f) {
                            p.fallVy += gravity * 1.1f * dt
                            p.y += p.fallVy * dt
                        }
                    }
                }
                PlatformType.SINKING -> {
                    if (p.hasTriggered) {
                        p.y += 32f * dt
                    }
                }
                else -> {}
            }
        }
    }

    private fun updateSpikes(dt: Float) {
        val playerCenterX = playerX + playerWidth / 2f
        val playerCenterY = playerY + playerHeight / 2f

        for (spike in spikes) {
            if (spike.isSneaky && !spike.isTriggered) {
                val spikeCenterX = spike.x + spike.width / 2f
                val spikeCenterY = spike.y + spike.height / 2f
                val d = hypot(playerCenterX - spikeCenterX, playerCenterY - spikeCenterY)
                if (d < spike.triggerDistance) {
                    spike.isTriggered = true
                    soundSynth.playTrapTrigger()
                    haptics.vibrateTrap()
                    playerEyeShock = 1f
                }
            }

            if (spike.isTriggered && spike.popProgress < 1f) {
                spike.popProgress = (spike.popProgress + dt * 7.5f).coerceAtMost(1f)
            }

            if (spike.isMoving) {
                spike.y += spike.moveSpeed * spike.moveDir * dt
                if (spike.y <= spike.moveMin) {
                    spike.y = spike.moveMin
                    spike.moveDir = 1f
                } else if (spike.y >= spike.moveMax) {
                    spike.y = spike.moveMax
                    spike.moveDir = -1f
                }
            }
        }
    }

    private fun checkTriggers() {
        val playerBounds = RectF(playerX, playerY, playerX + playerWidth, playerY + playerHeight)
        for (trigger in triggers) {
            if (!trigger.isTriggered && trigger.bounds.intersects(playerBounds)) {
                trigger.isTriggered = true
                if (trigger.message.isNotEmpty()) {
                    activeMessage = trigger.message
                    messageTimer = 3f
                    soundSynth.playTrapTrigger()
                    haptics.vibrateTrap()
                }

                when (trigger.action) {
                    TriggerAction.FALLING_BLOCK -> {
                        val target = platforms.firstOrNull { it.id == trigger.targetId }
                        if (target != null) {
                            target.hasTriggered = true
                            target.fallVy = 390f
                            soundSynth.playTrapTrigger()
                            haptics.vibrateTrap()
                            playerEyeShock = 1f
                        }
                    }
                    TriggerAction.INVERT_CONTROLS -> {
                        isInvertedControls = !isInvertedControls
                        soundSynth.playTroll()
                        haptics.vibrateTrap()
                        playerEyeShock = 1f
                    }
                    TriggerAction.REVERSE_GRAVITY -> {
                        isGravityInverted = !isGravityInverted
                        playerVy = if (isGravityInverted) -200f else 200f
                        soundSynth.playTrapTrigger()
                        haptics.vibrateTrap()
                        playerEyeShock = 1f
                    }
                    TriggerAction.POP_SPIKE_GROUP -> {
                        spikes.forEach { s ->
                            if (s.isSneaky) {
                                s.isTriggered = true
                            }
                        }
                        soundSynth.playTrapTrigger()
                    }
                    TriggerAction.REVEAL_SECRET_DOOR -> {
                        door.x = 290f
                        door.y = 110f
                        door.isReal = true
                        soundSynth.playTrapTrigger()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun resolveCollisionsX() {
        val playerBounds = RectF(playerX, playerY, playerX + playerWidth, playerY + playerHeight)
        for (p in platforms) {
            if (!p.isSolid) continue
            val pb = p.bounds
            if (playerBounds.intersects(pb)) {
                if (playerVx > 0f) {
                    playerX = pb.left - playerWidth
                    playerVx = 0f
                } else if (playerVx < 0f) {
                    playerX = pb.right
                    playerVx = 0f
                }
            }
        }
    }

    private fun resolveCollisionsY() {
        val playerBounds = RectF(playerX, playerY, playerX + playerWidth, playerY + playerHeight)
        var hitGrounded = false

        for (p in platforms) {
            if (!p.isSolid) continue
            val pb = p.bounds
            if (playerBounds.intersects(pb)) {
                if (!isGravityInverted) {
                    if (playerVy > 0f) {
                        playerY = pb.top - playerHeight
                        playerVy = 0f
                        hitGrounded = true

                        // Check special platform types
                        if (p.type == PlatformType.BOUNCE) {
                            playerVy = -680f
                            hitGrounded = false
                            playerSquashX = 0.65f
                            playerSquashY = 1.45f
                            soundSynth.playJump()
                            haptics.vibrateJump()
                        } else {
                            if (!playerGrounded) {
                                playerSquashX = 1.35f
                                playerSquashY = 0.70f
                                soundSynth.playLand()
                            }
                            if (p.type == PlatformType.CRUMBLING || p.type == PlatformType.SINKING) {
                                p.hasTriggered = true
                            }
                        }
                    } else if (playerVy < 0f) {
                        playerY = pb.bottom
                        playerVy = 0f
                        playerSquashX = 1.25f
                        playerSquashY = 0.8f
                    }
                } else {
                    // Inverted gravity
                    if (playerVy < 0f) {
                        playerY = pb.bottom
                        playerVy = 0f
                        hitGrounded = true
                        if (!playerGrounded) {
                            playerSquashX = 1.35f
                            playerSquashY = 0.70f
                            soundSynth.playLand()
                        }
                    } else if (playerVy > 0f) {
                        playerY = pb.top - playerHeight
                        playerVy = 0f
                    }
                }
            }
        }
        playerGrounded = hitGrounded
    }

    fun killPlayer(reason: String = "") {
        if (playerState != PlayerState.ALIVE) return
        playerState = PlayerState.DEAD
        deathTimer = 0f
        levelDeaths++
        onDeathRecorded(currentLevelDef.levelNumber)

        soundSynth.playDeath()
        haptics.vibrateDeath()

        // Generate burst of 16 white debris particles
        deathParticles.clear()
        val random = Random(System.currentTimeMillis())
        val centerX = playerX + playerWidth / 2f
        val centerY = playerY + playerHeight / 2f

        for (i in 0 until 16) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = random.nextFloat() * 260f + 80f
            deathParticles.add(
                DeathParticle(
                    x = centerX,
                    y = centerY,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed - 120f,
                    size = random.nextFloat() * 5f + 4f,
                    rotation = random.nextFloat() * 360f,
                    vRot = (random.nextFloat() - 0.5f) * 720f,
                    alpha = 1f
                )
            )
        }
    }

    private fun updateDead(dt: Float) {
        deathTimer += dt
        // Update particles
        for (part in deathParticles) {
            part.x += part.vx * dt
            part.y += part.vy * dt
            part.vy += 800f * dt
            part.rotation += part.vRot * dt
            part.alpha = (1f - (deathTimer / 0.45f)).coerceIn(0f, 1f)
        }

        // Immediate snappy respawn after 0.45s
        if (deathTimer >= 0.45f) {
            restartLevel(isFullReset = false)
        }
    }

    private fun updateCelebrating(dt: Float) {
        celebrationTimer += dt
        // Shrink and sucked into door
        val targetX = door.x + door.width / 2f - playerWidth / 2f
        val targetY = door.y + door.height / 2f - playerHeight / 2f
        playerX += (targetX - playerX) * (dt * 8f)
        playerY += (targetY - playerY) * (dt * 8f)
        playerSquashX = (playerSquashX - dt * 1.5f).coerceAtLeast(0.05f)
        playerSquashY = (playerSquashY - dt * 1.5f).coerceAtLeast(0.05f)
        door.openProgress = (door.openProgress + dt * 3f).coerceAtMost(1f)
    }
}
