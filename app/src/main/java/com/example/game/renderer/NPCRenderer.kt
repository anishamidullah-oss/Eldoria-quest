package com.example.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.entities.NPC
import com.example.game.entities.NPCType
import com.example.game.model.Vector2D
import kotlin.math.PI
import kotlin.math.sin

class NPCRenderer {

    fun renderNPCs(
        drawScope: DrawScope,
        npcs: List<NPC>,
        cameraPos: Vector2D,
        screenWidth: Float,
        screenHeight: Float,
        time: Float,
        playerPos: Vector2D
    ) {
        for (npc in npcs) {
            val screenX = npc.pos.x - cameraPos.x
            val screenY = npc.pos.y - cameraPos.y

            // Frustum Culling
            if (screenX + npc.width < -60f || screenX > screenWidth + 60f ||
                screenY + npc.height < -60f || screenY > screenHeight + 60f
            ) {
                continue
            }

            renderSingleNPC(drawScope, npc, screenX, screenY, time, playerPos)
        }
    }

    private fun renderSingleNPC(
        drawScope: DrawScope,
        npc: NPC,
        sx: Float,
        sy: Float,
        time: Float,
        playerPos: Vector2D
    ) {
        val dir = if (npc.facingRight) 1f else -1f
        val cx = sx + npc.width * 0.5f
        val w = npc.width
        val h = npc.height

        // Subtle idle breathing
        val breath = sin(time * 3f + npc.animTimer) * 1.5f

        // Distance to player for interaction prompt
        val distToPlayer = kotlin.math.hypot(playerPos.x - npc.pos.x, playerPos.y - npc.pos.y)
        val isNearPlayer = distToPlayer < 90f

        // NPC Shadow
        drawScope.drawOval(
            color = Color(0x55000000),
            topLeft = Offset(cx - 16f, sy + h - 3f),
            size = Size(32f, 7f)
        )

        when (npc.type) {
            NPCType.VILLAGE_ELDER -> {
                // ELDER BRAN: Robes, long silver beard, oak walking staff with glowing azure gem
                val robeColor = Color(0xFF1E3A8A)
                val trimColor = Color(0xFFF59E0B)
                val skinColor = Color(0xFFFFD1A4)
                val beardColor = Color(0xFFE2E8F0)

                // Robe Body
                val robePath = Path().apply {
                    moveTo(cx - 10f, sy + 16f + breath)
                    lineTo(cx + 10f, sy + 16f + breath)
                    lineTo(cx + 14f, sy + h)
                    lineTo(cx - 14f, sy + h)
                    close()
                }
                drawScope.drawPath(robePath, color = robeColor)
                drawScope.drawPath(robePath, color = trimColor, style = Stroke(width = 1.5f))

                // Elder Head & Hood
                drawScope.drawCircle(color = skinColor, radius = 6.5f, center = Offset(cx, sy + 10f + breath))
                // Hood
                val hoodPath = Path().apply {
                    moveTo(cx - 8f, sy + 12f + breath)
                    cubicTo(cx - 9f, sy + 2f + breath, cx + 9f, sy + 2f + breath, cx + 8f, sy + 12f + breath)
                    lineTo(cx - 8f, sy + 12f + breath)
                }
                drawScope.drawPath(hoodPath, color = robeColor)

                // Flowing Silver Beard
                val beardPath = Path().apply {
                    moveTo(cx - 4f, sy + 11f + breath)
                    cubicTo(cx - 6f, sy + 24f + breath, cx + 6f, sy + 24f + breath, cx + 4f, sy + 11f + breath)
                    close()
                }
                drawScope.drawPath(beardPath, color = beardColor)

                // Wise Eyes
                drawScope.drawCircle(color = Color(0xFF0F172A), radius = 1.2f, center = Offset(cx + 2.5f * dir, sy + 9f + breath))

                // Wooden Staff with Radiant Sapphire Gem
                val staffX = cx + 13f * dir
                drawScope.drawLine(
                    color = Color(0xFF78350F),
                    start = Offset(staffX, sy + 4f),
                    end = Offset(staffX, sy + h),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                // Staff Orb
                val gemGlow = sin(time * 5f) * 2f
                drawScope.drawCircle(color = Color(0x6638BDF8), radius = 7f + gemGlow, center = Offset(staffX, sy + 4f))
                drawScope.drawCircle(color = Color(0xFF00E5FF), radius = 4f, center = Offset(staffX, sy + 4f))
                drawScope.drawCircle(color = Color.White, radius = 1.5f, center = Offset(staffX, sy + 4f))
            }

            NPCType.BLACKSMITH -> {
                // MASTER TORIN: Muscular build, leather smithing apron, heavy forging hammer, glowing anvil
                val skinColor = Color(0xFFE0A97E)
                val leatherApron = Color(0xFF78350F)
                val ironColor = Color(0xFF334155)

                // Arms & Torso
                drawScope.drawRoundRect(
                    color = skinColor,
                    topLeft = Offset(cx - 11f, sy + 12f + breath),
                    size = Size(22f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Apron
                drawScope.drawRect(
                    color = leatherApron,
                    topLeft = Offset(cx - 8f, sy + 16f + breath),
                    size = Size(16f, 22f)
                )
                // Legs / Boots
                drawScope.drawRect(color = Color(0xFF1E293B), topLeft = Offset(cx - 9f, sy + 36f), size = Size(7f, 12f))
                drawScope.drawRect(color = Color(0xFF1E293B), topLeft = Offset(cx + 2f, sy + 36f), size = Size(7f, 12f))

                // Head with Bandana
                drawScope.drawCircle(color = skinColor, radius = 7f, center = Offset(cx, sy + 8f + breath))
                drawScope.drawRect(color = Color(0xFFDC2626), topLeft = Offset(cx - 7f, sy + 3f + breath), size = Size(14f, 4f))
                // Bushy Beard
                drawScope.drawRoundRect(color = Color(0xFF451A03), topLeft = Offset(cx - 5f, sy + 9f + breath), size = Size(10f, 7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))

                // Heavy Forging Sledgehammer
                val hammerX = cx + 12f * dir
                drawScope.drawLine(
                    color = Color(0xFF78350F),
                    start = Offset(hammerX, sy + 16f),
                    end = Offset(hammerX, sy + h),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawRoundRect(
                    color = ironColor,
                    topLeft = Offset(hammerX - 6f, sy + 12f),
                    size = Size(12f, 8f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            NPCType.MERCHANT -> {
                // ELOWEN: Traveling green cloak, golden brass buckles, big merchant backpack with potion vials
                val cloakGreen = Color(0xFF15803D)
                val goldColor = Color(0xFFF59E0B)
                val skinColor = Color(0xFFFFD1A4)

                // Large Merchant Pack
                drawScope.drawRoundRect(
                    color = Color(0xFF78350F),
                    topLeft = Offset(cx - 14f * dir - 6f, sy + 14f + breath),
                    size = Size(12f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Potion vials strapped to pack
                drawScope.drawCircle(color = Color(0xFFFF1744), radius = 2.5f, center = Offset(cx - 14f * dir, sy + 18f + breath))
                drawScope.drawCircle(color = Color(0xFF00E5FF), radius = 2.5f, center = Offset(cx - 14f * dir, sy + 25f + breath))

                // Cloak
                drawScope.drawRoundRect(
                    color = cloakGreen,
                    topLeft = Offset(cx - 9f, sy + 14f + breath),
                    size = Size(18f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Gold Trim
                drawScope.drawRect(color = goldColor, topLeft = Offset(cx - 2f, sy + 14f + breath), size = Size(4f, 26f))

                // Merchant Hat / Feather
                drawScope.drawCircle(color = skinColor, radius = 6.5f, center = Offset(cx, sy + 9f + breath))
                drawScope.drawOval(color = Color(0xFF14532D), topLeft = Offset(cx - 10f, sy + 3f + breath), size = Size(20f, 6f))
                // Red Feather
                drawScope.drawLine(color = Color(0xFFEF4444), start = Offset(cx - 4f, sy + 4f + breath), end = Offset(cx - 10f, sy - 4f + breath), strokeWidth = 2f)

                // Pouch of Gold Coins in Hand
                drawScope.drawCircle(color = goldColor, radius = 4f, center = Offset(cx + 8f * dir, sy + 26f + breath))
            }

            NPCType.GUARD -> {
                // CAPTAIN VANE: Heavy polished steel armor, royal blue surcoat, crested helmet, upright halberd
                val armorColor = Color(0xFF94A3B8)
                val armorDark = Color(0xFF475569)
                val surcoatBlue = Color(0xFF1D4ED8)

                // Plate Greaves
                drawScope.drawRect(color = armorDark, topLeft = Offset(cx - 8f, sy + 34f), size = Size(6f, 14f))
                drawScope.drawRect(color = armorDark, topLeft = Offset(cx + 2f, sy + 34f), size = Size(6f, 14f))

                // Steel Cuirass & Blue Surcoat
                drawScope.drawRoundRect(
                    color = armorColor,
                    topLeft = Offset(cx - 10f, sy + 13f + breath),
                    size = Size(20f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                drawScope.drawRect(color = surcoatBlue, topLeft = Offset(cx - 5f, sy + 14f + breath), size = Size(10f, 20f))

                // Crested Greathelm
                drawScope.drawRoundRect(
                    color = armorColor,
                    topLeft = Offset(cx - 7f, sy + 2f + breath),
                    size = Size(14f, 13f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Visor Slit
                drawScope.drawRect(color = Color(0xFF0F172A), topLeft = Offset(cx - 4f + 2f * dir, sy + 8f + breath), size = Size(8f, 2.5f))
                // Red Plume
                drawScope.drawLine(color = Color(0xFFDC2626), start = Offset(cx, sy + 2f + breath), end = Offset(cx - 4f, sy - 6f + breath), strokeWidth = 3f, cap = StrokeCap.Round)

                // Upright Halberd Polearm
                val spearX = cx + 12f * dir
                drawScope.drawLine(color = Color(0xFF78350F), start = Offset(spearX, sy - 14f), end = Offset(spearX, sy + h), strokeWidth = 3f)
                // Spear Blade
                val spearBlade = Path().apply {
                    moveTo(spearX, sy - 22f); lineTo(spearX + 5f, sy - 12f); lineTo(spearX - 5f, sy - 12f); close()
                }
                drawScope.drawPath(spearBlade, color = Color(0xFFCBD5E1))
            }

            NPCType.TRAVELER -> {
                // RODERICK: Bedroll pack, traveling boots, walking stick, warm traveler tunic
                val tunicBrown = Color(0xFF92400E)
                val skinColor = Color(0xFFFFD1A4)

                // Bedroll on shoulders
                drawScope.drawRoundRect(color = Color(0xFF475569), topLeft = Offset(cx - 12f, sy + 10f + breath), size = Size(24f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))

                // Body
                drawScope.drawRoundRect(color = tunicBrown, topLeft = Offset(cx - 8f, sy + 15f + breath), size = Size(16f, 22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
                // Head with Traveler Hood
                drawScope.drawCircle(color = skinColor, radius = 6.5f, center = Offset(cx, sy + 9f + breath))
                drawScope.drawCircle(color = Color(0xFF78350F), radius = 7.5f, center = Offset(cx, sy + 7f + breath))

                // Sturdy Walking Staff
                drawScope.drawLine(color = Color(0xFF5A2E07), start = Offset(cx + 10f * dir, sy + 10f), end = Offset(cx + 10f * dir, sy + h), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }

            NPCType.EXPLORER -> {
                // KAELEN: Leather duster coat, spyglass, climbing rope coils, brass headlamp
                val dusterBrown = Color(0xFF451A03)
                val skinColor = Color(0xFFFFD1A4)

                // Rope coil on hip
                drawScope.drawCircle(color = Color(0xFFD97706), radius = 5f, center = Offset(cx - 8f * dir, sy + 28f + breath), style = Stroke(width = 2f))

                // Duster Coat
                drawScope.drawRoundRect(color = dusterBrown, topLeft = Offset(cx - 9f, sy + 14f + breath), size = Size(18f, 24f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
                // Head
                drawScope.drawCircle(color = skinColor, radius = 6.5f, center = Offset(cx, sy + 8f + breath))
                // Brass Goggles on Forehead
                drawScope.drawCircle(color = Color(0xFFF59E0B), radius = 2.5f, center = Offset(cx - 2f, sy + 6f + breath))
                drawScope.drawCircle(color = Color(0xFFF59E0B), radius = 2.5f, center = Offset(cx + 3f, sy + 6f + breath))

                // Brass Spyglass in Hand
                drawScope.drawLine(color = Color(0xFFF59E0B), start = Offset(cx + 6f * dir, sy + 22f + breath), end = Offset(cx + 14f * dir, sy + 20f + breath), strokeWidth = 3f)
            }

            NPCType.SCHOLAR -> {
                // ARCHIVIST LYSANDRA: Celestial indigo robes, floating ancient arcane grimoire, glowing eyes
                val robeIndigo = Color(0xFF312E81)
                val starGold = Color(0xFFFDE047)
                val skinColor = Color(0xFFFFE4E6)

                // Flowing Robes
                val robePath = Path().apply {
                    moveTo(cx - 9f, sy + 14f + breath)
                    lineTo(cx + 9f, sy + 14f + breath)
                    lineTo(cx + 13f, sy + h)
                    lineTo(cx - 13f, sy + h)
                    close()
                }
                drawScope.drawPath(robePath, color = robeIndigo)

                // Celestial Gold Constellation Markings
                drawScope.drawCircle(color = starGold, radius = 1.5f, center = Offset(cx - 4f, sy + 24f + breath))
                drawScope.drawCircle(color = starGold, radius = 1.5f, center = Offset(cx + 3f, sy + 30f + breath))
                drawScope.drawCircle(color = starGold, radius = 1.5f, center = Offset(cx, sy + 38f + breath))

                // Head with Circlet
                drawScope.drawCircle(color = skinColor, radius = 6.5f, center = Offset(cx, sy + 8f + breath))
                drawScope.drawRect(color = starGold, topLeft = Offset(cx - 6f, sy + 5f + breath), size = Size(12f, 2f))

                // Floating Mystic Grimoire Tome
                val bookY = sy + 18f + sin(time * 4f) * 4f
                val bookX = cx + 14f * dir
                drawScope.drawRoundRect(
                    color = Color(0xFF7C3AED),
                    topLeft = Offset(bookX - 6f, bookY - 5f),
                    size = Size(12f, 10f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
                // Glowing Arcane Glyphs
                drawScope.drawCircle(color = Color(0xFF00E5FF), radius = 2f, center = Offset(bookX, bookY))
            }
        }

        // QUEST / INTERACTION INDICATOR ABOVE HEAD
        val indicatorY = sy - 14f + sin(time * 5f) * 3f

        if (isNearPlayer) {
            // TALK / READ Prompt Bubble
            drawScope.drawRoundRect(
                color = Color(0xEE090D16),
                topLeft = Offset(cx - 24f, indicatorY - 8f),
                size = Size(48f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawScope.drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(cx - 24f, indicatorY - 8f),
                size = Size(48f, 18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )
            // Golden Diamond Icon inside bubble
            val diamond = Path().apply {
                moveTo(cx, indicatorY - 5f)
                lineTo(cx + 4f, indicatorY + 1f)
                lineTo(cx, indicatorY + 7f)
                lineTo(cx - 4f, indicatorY + 1f)
                close()
            }
            drawScope.drawPath(diamond, color = Color(0xFFFFD700))
        } else if (npc.hasQuestAvailable) {
            // Radiant Exclamation Mark `!` Marker
            drawScope.drawCircle(
                color = Color(0x55FFD700),
                radius = 10f,
                center = Offset(cx, indicatorY)
            )
            drawScope.drawCircle(
                color = Color(0xFFFFD700),
                radius = 7.5f,
                center = Offset(cx, indicatorY)
            )
            // Black `!` mark
            drawScope.drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(cx - 1.2f, indicatorY - 5f),
                size = Size(2.4f, 6f)
            )
            drawScope.drawCircle(
                color = Color(0xFF0F172A),
                radius = 1.2f,
                center = Offset(cx, indicatorY + 3.5f)
            )
        }
    }
}
