package com.example.game.renderer

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.puzzles.LeverSwitch
import com.example.game.puzzles.LockedGate
import com.example.game.puzzles.MovingPlatform
import com.example.game.puzzles.PressurePlate
import com.example.game.puzzles.RunicTorch
import com.example.game.puzzles.SecretCrumbleWall
import com.example.game.world.WorldMap
import kotlin.math.sin

object PuzzleRenderer {

    fun renderPuzzles(drawScope: DrawScope, world: WorldMap, time: Float) {
        // 1. Moving Platforms
        for (plat in world.movingPlatforms) {
            drawMovingPlatform(drawScope, plat, time)
        }

        // 2. Pressure Plates
        for (plate in world.pressurePlates) {
            drawPressurePlate(drawScope, plate)
        }

        // 3. Lever Switches
        for (lever in world.leverSwitches) {
            drawLeverSwitch(drawScope, lever)
        }

        // 4. Runic Torches
        for (torch in world.runicTorches) {
            drawRunicTorch(drawScope, torch, time)
        }

        // 5. Crumble Walls
        for (wall in world.crumbleWalls) {
            drawCrumbleWall(drawScope, wall)
        }

        // 6. Locked Gates
        for (gate in world.lockedGates) {
            drawLockedGate(drawScope, gate, time)
        }
    }

