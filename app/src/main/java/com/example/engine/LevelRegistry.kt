package com.example.engine

object LevelRegistry {
    const val TOTAL_LEVELS = 20

    fun getLevel(levelNumber: Int): LevelDef {
        return when (levelNumber) {
            1 -> createLevel1()
            2 -> createLevel2()
            3 -> createLevel3()
            4 -> createLevel4()
            5 -> createLevel5()
            6 -> createLevel6()
            7 -> createLevel7()
            8 -> createLevel8()
            9 -> createLevel9()
            10 -> createLevel10()
            11 -> createLevel11()
            12 -> createLevel12()
            13 -> createLevel13()
            14 -> createLevel14()
            15 -> createLevel15()
            16 -> createLevel16()
            17 -> createLevel17()
            18 -> createLevel18()
            19 -> createLevel19()
            20 -> createLevel20()
            else -> createLevel1()
        }
    }

    // LEVEL 1: "The Warmup"
    // Ground looks flat and easy, but right in front of the door, a section drops into an abyss!
    private fun createLevel1(): LevelDef {
        return LevelDef(
            levelNumber = 1,
            title = "A Simple Walk",
            subtitle = "Just walk to the door. What could go wrong?",
            spawnX = 80f,
            spawnY = 300f,
            door = Door(x = 700f, y = 300f),
            platforms = listOf(
                // Boundary walls
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                // Floor start
                Platform(x = 30f, y = 350f, width = 520f, height = 100f),
                // Crumbling trap floor right before door (moderate 90f gap)
                Platform(
                    id = "trap_floor",
                    x = 550f,
                    y = 350f,
                    width = 90f,
                    height = 100f,
                    type = PlatformType.CRUMBLING
                ),
                // Door ledge
                Platform(x = 640f, y = 350f, width = 130f, height = 100f)
            ),
            spikes = listOf(
                Spike(x = 575f, y = 430f, width = 24f, height = 20f),
                Spike(x = 600f, y = 430f, width = 24f, height = 20f)
            )
        )
    }

