package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomLevelEntity
import com.example.engine.Door
import com.example.engine.DoorType
import com.example.engine.LevelDef
import com.example.engine.LevelTrigger
import com.example.engine.Platform
import com.example.engine.PlatformType
import com.example.engine.RectF
import com.example.engine.Spike
import com.example.engine.SpikeDirection
import com.example.engine.TriggerAction
import org.json.JSONArray
import org.json.JSONObject

enum class ForgeTool(val title: String) {
    SPAWN("Spawn"),
    DOOR_NORMAL("Door"),
    DOOR_RUNAWAY("Troll Door"),
    SOLID("Solid"),
    CRUMBLE("Crumble"),
    BOUNCE("Bounce"),
    SPIKE_UP("Spike"),
    SNEAKY_SPIKE("Sneaky"),
    DROP_TRAP("Drop Block"),
    ERASER("Eraser")
}

data class ForgeCell(
    val col: Int,
    val row: Int,
    var tool: ForgeTool
)

@Composable
fun CustomForgeScreen(
    customLevels: List<CustomLevelEntity>,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onTestPlay: (LevelDef) -> Unit,
    onSaveLevel: (name: String, json: String) -> Unit,
    onDeleteLevel: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    var levelName by remember { mutableStateOf("My Troll Level") }
    var selectedTool by remember { mutableStateOf(ForgeTool.SOLID) }
    var showSavedDrawer by remember { mutableStateOf(false) }

    // Grid constants: 20 columns x 11 rows in 800x450 world (each cell 40 x 40)
    val cols = 20
    val rows = 11
    val cellW = 40f
    val cellH = 40f

    val placedCells = remember {
        mutableStateListOf<ForgeCell>().apply {
            // Default base floor
            for (c in 0 until cols) {
                add(ForgeCell(c, 9, ForgeTool.SOLID))
            }
            // Default spawn and door
            add(ForgeCell(2, 8, ForgeTool.SPAWN))
            add(ForgeCell(17, 8, ForgeTool.DOOR_NORMAL))
        }
    }

    val bgColor = if (isDarkTheme) Color(0xFF16171E) else Color(0xFFEDE5DA)
    val surfaceColor = if (isDarkTheme) Color(0xFF1E202B) else Color(0xFFFAF7F2)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(
                top = insets.calculateTopPadding(),
                bottom = insets.calculateBottomPadding()
            )
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("button_forge_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LEVEL FORGE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 1.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Clear button
                IconButton(
                    onClick = {
                        placedCells.clear()
                        for (c in 0 until cols) {
                            placedCells.add(ForgeCell(c, 9, ForgeTool.SOLID))
                        }
                        placedCells.add(ForgeCell(2, 8, ForgeTool.SPAWN))
                        placedCells.add(ForgeCell(17, 8, ForgeTool.DOOR_NORMAL))
                    },
                    modifier = Modifier.testTag("button_forge_clear")
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = textColor)
                }

                // Saved Levels Drawer Toggle
                IconButton(
                    onClick = { showSavedDrawer = !showSavedDrawer },
                    modifier = Modifier.testTag("button_forge_saved")
                ) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = "Saved Levels", tint = textColor)
                }

                // Save button
                IconButton(
                    onClick = {
                        val json = serializeForgeToJSON(placedCells)
                        onSaveLevel(levelName.ifBlank { "Troll Level" }, json)
                    },
                    modifier = Modifier.testTag("button_forge_save")
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save Level", tint = Color(0xFF06D6A0))
                }
            }
        }

        // Level Name Input
        OutlinedTextField(
            value = levelName,
            onValueChange = { levelName = it },
            singleLine = true,
            label = { Text("Level Name") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("input_level_name")
        )

        // Saved Levels Row (if drawer toggled open)
        if (showSavedDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .padding(8.dp)
            ) {
                if (customLevels.isEmpty()) {
                    Text(
                        text = "No saved levels yet. Build and tap Save!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(customLevels) { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) Color(0xFF282A36) else Color(0xFFE4DEC8))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.clickable {
                                        levelName = item.name
                                        val loaded = deserializeJSONToForge(item.levelDataJson)
                                        placedCells.clear()
                                        placedCells.addAll(loaded)
                                        showSavedDrawer = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onDeleteLevel(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tools Selector Bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ForgeTool.entries) { tool ->
                val isSelected = tool == selectedTool
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFFFFD166) else surfaceColor)
                        .clickable { selectedTool = tool }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("tool_${tool.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tool.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        color = if (isSelected) Color.Black else textColor
                    )
                }
            }
        }

        // Interactive Editor Grid Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDarkTheme) Color(0xFF13141A) else Color(0xFFDDD2C0))
                .border(
                    width = 2.dp,
                    color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000),
                    shape = RoundedCornerShape(16.dp)
                )
                .testTag("forge_grid_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTool) {
                        detectTapGestures { tapOffset ->
                            val scaleVal = minOf(size.width / (cols * cellW), size.height / (rows * cellH))
                            val ox = (size.width - cols * cellW * scaleVal) / 2f
                            val oy = (size.height - rows * cellH * scaleVal) / 2f

                            val gx = (tapOffset.x - ox) / scaleVal
                            val gy = (tapOffset.y - oy) / scaleVal

                            val col = (gx / cellW).toInt()
                            val row = (gy / cellH).toInt()

                            if (col in 0 until cols && row in 0 until rows) {
                                // If eraser, remove
                                if (selectedTool == ForgeTool.ERASER) {
                                    placedCells.removeAll { it.col == col && it.row == row }
                                } else {
                                    // If spawn or door, ensure uniqueness
                                    if (selectedTool == ForgeTool.SPAWN) {
                                        placedCells.removeAll { it.tool == ForgeTool.SPAWN }
                                    } else if (selectedTool == ForgeTool.DOOR_NORMAL || selectedTool == ForgeTool.DOOR_RUNAWAY) {
                                        placedCells.removeAll { it.tool == ForgeTool.DOOR_NORMAL || it.tool == ForgeTool.DOOR_RUNAWAY }
                                    }
                                    placedCells.removeAll { it.col == col && it.row == row }
                                    placedCells.add(ForgeCell(col, row, selectedTool))
                                }
                            }
                        }
                    }
            ) {
                val scaleVal = minOf(size.width / (cols * cellW), size.height / (rows * cellH))
                val ox = (size.width - cols * cellW * scaleVal) / 2f
                val oy = (size.height - rows * cellH * scaleVal) / 2f

                // Draw Grid Lines
                val gridColor = if (isDarkTheme) Color(0x18FFFFFF) else Color(0x18000000)
                for (c in 0..cols) {
                    val x = ox + c * cellW * scaleVal
                    drawLine(gridColor, Offset(x, oy), Offset(x, oy + rows * cellH * scaleVal), 1f)
                }
                for (r in 0..rows) {
                    val y = oy + r * cellH * scaleVal
                    drawLine(gridColor, Offset(ox, y), Offset(ox + cols * cellW * scaleVal, y), 1f)
                }

                // Draw Placed Cells
                for (cell in placedCells) {
                    val cx = ox + cell.col * cellW * scaleVal
                    val cy = oy + cell.row * cellH * scaleVal
                    val cw = cellW * scaleVal
                    val ch = cellH * scaleVal

                    drawForgeCell(cell.tool, cx, cy, cw, ch, isDarkTheme)
                }
            }
        }

        // Bottom CTA: Test Play Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = {
                    val levelDef = buildLevelDefFromForge(levelName, placedCells)
                    onTestPlay(levelDef)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06D6A0),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_test_play")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TEST PLAY LEVEL",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun DrawScope.drawForgeCell(
    tool: ForgeTool,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    isDarkTheme: Boolean
) {
    when (tool) {
        ForgeTool.SOLID -> {
            drawRoundRect(
                color = if (isDarkTheme) Color(0xFF1E2028) else Color(0xFF33353D),
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(w - 2f, h - 2f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        ForgeTool.CRUMBLE -> {
            drawRoundRect(
                color = Color(0xFF5C5449),
                topLeft = Offset(x + 1f, y + 1f),
                size = Size(w - 2f, h - 2f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        ForgeTool.BOUNCE -> {
            drawRoundRect(
                color = Color(0xFFFF5252),
                topLeft = Offset(x + 1f, y + h * 0.4f),
                size = Size(w - 2f, h * 0.6f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        ForgeTool.SPIKE_UP -> {
            val path = Path().apply {
                moveTo(x + 2f, y + h - 2f)
                lineTo(x + w / 2f, y + 4f)
                lineTo(x + w - 2f, y + h - 2f)
                close()
            }
            drawPath(path, color = Color(0xFF0D0E12))
        }
        ForgeTool.SNEAKY_SPIKE -> {
            val path = Path().apply {
                moveTo(x + 2f, y + h - 2f)
                lineTo(x + w / 2f, y + h * 0.4f)
                lineTo(x + w - 2f, y + h - 2f)
                close()
            }
            drawPath(path, color = Color(0xFFFF8800))
        }
        ForgeTool.DROP_TRAP -> {
            drawRoundRect(
                color = Color(0xFF8B0000),
                topLeft = Offset(x + 2f, y + 2f),
                size = Size(w - 4f, h - 4f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        ForgeTool.SPAWN -> {
            drawRoundRect(
                color = Color(0xFF13141B),
                topLeft = Offset(x + w * 0.28f, y + h * 0.1f),
                size = Size(w * 0.44f, h * 0.8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(x + w * 0.28f, y + h * 0.1f),
                size = Size(w * 0.44f, h * 0.8f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 1.2f)
            )
            // Luminous eyes
            drawCircle(Color.White, 2f, Offset(x + w * 0.42f, y + h * 0.35f))
            drawCircle(Color.White, 2f, Offset(x + w * 0.58f, y + h * 0.35f))
        }
        ForgeTool.DOOR_NORMAL, ForgeTool.DOOR_RUNAWAY -> {
            val glow = if (tool == ForgeTool.DOOR_RUNAWAY) Color(0xFFFF5252) else Color(0xFFFFD166)
            drawRoundRect(
                color = Color(0xFF101115),
                topLeft = Offset(x + 3f, y + 2f),
                size = Size(w - 6f, h - 2f),
                cornerRadius = CornerRadius(w / 2f, w / 2f)
            )
            drawRoundRect(
                color = glow,
                topLeft = Offset(x + 6f, y + 5f),
                size = Size(w - 12f, h - 5f),
                cornerRadius = CornerRadius(w / 2f, w / 2f)
            )
        }
        ForgeTool.ERASER -> {}
    }
}

private fun buildLevelDefFromForge(name: String, cells: List<ForgeCell>): LevelDef {
    val cellW = 40f
    val cellH = 40f
    var spawnX = 80f
    var spawnY = 320f
    var door = Door(x = 680f, y = 312f)
    val platforms = mutableListOf<Platform>()
    val spikes = mutableListOf<Spike>()
    val triggers = mutableListOf<LevelTrigger>()

    // Boundary walls
    platforms.add(Platform(x = 0f, y = 0f, width = 20f, height = 450f))
    platforms.add(Platform(x = 780f, y = 0f, width = 20f, height = 450f))
    platforms.add(Platform(x = 0f, y = 0f, width = 800f, height = 20f))

    for (cell in cells) {
        val px = cell.col * cellW
        val py = cell.row * cellH

        when (cell.tool) {
            ForgeTool.SPAWN -> {
                spawnX = px + 8f
                spawnY = py + 8f
            }
            ForgeTool.DOOR_NORMAL -> {
                door = Door(x = px + 2f, y = py - 8f, type = DoorType.NORMAL)
            }
            ForgeTool.DOOR_RUNAWAY -> {
                door = Door(
                    x = px + 2f,
                    y = py - 8f,
                    type = DoorType.RUNAWAY,
                    runawayTargetX = 80f,
                    runawayTargetY = py - 8f,
                    triggerDistance = 120f
                )
            }
            ForgeTool.SOLID -> {
                platforms.add(Platform(x = px, y = py, width = cellW, height = cellH, type = PlatformType.SOLID))
            }
            ForgeTool.CRUMBLE -> {
                platforms.add(Platform(x = px, y = py, width = cellW, height = cellH, type = PlatformType.CRUMBLING))
            }
            ForgeTool.BOUNCE -> {
                platforms.add(Platform(x = px, y = py + 15f, width = cellW, height = cellH - 15f, type = PlatformType.BOUNCE))
            }
            ForgeTool.SPIKE_UP -> {
                spikes.add(Spike(x = px + 8f, y = py + 16f, width = 24f, height = 24f, direction = SpikeDirection.UP))
            }
            ForgeTool.SNEAKY_SPIKE -> {
                spikes.add(Spike(x = px + 8f, y = py + 16f, width = 24f, height = 24f, direction = SpikeDirection.UP, isSneaky = true, triggerDistance = 80f))
            }
            ForgeTool.DROP_TRAP -> {
                val blockId = "drop_${cell.col}_${cell.row}"
                platforms.add(Platform(id = blockId, x = px, y = py, width = cellW, height = cellH))
                triggers.add(
                    LevelTrigger(
                        id = "trig_$blockId",
                        bounds = RectF(px - 10f, py, px + cellW + 10f, 450f),
                        action = TriggerAction.FALLING_BLOCK,
                        targetId = blockId
                    )
                )
            }
            ForgeTool.ERASER -> {}
        }
    }

    return LevelDef(
        levelNumber = 999,
        title = name.ifBlank { "Custom Level" },
        subtitle = "Created in Level Forge",
        spawnX = spawnX,
        spawnY = spawnY,
        door = door,
        platforms = platforms,
        spikes = spikes,
        triggers = triggers
    )
}

private fun serializeForgeToJSON(cells: List<ForgeCell>): String {
    val array = JSONArray()
    for (c in cells) {
        val obj = JSONObject()
        obj.put("c", c.col)
        obj.put("r", c.row)
        obj.put("t", c.tool.name)
        array.put(obj)
    }
    return array.toString()
}

private fun deserializeJSONToForge(json: String): List<ForgeCell> {
    val list = mutableListOf<ForgeCell>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val c = obj.getInt("c")
            val r = obj.getInt("r")
            val tName = obj.getString("t")
            val tool = ForgeTool.valueOf(tName)
            list.add(ForgeCell(c, r, tool))
        }
    } catch (_: Exception) {}
    return list
}