    private fun drawMovingPlatform(drawScope: DrawScope, plat: MovingPlatform, time: Float) {
        val px = plat.pos.x
        val py = plat.pos.y
        val w = plat.width
        val h = plat.height

        // Stone Base Slab
        drawScope.drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF455A64), Color(0xFF263238))),
            topLeft = Offset(px, py),
            size = Size(w, h),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Top highlight
        drawScope.drawLine(
            color = Color(0xFF90A4AE),
            start = Offset(px, py),
            end = Offset(px + w, py),
            strokeWidth = 1.5f
        )
        // Glowing Ancient Runes on side
        val runePulse = (sin(time * 4f) * 0.3f + 0.7f)
        val runeColor = Color(0xFF00E5FF).copy(alpha = runePulse)
        for (i in 0..2) {
            val rx = px + 12f + i * 20f
            drawScope.drawCircle(
                color = runeColor,
                radius = 2.5f,
                center = Offset(rx, py + h * 0.5f)
            )
        }
        // Underneath Arcane Thruster Glow
        drawScope.drawOval(
            color = Color(0x5500E5FF),
            topLeft = Offset(px + 8f, py + h - 2f),
            size = Size(w - 16f, 6f)
        )
    }

    private fun drawPressurePlate(drawScope: DrawScope, plate: PressurePlate) {
        val px = plate.pos.x
        val py = plate.pos.y
        val w = plate.width
        val h = plate.height

        val stoneColor = if (plate.isPressed) Color(0xFF1E88E5) else Color(0xFF546E7A)
        val plateHeight = if (plate.isPressed) 3f else h

        // Base Rim
        drawScope.drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(px - 2f, py + h - 2f),
            size = Size(w + 4f, 4f)
        )
        // Stepped Plate
        drawScope.drawRoundRect(
            color = stoneColor,
            topLeft = Offset(px, py + (h - plateHeight)),
            size = Size(w, plateHeight),
            cornerRadius = CornerRadius(1.5f, 1.5f)
        )
        if (plate.isPressed) {
            drawScope.drawCircle(
                color = Color(0xFF64B5F6),
                radius = 3f,
                center = Offset(px + w * 0.5f, py + h - 1.5f)
            )
        }
    }

    private fun drawLeverSwitch(drawScope: DrawScope, lever: LeverSwitch) {
        val lx = lever.pos.x
        val ly = lever.pos.y

        // Stone Box Housing
        drawScope.drawRoundRect(
            color = Color(0xFF37474F),
            topLeft = Offset(lx, ly + 14f),
            size = Size(18f, 10f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        val handleColor = if (lever.isOn) Color(0xFF00E676) else Color(0xFFFF5252)
        val startX = lx + 9f
        val startY = ly + 18f
        val endX = if (lever.isOn) lx + 16f else lx + 2f
        val endY = ly + 4f

        // Lever Stick
        drawScope.drawLine(
            color = Color(0xFFB0BEC5),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        // Lever Knob
        drawScope.drawCircle(
            color = handleColor,
            radius = 3.8f,
            center = Offset(endX, endY)
        )
    }

    private fun drawRunicTorch(drawScope: DrawScope, torch: RunicTorch, time: Float) {
        val tx = torch.pos.x
        val ty = torch.pos.y

        // Stone Brazier Base
        val brazierPath = Path().apply {
            moveTo(tx + 4f, ty + 28f)
            lineTo(tx + 16f, ty + 28f)
            lineTo(tx + 14f, ty + 12f)
            lineTo(tx + 18f, ty + 8f)
            lineTo(tx + 2f, ty + 8f)
            lineTo(tx + 6f, ty + 12f)
            close()
        }
        drawScope.drawPath(brazierPath, color = Color(0xFF37474F))
        drawScope.drawPath(brazierPath, color = Color(0xFF78909C), style = Stroke(width = 1.2f))

        if (torch.isLit) {
            // Flickering magical flame
            val flameFlicker = sin(time * 12f + tx) * 2f
            val flameColor1 = Color(0xFFFF9100)
            val flameColor2 = Color(0xFFFFEA00)
            val flameGlow = Color(0x44FF9100)

            drawScope.drawCircle(
                color = flameGlow,
                radius = 16f + flameFlicker,
                center = Offset(tx + 10f, ty + 4f)
            )
            // Inner flame
            val flamePath = Path().apply {
                moveTo(tx + 4f, ty + 8f)
                cubicTo(tx + 2f, ty + 2f, tx + 6f, ty - 6f + flameFlicker, tx + 10f, ty - 8f + flameFlicker)
                cubicTo(tx + 14f, ty - 6f + flameFlicker, tx + 18f, ty + 2f, tx + 16f, ty + 8f)
                close()
            }
            drawScope.drawPath(flamePath, color = flameColor1)
            drawScope.drawCircle(
                color = flameColor2,
                radius = 3.5f,
                center = Offset(tx + 10f, ty + 4f)
            )
        } else {
            // Unlit Ash Core
            drawScope.drawCircle(
                color = Color(0xFF212121),
                radius = 3f,
                center = Offset(tx + 10f, ty + 8f)
            )
        }
    }

    private fun drawCrumbleWall(drawScope: DrawScope, wall: SecretCrumbleWall) {
        if (wall.isBroken) return

        val wx = wall.pos.x
        val wy = wall.pos.y
        val ts = 32f

        // Ancient Cracked Rock Block
        drawScope.drawRect(
            color = Color(0xFF455A64),
            topLeft = Offset(wx, wy),
            size = Size(ts, ts)
        )
        // Deep Fissures / Cracks
        drawScope.drawLine(
            color = Color(0xFF1C2833),
            start = Offset(wx + 4f, wy + 2f),
            end = Offset(wx + 14f, wy + 16f),
            strokeWidth = 1.8f
        )
        drawScope.drawLine(
            color = Color(0xFF1C2833),
            start = Offset(wx + 14f, wy + 16f),
            end = Offset(wx + 28f, wy + 22f),
            strokeWidth = 1.8f
        )
        drawScope.drawLine(
            color = Color(0xFF1C2833),
            start = Offset(wx + 14f, wy + 16f),
            end = Offset(wx + 8f, wy + 28f),
            strokeWidth = 1.5f
        )
        // Danger / Destructible Hint: Small moss and pebbles
        drawScope.drawCircle(
            color = Color(0xFF2E7D32),
            radius = 2.5f,
            center = Offset(wx + 6f, wy + 8f)
        )
    }

    private fun drawLockedGate(drawScope: DrawScope, gate: LockedGate, time: Float) {
        val gx = gate.pos.x
        val gy = gate.currentY
        val w = gate.width
        val h = gate.height

        // Iron Portcullis Bars
        val barColor = Color(0xFF263238)
        val ironColor = Color(0xFF455A64)
        val runeColor = if (gate.isOpen) Color(0xFF00E676) else Color(0xFF00E5FF)

        // Draw Vertical Iron Bars
        val numBars = (w / 8f).toInt().coerceAtLeast(2)
        for (i in 0 until numBars) {
            val bx = gx + 4f + i * 8f
            drawScope.drawRect(
                color = ironColor,
                topLeft = Offset(bx, gy),
                size = Size(3f, h)
            )
            // Spike tip at bottom
            val spike = Path().apply {
                moveTo(bx - 1f, gy + h)
                lineTo(bx + 1.5f, gy + h + 4f)
                lineTo(bx + 4f, gy + h)
                close()
            }
            drawScope.drawPath(spike, color = barColor)
        }

        // Horizontal Reinforced Crossbeams
        drawScope.drawRect(
            color = barColor,
            topLeft = Offset(gx, gy + 10f),
            size = Size(w, 5f)
        )
        drawScope.drawRect(
            color = barColor,
            topLeft = Offset(gx, gy + h - 14f),
            size = Size(w, 5f)
        )

        // Central Arcane Lock Rune
        val runeCenter = Offset(gx + w * 0.5f, gy + h * 0.5f)
        drawScope.drawCircle(
            color = Color(0xFF37474F),
            radius = 8f,
            center = runeCenter
        )
        drawScope.drawCircle(
            color = runeColor,
            radius = 4f + sin(time * 3f) * 1f,
            center = runeCenter
        )
    }
}