    // LEVEL 2: "Runaway Door"
    // Door runs away backwards when you get close!
    private fun createLevel2(): LevelDef {
        return LevelDef(
            levelNumber = 2,
            title = "Runaway Door",
            subtitle = "Wait... where are you going?!",
            spawnX = 100f,
            spawnY = 300f,
            door = Door(
                id = "door_runaway",
                x = 680f,
                y = 300f,
                type = DoorType.RUNAWAY,
                runawayTargetX = 90f,
                runawayTargetY = 300f,
                triggerDistance = 140f
            ),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                // Main floor
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Gentle middle hurdle
                Platform(x = 390f, y = 295f, width = 35f, height = 55f)
            ),
            spikes = listOf(
                Spike(x = 360f, y = 328f, width = 24f, height = 22f),
                Spike(x = 430f, y = 328f, width = 24f, height = 22f)
            )
        )
    }

    // LEVEL 3: "Pop Goes The Floor"
    // Floor looks harmless, but sneaky spikes pop straight up under your feet!
    private fun createLevel3(): LevelDef {
        return LevelDef(
            levelNumber = 3,
            title = "Sneaky Spikes",
            subtitle = "The floor has trust issues.",
            spawnX = 80f,
            spawnY = 300f,
            door = Door(x = 700f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f)
            ),
            spikes = listOf(
                // Sneaky spikes with generous advance notice trigger
                Spike(x = 280f, y = 326f, width = 24f, height = 24f, isSneaky = true, triggerDistance = 90f),
                Spike(x = 450f, y = 326f, width = 24f, height = 24f, isSneaky = true, triggerDistance = 95f),
                Spike(x = 600f, y = 326f, width = 24f, height = 24f, isSneaky = true, triggerDistance = 95f)
            )
        )
    }

    // LEVEL 4: "Falling Sky"
    // Ceiling blocks drop when passing underneath
    private fun createLevel4(): LevelDef {
        return LevelDef(
            levelNumber = 4,
            title = "Falling Sky",
            subtitle = "Don't look down. Look up!",
            spawnX = 80f,
            spawnY = 300f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Guillotine falling blocks
                Platform(id = "drop1", x = 270f, y = 30f, width = 45f, height = 120f),
                Platform(id = "drop2", x = 480f, y = 30f, width = 45f, height = 120f)
            ),
            triggers = listOf(
                LevelTrigger(
                    id = "trig_drop1",
                    bounds = RectF(220f, 200f, 260f, 350f),
                    action = TriggerAction.FALLING_BLOCK,
                    targetId = "drop1"
                ),
                LevelTrigger(
                    id = "trig_drop2",
                    bounds = RectF(430f, 200f, 470f, 350f),
                    action = TriggerAction.FALLING_BLOCK,
                    targetId = "drop2"
                )
            )
        )
    }

    // LEVEL 5: "The Crumbling Bridge"
    // Fast-falling bridge stones. Run and never look back!
    private fun createLevel5(): LevelDef {
        val stones = (0..5).map { i ->
            Platform(
                id = "bridge_$i",
                x = 180f + i * 85f,
                y = 350f,
                width = 65f,
                height = 25f,
                type = PlatformType.CRUMBLING
            )
        }
        return LevelDef(
            levelNumber = 5,
            title = "The Collapsing Bridge",
            subtitle = "Hesitation is defeat.",
            spawnX = 70f,
            spawnY = 300f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                // Start island
                Platform(x = 30f, y = 350f, width = 130f, height = 100f),
                // End island
                Platform(x = 670f, y = 350f, width = 100f, height = 100f)
            ) + stones,
            spikes = (0..15).map { i ->
                Spike(x = 150f + i * 34f, y = 425f, width = 24f, height = 25f)
            }
        )
    }

    // LEVEL 6: "Opposite Day"
    // Controls reverse halfway through!
    private fun createLevel6(): LevelDef {
        return LevelDef(
            levelNumber = 6,
            title = "Opposite Day",
            subtitle = "Left is right. Right is wrong.",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Middle platform
                Platform(x = 420f, y = 260f, width = 60f, height = 20f)
            ),
            spikes = listOf(
                Spike(x = 520f, y = 326f, width = 24f, height = 24f),
                Spike(x = 544f, y = 326f, width = 24f, height = 24f)
            ),
            triggers = listOf(
                LevelTrigger(
                    id = "invert_ctrl",
                    bounds = RectF(360f, 200f, 400f, 350f),
                    action = TriggerAction.INVERT_CONTROLS,
                    message = "OPPOSITE DAY! CONTROLS REVERSED!"
                )
            )
        )
    }

    // LEVEL 7: "The Mimic"
    // Door on ground is a fake mimic that laughs at you. Real door appears on the top ceiling ledge!
    private fun createLevel7(): LevelDef {
        return LevelDef(
            levelNumber = 7,
            title = "The Mimic",
            subtitle = "Not every door wants you to leave.",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(
                id = "fake_door",
                x = 680f,
                y = 300f,
                type = DoorType.FAKE_MIMIC,
                isReal = false
            ),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Hidden upper ledge with real door
                Platform(id = "secret_ledge", x = 250f, y = 160f, width = 120f, height = 20f)
            ),
            triggers = listOf(
                LevelTrigger(
                    id = "reveal_real",
                    bounds = RectF(600f, 250f, 670f, 350f),
                    action = TriggerAction.REVEAL_SECRET_DOOR,
                    message = "HA! IT'S A MIMIC! REAL DOOR IS ABOVE!"
                )
            )
        )
    }

    // LEVEL 8: "The Jumping Door"
    // Door jumps into the air whenever you jump!
    private fun createLevel8(): LevelDef {
        return LevelDef(
            levelNumber = 8,
            title = "The Jumping Door",
            subtitle = "You jump, it jumps.",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(
                id = "door_jumper",
                x = 650f,
                y = 302f,
                type = DoorType.JUMPER
            ),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Step to trap the door
                Platform(x = 550f, y = 240f, width = 40f, height = 110f)
            ),
            spikes = listOf(
                Spike(x = 380f, y = 326f, width = 24f, height = 24f),
                Spike(x = 404f, y = 326f, width = 24f, height = 24f)
            )
        )
    }

    // LEVEL 9: "Upside Down"
    // Gravity reverses! Walk on ceiling to reach exit.
    private fun createLevel9(): LevelDef {
        return LevelDef(
            levelNumber = 9,
            title = "Upside Down",
            subtitle = "What goes up... stays up.",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(
                id = "ceiling_door",
                x = 680f,
                y = 30f,
                type = DoorType.CEILING
            ),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                // Floor first half
                Platform(x = 30f, y = 350f, width = 320f, height = 100f),
                // Ceiling second half
                Platform(x = 320f, y = 30f, width = 450f, height = 40f)
            ),
            spikes = listOf(
                // Ceiling spikes to avoid
                Spike(x = 480f, y = 70f, width = 24f, height = 24f, direction = SpikeDirection.DOWN),
                Spike(x = 504f, y = 70f, width = 24f, height = 24f, direction = SpikeDirection.DOWN)
            ),
            triggers = listOf(
                LevelTrigger(
                    id = "grav_trig",
                    bounds = RectF(280f, 200f, 320f, 350f),
                    action = TriggerAction.REVERSE_GRAVITY,
                    message = "GRAVITY REVERSED!"
                )
            )
        )
    }

    // LEVEL 10: "Disappearing Steps"
    // Stepping stones that blink in and out
    private fun createLevel10(): LevelDef {
        return LevelDef(
            levelNumber = 10,
            title = "Ghost Steps",
            subtitle = "Now you see them...",
            spawnX = 70f,
            spawnY = 320f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 100f, height = 100f),
                // Blinking platforms
                Platform(x = 180f, y = 330f, width = 70f, height = 20f, type = PlatformType.DISAPPEARING),
                Platform(x = 320f, y = 290f, width = 70f, height = 20f, type = PlatformType.DISAPPEARING),
                Platform(x = 460f, y = 270f, width = 70f, height = 20f, type = PlatformType.DISAPPEARING),
                Platform(x = 590f, y = 310f, width = 70f, height = 20f, type = PlatformType.DISAPPEARING),
                Platform(x = 680f, y = 350f, width = 90f, height = 100f)
            ),
            spikes = (0..16).map { i ->
                Spike(x = 130f + i * 34f, y = 425f, width = 24f, height = 25f)
            }
        )
    }

    // LEVEL 11: "Elevator of Doom"
    // Elevator rockets upward directly into ceiling spikes! Hop off quickly!
    private fun createLevel11(): LevelDef {
        return LevelDef(
            levelNumber = 11,
            title = "Elevator of Doom",
            subtitle = "Exit on floor 2... BEFORE FLOOR 3!",
            spawnX = 70f,
            spawnY = 320f,
            door = Door(x = 690f, y = 140f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 120f, height = 100f),
                // Vertical Elevator
                Platform(
                    id = "elevator",
                    x = 220f,
                    y = 350f,
                    width = 80f,
                    height = 20f,
                    type = PlatformType.MOVING_V,
                    minY = 70f,
                    maxY = 350f,
                    moveSpeed = 160f
                ),
                // Safe mid-exit ledge
                Platform(x = 360f, y = 200f, width = 140f, height = 20f),
                // Door ledge
                Platform(x = 650f, y = 190f, width = 120f, height = 20f)
            ),
            spikes = listOf(
                // Ceiling spikes above the elevator!
                Spike(x = 220f, y = 30f, width = 24f, height = 30f, direction = SpikeDirection.DOWN),
                Spike(x = 250f, y = 30f, width = 24f, height = 30f, direction = SpikeDirection.DOWN),
                Spike(x = 280f, y = 30f, width = 24f, height = 30f, direction = SpikeDirection.DOWN),
                // Floor pit
                Spike(x = 180f, y = 425f, width = 24f, height = 25f),
                Spike(x = 540f, y = 425f, width = 24f, height = 25f)
            )
        )
    }

    // LEVEL 12: "The Wave of Spikes"
    // Moving wave spikes undulating up and down
    private fun createLevel12(): LevelDef {
        val waveSpikes = (0..5).map { i ->
            Spike(
                id = "wave_$i",
                x = 220f + i * 70f,
                y = 326f,
                width = 24f,
                height = 24f,
                isMoving = true,
                moveMin = 270f,
                moveMax = 330f,
                moveSpeed = 70f + i * 15f
            )
        }
        return LevelDef(
            levelNumber = 12,
            title = "The Wave",
            subtitle = "Surfing isn't advised.",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f)
            ),
            spikes = waveSpikes
        )
    }

    // LEVEL 13: "Low Ceiling"
    // Head bonker spikes on low ceiling!
    private fun createLevel13(): LevelDef {
        return LevelDef(
            levelNumber = 13,
            title = "Watch Your Head",
            subtitle = "Short hops only!",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Low ceiling slab
                Platform(x = 180f, y = 240f, width = 450f, height = 30f)
            ),
            spikes = listOf(
                // Ceiling spikes under the slab
                Spike(x = 240f, y = 270f, width = 24f, height = 20f, direction = SpikeDirection.DOWN),
                Spike(x = 340f, y = 270f, width = 24f, height = 20f, direction = SpikeDirection.DOWN),
                Spike(x = 440f, y = 270f, width = 24f, height = 20f, direction = SpikeDirection.DOWN),
                Spike(x = 540f, y = 270f, width = 24f, height = 20f, direction = SpikeDirection.DOWN),
                // Floor hurdles
                Spike(x = 290f, y = 326f, width = 24f, height = 24f),
                Spike(x = 490f, y = 326f, width = 24f, height = 24f)
            )
        )
    }

    // LEVEL 14: "The Sinking Hall"
    // Floor sinks into lava pit!
    private fun createLevel14(): LevelDef {
        return LevelDef(
            levelNumber = 14,
            title = "The Sinking Hall",
            subtitle = "Hurry up!",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(x = 710f, y = 200f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                // Sinking floors
                Platform(id = "sink1", x = 30f, y = 350f, width = 200f, height = 100f, type = PlatformType.SINKING),
                Platform(id = "sink2", x = 250f, y = 330f, width = 180f, height = 100f, type = PlatformType.SINKING),
                Platform(id = "sink3", x = 450f, y = 300f, width = 180f, height = 100f, type = PlatformType.SINKING),
                // Door high pillar
                Platform(x = 660f, y = 250f, width = 110f, height = 200f)
            ),
            spikes = (0..16).map { i ->
                Spike(x = 30f + i * 45f, y = 425f, width = 24f, height = 25f)
            }
        )
    }

    // LEVEL 15: "Trust Issues"
    // Arrow points right to an inescapable spike wall; left wall is fake walk-through!
    private fun createLevel15(): LevelDef {
        return LevelDef(
            levelNumber = 15,
            title = "Trust Issues",
            subtitle = "Don't believe the signs.",
            spawnX = 200f,
            spawnY = 320f,
            door = Door(x = 50f, y = 140f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                // Main floor
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Fake wall on left
                Platform(x = 100f, y = 220f, width = 30f, height = 130f, type = PlatformType.FAKE),
                // Upper secret door ledge
                Platform(x = 30f, y = 190f, width = 120f, height = 20f)
            ),
            spikes = listOf(
                // Dead end death spikes on the right!
                Spike(x = 640f, y = 326f, width = 24f, height = 24f),
                Spike(x = 664f, y = 326f, width = 24f, height = 24f),
                Spike(x = 688f, y = 326f, width = 24f, height = 24f),
                Spike(x = 712f, y = 326f, width = 24f, height = 24f)
            ),
            trollMessage = "DOOR ->"
        )
    }

    // LEVEL 16: "Bounce Frenzy"
    // High bounce pads with spikes everywhere!
    private fun createLevel16(): LevelDef {
        return LevelDef(
            levelNumber = 16,
            title = "Super Bounce",
            subtitle = "Control your landing!",
            spawnX = 70f,
            spawnY = 320f,
            door = Door(x = 710f, y = 120f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 100f, height = 100f),
                // Bounce pads
                Platform(x = 180f, y = 350f, width = 60f, height = 20f, type = PlatformType.BOUNCE),
                Platform(x = 360f, y = 350f, width = 60f, height = 20f, type = PlatformType.BOUNCE),
                Platform(x = 540f, y = 350f, width = 60f, height = 20f, type = PlatformType.BOUNCE),
                // Door perch
                Platform(x = 670f, y = 170f, width = 100f, height = 20f)
            ),
            spikes = listOf(
                // Spikes on floor between pads
                Spike(x = 250f, y = 425f, width = 24f, height = 25f),
                Spike(x = 280f, y = 425f, width = 24f, height = 25f),
                Spike(x = 440f, y = 425f, width = 24f, height = 25f),
                Spike(x = 470f, y = 425f, width = 24f, height = 25f),
                // Mid-air hazard blocks
                Spike(x = 270f, y = 160f, width = 24f, height = 24f, direction = SpikeDirection.DOWN),
                Spike(x = 450f, y = 160f, width = 24f, height = 24f, direction = SpikeDirection.DOWN)
            )
        )
    }

    // LEVEL 17: "Teleport Tantrum"
    // Door teleports 2 times before you can catch it!
    private fun createLevel17(): LevelDef {
        return LevelDef(
            levelNumber = 17,
            title = "Teleport Tantrum",
            subtitle = "Catch me if you can!",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(
                id = "teleport_door",
                x = 680f,
                y = 300f,
                type = DoorType.TELEPORT,
                runawayTargetX = 400f,
                runawayTargetY = 160f,
                triggerDistance = 120f
            ),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                // Floating perch
                Platform(x = 360f, y = 210f, width = 120f, height = 20f)
            ),
            spikes = listOf(
                Spike(x = 280f, y = 326f, width = 24f, height = 24f),
                Spike(x = 520f, y = 326f, width = 24f, height = 24f)
            )
        )
    }

    // LEVEL 18: "Spike Chase"
    // Spiked wall pursues from the left!
    private fun createLevel18(): LevelDef {
        return LevelDef(
            levelNumber = 18,
            title = "Spike Rush",
            subtitle = "Don't look behind you.",
            spawnX = 140f,
            spawnY = 320f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 740f, height = 100f),
                Platform(x = 300f, y = 290f, width = 40f, height = 60f),
                Platform(x = 480f, y = 270f, width = 40f, height = 80f)
            ),
            spikes = listOf(
                // Chasing spike moving right
                Spike(
                    id = "chaser",
                    x = 40f,
                    y = 300f,
                    width = 40f,
                    height = 50f,
                    isMoving = true,
                    moveMin = 40f,
                    moveMax = 650f,
                    moveSpeed = 65f
                ),
                Spike(x = 390f, y = 326f, width = 24f, height = 24f)
            )
        )
    }

    // LEVEL 19: "Reverse & Drop"
    // Inverted controls with falling ceilings and crumbling steps
    private fun createLevel19(): LevelDef {
        return LevelDef(
            levelNumber = 19,
            title = "Chaos Theory",
            subtitle = "Everything is backwards and falling.",
            spawnX = 80f,
            spawnY = 320f,
            door = Door(x = 710f, y = 300f),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 200f, height = 100f),
                Platform(id = "c1", x = 270f, y = 350f, width = 70f, height = 20f, type = PlatformType.CRUMBLING),
                Platform(id = "c2", x = 400f, y = 330f, width = 70f, height = 20f, type = PlatformType.CRUMBLING),
                Platform(id = "c3", x = 530f, y = 310f, width = 70f, height = 20f, type = PlatformType.CRUMBLING),
                Platform(x = 650f, y = 350f, width = 120f, height = 100f),
                Platform(id = "drop_19", x = 400f, y = 30f, width = 50f, height = 120f)
            ),
            spikes = (0..10).map { i ->
                Spike(x = 240f + i * 40f, y = 425f, width = 24f, height = 25f)
            },
            triggers = listOf(
                LevelTrigger(
                    id = "invert_19",
                    bounds = RectF(180f, 200f, 220f, 350f),
                    action = TriggerAction.INVERT_CONTROLS,
                    message = "CONTROLS INVERTED!"
                ),
                LevelTrigger(
                    id = "trig_drop_19",
                    bounds = RectF(360f, 200f, 420f, 350f),
                    action = TriggerAction.FALLING_BLOCK,
                    targetId = "drop_19"
                )
            )
        )
    }

    // LEVEL 20: "The Forge Master"
    // The ultimate troll finale!
    private fun createLevel20(): LevelDef {
        return LevelDef(
            levelNumber = 20,
            title = "The Forge Master",
            subtitle = "Survive the ultimate gauntlet.",
            spawnX = 70f,
            spawnY = 320f,
            door = Door(
                id = "finale_door",
                x = 680f,
                y = 300f,
                type = DoorType.RUNAWAY,
                runawayTargetX = 390f,
                runawayTargetY = 160f,
                triggerDistance = 140f
            ),
            platforms = listOf(
                Platform(x = 0f, y = 0f, width = 30f, height = 450f),
                Platform(x = 770f, y = 0f, width = 30f, height = 450f),
                Platform(x = 0f, y = 0f, width = 800f, height = 30f),
                Platform(x = 30f, y = 350f, width = 140f, height = 100f),
                Platform(id = "fin_c1", x = 210f, y = 350f, width = 60f, height = 20f, type = PlatformType.CRUMBLING),
                Platform(x = 310f, y = 330f, width = 60f, height = 20f, type = PlatformType.BOUNCE),
                Platform(x = 360f, y = 210f, width = 120f, height = 20f),
                Platform(id = "fin_drop", x = 520f, y = 30f, width = 50f, height = 140f),
                Platform(x = 500f, y = 350f, width = 270f, height = 100f)
            ),
            spikes = listOf(
                Spike(x = 210f, y = 425f, width = 24f, height = 25f),
                Spike(x = 550f, y = 326f, width = 24f, height = 24f, isSneaky = true, triggerDistance = 80f),
                Spike(x = 600f, y = 326f, width = 24f, height = 24f, isSneaky = true, triggerDistance = 80f)
            ),
            triggers = listOf(
                LevelTrigger(
                    id = "trig_fin_drop",
                    bounds = RectF(480f, 200f, 540f, 350f),
                    action = TriggerAction.FALLING_BLOCK,
                    targetId = "fin_drop"
                )
            )
        )
    }
}
