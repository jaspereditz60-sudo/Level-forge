package com.example.engine

data class RectF(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun intersects(other: RectF): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}

enum class PlayerState {
    ALIVE,
    CELEBRATING,
    DEAD
}

data class DeathParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var rotation: Float,
    var vRot: Float,
    var alpha: Float = 1f
)

enum class PlatformType {
    SOLID,
    CRUMBLING,      // Falls shortly after stepped on
    SINKING,        // Sinks continuously while stepped on
    MOVING_H,       // Moves back and forth horizontally
    MOVING_V,       // Moves vertically
    BOUNCE,         // Super bouncy launcher
    FAKE,           // Passes straight through
    DISAPPEARING    // Blinks on timer
}

data class Platform(
    val id: String = "",
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: PlatformType = PlatformType.SOLID,
    val minX: Float = x,
    val maxX: Float = x,
    val minY: Float = y,
    val maxY: Float = y,
    var moveSpeed: Float = 80f,
    var moveDir: Float = 1f,
    var triggerTimer: Float = 0f,
    var hasTriggered: Boolean = false,
    var fallVy: Float = 0f,
    var isVisible: Boolean = true,
    var isSolid: Boolean = true,
    val isHiddenWall: Boolean = false
) {
    val bounds: RectF get() = RectF(x, y, x + width, y + height)
}

enum class SpikeDirection {
    UP, DOWN, LEFT, RIGHT
}

data class Spike(
    val id: String = "",
    var x: Float,
    var y: Float,
    val width: Float = 24f,
    val height: Float = 24f,
    val direction: SpikeDirection = SpikeDirection.UP,
    val isSneaky: Boolean = false,
    val triggerDistance: Float = 80f,
    var popProgress: Float = if (isSneaky) 0f else 1f,
    var isTriggered: Boolean = false,
    var triggerDelay: Float = 0f,
    var isMoving: Boolean = false,
    var moveSpeed: Float = 0f,
    var moveMin: Float = 0f,
    var moveMax: Float = 0f,
    var moveDir: Float = 1f
) {
    val currentHeight: Float get() = height * popProgress
    val currentWidth: Float get() = width * popProgress

    fun getHitbox(): RectF {
        if (popProgress <= 0.08f) return RectF(0f, 0f, 0f, 0f)
        val insetX = 6f
        val insetY = 5f
        return when (direction) {
            SpikeDirection.UP -> RectF(
                x + insetX,
                y + height * (1f - popProgress) + insetY,
                x + width - insetX,
                y + height
            )
            SpikeDirection.DOWN -> RectF(
                x + insetX,
                y,
                x + width - insetX,
                y + height * popProgress - insetY
            )
            SpikeDirection.LEFT -> RectF(
                x + width * (1f - popProgress) + insetY,
                y + insetX,
                x + width,
                y + height - insetX
            )
            SpikeDirection.RIGHT -> RectF(
                x,
                y + insetX,
                x + width * popProgress - insetY,
                y + height - insetX
            )
        }
    }
}

enum class DoorType {
    NORMAL,
    RUNAWAY,         // Slides away horizontally when approached
    JUMPER,          // Jumps when player jumps or approaches
    FAKE_MIMIC,      // Explodes/laughs when reached; real door is elsewhere
    SINKING,         // Sinks down when player gets close
    TELEPORT,        // Teleports to a secondary spot
    CEILING          // Suspended upside down
}

data class Door(
    val id: String = "door",
    var x: Float,
    var y: Float,
    val width: Float = 36f,
    val height: Float = 48f,
    val type: DoorType = DoorType.NORMAL,
    val runawayTargetX: Float = x,
    val runawayTargetY: Float = y,
    val triggerDistance: Float = 100f,
    var isTriggered: Boolean = false,
    var isReal: Boolean = true,
    var jumpVy: Float = 0f,
    var isGrounded: Boolean = true,
    var openProgress: Float = 0f,
    var glowPhase: Float = 0f
) {
    val bounds: RectF get() = RectF(x, y, x + width, y + height)
}

enum class TriggerAction {
    FALLING_BLOCK,
    INVERT_CONTROLS,
    REVERSE_GRAVITY,
    POP_SPIKE_GROUP,
    SHOW_MESSAGE,
    REVEAL_SECRET_DOOR,
    SINK_FLOOR_SPEED
}

data class LevelTrigger(
    val id: String = "",
    val bounds: RectF,
    val action: TriggerAction,
    val targetId: String = "",
    val message: String = "",
    val triggerOnce: Boolean = true,
    var isTriggered: Boolean = false
)

data class LevelDef(
    val levelNumber: Int,
    val title: String,
    val subtitle: String = "",
    val spawnX: Float,
    val spawnY: Float,
    val door: Door,
    val platforms: List<Platform>,
    val spikes: List<Spike> = emptyList(),
    val triggers: List<LevelTrigger> = emptyList(),
    val trollMessage: String = "",
    val worldWidth: Float = 800f,
    val worldHeight: Float = 450f,
    val initialInvertControls: Boolean = false,
    val initialGravityInverted: Boolean = false
)
