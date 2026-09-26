package com.example.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.game.engine.GameEngine
import com.example.game.world.TileType
import kotlin.math.sin

object EnvironmentRenderer {

    fun drawParallaxBackground(
        drawScope: DrawScope,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        val progress = (camX / 3200f).coerceIn(0f, 1f)

        // 1. SKY GRADIENT (Atmospheric transitions: Emerald Forest -> Deep Cavern -> Citadel Twilight)
        val (skyTop, skyBottom) = when {
            progress < 0.4f -> Pair(Color(0xFF0D2538), Color(0xFF386B62)) // Forest Emerald Twilight
            progress < 0.75f -> Pair(Color(0xFF080D1A), Color(0xFF1E293B)) // Deep Slate Cavern
            else -> Pair(Color(0xFF1E0C36), Color(0xFFB35446)) // Citadel Sunset Amber
        }

        drawScope.drawRect(
            brush = Brush.verticalGradient(listOf(skyTop, skyBottom)),
            topLeft = Offset(camX, camY),
            size = Size(viewW, viewH)
        )

        // 2. LAYER 1: DRIFTING CLOUDS & HIGH-ALTITUDE MIST (Parallax 0.08x + Wind Animation)
        val windDrift = time * 12f
        val cloudLoopW = viewW * 1.5f
        val cloudOffset = (camX * 0.08f + windDrift) % cloudLoopW

        for (k in -1..2) {
            val baseCloudX = camX + k * cloudLoopW - cloudOffset
            val cloudColor1 = Color(0x33FFFFFF)
            val cloudColor2 = Color(0x22ECEFF1)

            // Cloud Cluster A
            drawCloudFormation(
                drawScope = drawScope,
                x = baseCloudX + 60f,
                y = camY + viewH * 0.12f,
                scale = 1.2f,
                color = cloudColor1
            )
            // Cloud Cluster B
            drawCloudFormation(
                drawScope = drawScope,
                x = baseCloudX + viewW * 0.65f,
                y = camY + viewH * 0.06f,
                scale = 0.9f,
                color = cloudColor2
            )
            // Cloud Cluster C (Low wispy haze)
            drawCloudFormation(
                drawScope = drawScope,
                x = baseCloudX + viewW * 1.15f,
                y = camY + viewH * 0.18f,
                scale = 1.4f,
                color = Color(0x1AFFFFFF)
            )
        }

        // 3. LAYER 2: MAJESTIC DISTANT MOUNTAIN RANGES (Parallax 0.18x)
        // Deep Mountain Ridge 1 (Far background)
        val mFarLoopW = viewW * 1.2f
        val mFarOffset = (camX * 0.12f) % mFarLoopW
        for (k in -1..2) {
            val mx = camX + k * mFarLoopW - mFarOffset
            val farPeakPath = Path().apply {
                moveTo(mx, camY + viewH * 0.45f)
                lineTo(mx + mFarLoopW * 0.25f, camY + viewH * 0.22f) // High peak
                lineTo(mx + mFarLoopW * 0.45f, camY + viewH * 0.38f) // Saddle
                lineTo(mx + mFarLoopW * 0.70f, camY + viewH * 0.18f) // Grand jagged summit
                lineTo(mx + mFarLoopW * 0.90f, camY + viewH * 0.42f)
                lineTo(mx + mFarLoopW, camY + viewH * 0.48f)
                lineTo(mx + mFarLoopW, camY + viewH)
                lineTo(mx, camY + viewH)
                close()
            }
            drawScope.drawPath(farPeakPath, color = Color(0x3D102438))
        }

        // Fore-Mountain Ridge 2 (Crisp crags with snowline highlights, Parallax 0.18x)
        val mLoopW = viewW
        val mOffset = (camX * 0.18f) % mLoopW
        for (k in -1..2) {
            val mx = camX + k * mLoopW - mOffset
            val my = camY + viewH * 0.38f - camY * 0.04f

            val midPeakPath = Path().apply {
                moveTo(mx, my + viewH * 0.15f)
                lineTo(mx + mLoopW * 0.18f, my - 25f)
                lineTo(mx + mLoopW * 0.35f, my + 30f)
                lineTo(mx + mLoopW * 0.55f, my - 45f) // Main Crest
                lineTo(mx + mLoopW * 0.78f, my + 20f)
                lineTo(mx + mLoopW, my + viewH * 0.18f)
                lineTo(mx + mLoopW, camY + viewH)
                lineTo(mx, camY + viewH)
                close()
            }
            drawScope.drawPath(midPeakPath, color = Color(0x66183042))

            // Mountain Crest Light Edges / Snowcaps
            val snowCapPath = Path().apply {
                moveTo(mx + mLoopW * 0.50f, my - 30f)
                lineTo(mx + mLoopW * 0.55f, my - 45f)
                lineTo(mx + mLoopW * 0.60f, my - 28f)
                lineTo(mx + mLoopW * 0.57f, my - 22f)
                close()
            }
            drawScope.drawPath(snowCapPath, color = Color(0x55E0F2F1))
        }

        // Volumetric God Rays through peaks
        if (progress < 0.45f || progress > 0.75f) {
            val rayColor = if (progress < 0.45f) Color(0x16FFEB3B) else Color(0x16FF7043)
            for (i in 0..4) {
                val rayOffset = (camX * 0.14f + i * 190f) % (viewW * 1.5f)
                val rayPath = Path().apply {
                    moveTo(camX + rayOffset - 40f, camY)
                    lineTo(camX + rayOffset + 50f, camY)
                    lineTo(camX + rayOffset + 150f, camY + viewH)
                    lineTo(camX + rayOffset - 10f, camY + viewH)
                    close()
                }
                drawScope.drawPath(rayPath, color = rayColor)
            }
        }

        // 4. LAYER 3: DISTANT ANCIENT FOREST CANOPY & SILHOUETTES (Parallax 0.40x)
        val treeColor = if (progress < 0.45f) Color(0x66143026) else Color(0x661E293B)
        val forestLoopW = viewW * 0.9f
        val forestOffset = (camX * 0.40f) % forestLoopW

        for (k in -1..2) {
            val baseForestX = camX + k * forestLoopW - forestOffset

            for (i in 0..5) {
                val tx = baseForestX + i * 140f
                val ty = camY + viewH * 0.48f - camY * 0.08f

                // Deep Trunk
                drawScope.drawRect(
                    color = treeColor,
                    topLeft = Offset(tx + 20f, ty + 25f),
                    size = Size(24f, viewH - (ty + 25f - camY))
                )
                // Layered Canopy Foliage
                drawScope.drawCircle(
                    color = treeColor,
                    radius = 48f,
                    center = Offset(tx + 32f, ty + 18f)
                )
                drawScope.drawCircle(
                    color = treeColor,
                    radius = 36f,
                    center = Offset(tx + 10f, ty + 32f)
                )
                drawScope.drawCircle(
                    color = treeColor,
                    radius = 38f,
                    center = Offset(tx + 52f, ty + 28f)
                )
            }
        }

        // 5. AMBIENT SPORES & FIREFLIES
        for (i in 0..14) {
            val fx = camX + ((i * 123f + time * 30f) % (viewW + 60f)) - 30f
            val fy = camY + (sin((time + i) * 1.6f) * 35f) + viewH * 0.35f + (i * 24f % (viewH * 0.5f))
            val pulse = (sin(time * 3.5f + i) * 0.4f + 0.6f).coerceIn(0.2f, 1f)
            val glowColor = if (progress < 0.45f) Color(0xFFFFEB3B) else Color(0xFF00E5FF)

            drawScope.drawCircle(
                color = glowColor.copy(alpha = pulse * 0.75f),
                radius = 2.4f,
                center = Offset(fx, fy)
            )
        }
    }

    private fun drawCloudFormation(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        scale: Float,
        color: Color
    ) {
        // Multi-puff fluffy fantasy cloud
        drawScope.drawOval(
            color = color,
            topLeft = Offset(x, y + 10f * scale),
            size = Size(140f * scale, 35f * scale)
        )
        drawScope.drawCircle(
            color = color,
            radius = 24f * scale,
            center = Offset(x + 40f * scale, y + 14f * scale)
        )
        drawScope.drawCircle(
            color = color,
            radius = 32f * scale,
            center = Offset(x + 75f * scale, y + 8f * scale)
        )
        drawScope.drawCircle(
            color = color,
            radius = 20f * scale,
            center = Offset(x + 105f * scale, y + 16f * scale)
        )
    }

    fun drawWorldTiles(
        drawScope: DrawScope,
        engine: GameEngine,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        val world = engine.world
        val ts = world.tileSize

        val startCol = (camX / ts).toInt().coerceIn(0, world.cols - 1)
        val endCol = ((camX + viewW) / ts + 1).toInt().coerceIn(0, world.cols - 1)
        val startRow = (camY / ts).toInt().coerceIn(0, world.rows - 1)
        val endRow = ((camY + viewH) / ts + 1).toInt().coerceIn(0, world.rows - 1)

        for (r in startRow..endRow) {
            for (c in startCol..endCol) {
                val tile = world.getTile(r, c)
                if (tile == TileType.AIR) continue

                val tx = c * ts
                val ty = r * ts

                when (tile) {
                    TileType.GRASS_TOP -> {
                        // 1. Stratified Rich Soil Base
                        val soilBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF3E2723), Color(0xFF2C1810)),
                            startY = ty + 8f,
                            endY = ty + ts
                        )
                        drawScope.drawRect(
                            brush = soilBrush,
                            topLeft = Offset(tx, ty + 8f),
                            size = Size(ts, ts - 8f)
                        )

                        // Hanging Root Fibers into soil
                        drawScope.drawLine(
                            color = Color(0xFF5D4037),
                            start = Offset(tx + 8f, ty + 8f),
                            end = Offset(tx + 10f, ty + 20f),
                            strokeWidth = 1.5f,
                            cap = StrokeCap.Round
                        )
                        drawScope.drawLine(
                            color = Color(0xFF5D4037),
                            start = Offset(tx + 22f, ty + 8f),
                            end = Offset(tx + 20f, ty + 18f),
                            strokeWidth = 1.2f,
                            cap = StrokeCap.Round
                        )

                        // 2. Multi-Toned Lush Grass Layer
                        val grassBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32), Color(0xFF1B5E20)),
                            startY = ty,
                            endY = ty + 10f
                        )
                        drawScope.drawRect(
                            brush = grassBrush,
                            topLeft = Offset(tx, ty),
                            size = Size(ts, 10f)
                        )

                        // 3. Overhanging Individual Grass Blades & Tufts
                        drawBladeTuft(drawScope, tx + 2f, ty, 5f, 6f, Color(0xFF81C784))
                        drawBladeTuft(drawScope, tx + 12f, ty, 6f, 8f, Color(0xFF66BB6A))
                        drawBladeTuft(drawScope, tx + 22f, ty, 5.5f, 7f, Color(0xFF81C784))

                        // Woodland Flowers & Mushrooms
                        if ((c * 7) % 5 == 0) {
                            // Small Red Mushroom
                            drawScope.drawOval(
                                color = Color(0xFFE53935),
                                topLeft = Offset(tx + 16f, ty - 4f),
                                size = Size(5f, 4f)
                            )
                            drawScope.drawCircle(
                                color = Color.White,
                                radius = 0.7f,
                                center = Offset(tx + 18.5f, ty - 2.5f)
                            )
                        } else if ((c * 11) % 4 == 0) {
                            // Golden Woodland Flower
                            drawScope.drawCircle(
                                color = Color(0xFFFFEB3B),
                                radius = 2f,
                                center = Offset(tx + 8f, ty - 2f)
                            )
                        }
                    }

                    TileType.GRASS_LEFT, TileType.GRASS_RIGHT -> {
                        drawScope.drawRect(
                            color = Color(0xFF2C1810),
                            topLeft = Offset(tx, ty),
                            size = Size(ts, ts)
                        )
                        drawScope.drawRect(
                            color = Color(0xFF2E7D32),
                            topLeft = Offset(tx, ty),
                            size = Size(ts, 7f)
                        )
                        drawBladeTuft(drawScope, tx + 4f, ty, 5f, 5f, Color(0xFF66BB6A))
                        drawBladeTuft(drawScope, tx + 18f, ty, 5f, 6f, Color(0xFF81C784))
                    }

                    TileType.DIRT -> {
                        // Layered Deep Subterranean Earth with Mineral Specks & Pebbles
                        val dirtBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF2C1810), Color(0xFF1E100B)),
                            startY = ty,
                            endY = ty + ts
                        )
                        drawScope.drawRect(brush = dirtBrush, topLeft = Offset(tx, ty), size = Size(ts, ts))

                        // Embedded Slate Pebbles with Highlights
                        drawScope.drawCircle(color = Color(0xFF455A64), radius = 3.5f, center = Offset(tx + 9f, ty + 11f))
                        drawScope.drawCircle(color = Color(0xFF78909C), radius = 1.2f, center = Offset(tx + 8f, ty + 10f))

                        drawScope.drawCircle(color = Color(0xFF37474F), radius = 4.2f, center = Offset(tx + 23f, ty + 21f))
                        drawScope.drawCircle(color = Color(0xFF607D8B), radius = 1.5f, center = Offset(tx + 22f, ty + 20f))
                    }

                    TileType.STONE_BLOCK -> {
                        // Chiseled Stone Blocks with Beveled Edges & Fissures
                        val stoneBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF607D8B), Color(0xFF455A64), Color(0xFF263238)),
                            start = Offset(tx, ty),
                            end = Offset(tx + ts, ty + ts)
                        )
                        drawScope.drawRect(brush = stoneBrush, topLeft = Offset(tx, ty), size = Size(ts, ts))

                        // Light Top-Left Bevel
                        drawScope.drawLine(
                            color = Color(0xFF90A4AE),
                            start = Offset(tx, ty),
                            end = Offset(tx + ts, ty),
                            strokeWidth = 1.5f
                        )
                        drawScope.drawLine(
                            color = Color(0xFF90A4AE),
                            start = Offset(tx, ty),
                            end = Offset(tx, ty + ts),
                            strokeWidth = 1.5f
                        )

                        // Dark Bottom-Right Shadow Mortar
                        drawScope.drawLine(
                            color = Color(0xFF102027),
                            start = Offset(tx, ty + ts),
                            end = Offset(tx + ts, ty + ts),
                            strokeWidth = 2f
                        )
                        drawScope.drawLine(
                            color = Color(0xFF102027),
                            start = Offset(tx + ts, ty),
                            end = Offset(tx + ts, ty + ts),
                            strokeWidth = 2f
                        )

                        // Lichen / Ancient Stone Moss
                        if ((r + c) % 3 == 0) {
                            drawScope.drawOval(
                                color = Color(0x8833691E),
                                topLeft = Offset(tx + 4f, ty + 4f),
                                size = Size(9f, 6f)
                            )
                        }
                    }

                    TileType.STONE_BRICK -> {
                        // Masonry Castle Bricks with Mortar
                        drawScope.drawRect(color = Color(0xFF37474F), topLeft = Offset(tx, ty), size = Size(ts, ts))
                        // Mortar grid lines
                        drawScope.drawLine(
                            color = Color(0xFF102027),
                            start = Offset(tx, ty + ts * 0.5f),
                            end = Offset(tx + ts, ty + ts * 0.5f),
                            strokeWidth = 1.8f
                        )
                        drawScope.drawLine(
                            color = Color(0xFF102027),
                            start = Offset(tx + ts * 0.5f, ty),
                            end = Offset(tx + ts * 0.5f, ty + ts * 0.5f),
                            strokeWidth = 1.8f
                        )
                        drawScope.drawLine(
                            color = Color(0xFF102027),
                            start = Offset(tx + ts * 0.25f, ty + ts * 0.5f),
                            end = Offset(tx + ts * 0.25f, ty + ts),
                            strokeWidth = 1.8f
                        )
                    }

                    TileType.CAVE_ROCK -> {
                        // Dark Cavern Slate with Glowing Mana Veins
                        drawScope.drawRect(color = Color(0xFF151D28), topLeft = Offset(tx, ty), size = Size(ts, ts))

                        // Glowing Cyan Bioluminescent Mineral Vein
                        drawScope.drawLine(
                            color = Color(0xFF00E5FF),
                            start = Offset(tx + 4f, ty + 8f),
                            end = Offset(tx + 18f, ty + 24f),
                            strokeWidth = 1.6f,
                            cap = StrokeCap.Round
                        )
                        drawScope.drawCircle(
                            color = Color(0xFF80D8FF),
                            radius = 1.8f,
                            center = Offset(tx + 18f, ty + 24f)
                        )
                    }

                    TileType.WOOD_PLATFORM -> {
                        // Hand-Hewn Timber Planks with Woodgrain & Iron Brackets
                        val woodBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037)),
                            startY = ty,
                            endY = ty + 12f
                        )
                        drawScope.drawRoundRect(
                            brush = woodBrush,
                            topLeft = Offset(tx, ty),
                            size = Size(ts, 12f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                        // Woodgrain lines
                        drawScope.drawLine(
                            color = Color(0xFF4E342E),
                            start = Offset(tx + 2f, ty + 4f),
                            end = Offset(tx + ts - 2f, ty + 4f),
                            strokeWidth = 1f
                        )
                        drawScope.drawLine(
                            color = Color(0xFF3E2723),
                            start = Offset(tx + 4f, ty + 8f),
                            end = Offset(tx + ts - 4f, ty + 8f),
                            strokeWidth = 1f
                        )
                        // Iron Rivet Studs
                        drawScope.drawCircle(color = Color(0xFFCFD8DC), radius = 1.8f, center = Offset(tx + 4f, ty + 6f))
                        drawScope.drawCircle(color = Color(0xFFCFD8DC), radius = 1.8f, center = Offset(tx + ts - 4f, ty + 6f))
                    }

                    TileType.LADDER -> {
                        // Wooden Ladder with Bound Rope Rungs
                        drawScope.drawRect(color = Color(0xFF4E342E), topLeft = Offset(tx + 3f, ty), size = Size(5f, ts))
                        drawScope.drawRect(color = Color(0xFF4E342E), topLeft = Offset(tx + ts - 8f, ty), size = Size(5f, ts))

                        // Rungs
                        for (rungY in listOf(6f, 16f, 26f)) {
                            drawScope.drawRoundRect(
                                color = Color(0xFF795548),
                                topLeft = Offset(tx + 3f, ty + rungY),
                                size = Size(ts - 6f, 4f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
                            )
                            // Rope bindings
                            drawScope.drawCircle(color = Color(0xFFD7CCC8), radius = 1.5f, center = Offset(tx + 5f, ty + rungY + 2f))
                            drawScope.drawCircle(color = Color(0xFFD7CCC8), radius = 1.5f, center = Offset(tx + ts - 5f, ty + rungY + 2f))
                        }
                    }

                    TileType.SPIKE_HAZARD -> {
                        // Serrated Forged Iron Spikes with Razor Light Reflection
                        for (i in 0..2) {
                            val sx = tx + i * 10f + 2f
                            val spikePath = Path().apply {
                                moveTo(sx, ty + ts)
                                lineTo(sx + 5f, ty + 6f)
                                lineTo(sx + 10f, ty + ts)
                                close()
                            }
                            drawScope.drawPath(spikePath, color = Color(0xFF78909C))
                            drawScope.drawLine(
                                color = Color.White,
                                start = Offset(sx + 5f, ty + 6f),
                                end = Offset(sx + 5f, ty + ts),
                                strokeWidth = 1.2f
                            )
                            drawScope.drawPath(spikePath, color = Color(0xFF263238), style = Stroke(width = 1f))
                        }
                    }
                    TileType.VILLAGE_COBBLE -> {
                        // Village Cobblestone Paving
                        drawScope.drawRect(color = Color(0xFF4E342E), topLeft = Offset(tx, ty), size = Size(ts, ts))
                        // Individual cobbles
                        val cobbleColor1 = Color(0xFF6D4C41)
                        val cobbleColor2 = Color(0xFF5D4037)
                        drawScope.drawRoundRect(color = cobbleColor1, topLeft = Offset(tx + 2f, ty + 2f), size = Size(12f, 8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                        drawScope.drawRoundRect(color = cobbleColor2, topLeft = Offset(tx + 16f, ty + 3f), size = Size(14f, 9f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                        drawScope.drawRoundRect(color = cobbleColor2, topLeft = Offset(tx + 3f, ty + 13f), size = Size(13f, 8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                        drawScope.drawRoundRect(color = cobbleColor1, topLeft = Offset(tx + 18f, ty + 14f), size = Size(11f, 8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                        drawScope.drawRoundRect(color = cobbleColor1, topLeft = Offset(tx + 6f, ty + 23f), size = Size(20f, 7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                    }
                    TileType.VILLAGE_WOOD_WALL -> {
                        // Village Timber Wall Planks
                        val woodBase = Color(0xFF5D4037)
                        val woodLight = Color(0xFF795548)
                        val nailColor = Color(0xFF2E1C0C)
                        drawScope.drawRect(color = woodBase, topLeft = Offset(tx, ty), size = Size(ts, ts))
                        // 3 horizontal planks
                        for (i in 0..2) {
                            val py = ty + i * 10f
                            drawScope.drawRect(color = woodLight, topLeft = Offset(tx + 1f, py + 1f), size = Size(ts - 2f, 8f))
                            drawScope.drawLine(color = Color(0xFF3E2723), start = Offset(tx, py + 9f), end = Offset(tx + ts, py + 9f), strokeWidth = 1.5f)
                            // Iron Nails
                            drawScope.drawCircle(color = nailColor, radius = 1.2f, center = Offset(tx + 4f, py + 5f))
                            drawScope.drawCircle(color = nailColor, radius = 1.2f, center = Offset(tx + ts - 4f, py + 5f))
                        }
                    }
                    TileType.VILLAGE_ROOF -> {
                        // Terracotta Thatched Roof Shingles
                        val roofShingle = Color(0xFFBF360C)
                        val roofHighlight = Color(0xFFD84315)
                        drawScope.drawRect(color = roofShingle, topLeft = Offset(tx, ty), size = Size(ts, ts))
                        for (i in 0..2) {
                            val ry = ty + i * 10f
                            drawScope.drawRoundRect(
                                color = roofHighlight,
                                topLeft = Offset(tx + (i % 2) * 6f, ry),
                                size = Size(ts - 4f, 8f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                            )
                            drawScope.drawLine(color = Color(0xFF4E1402), start = Offset(tx, ry + 9f), end = Offset(tx + ts, ry + 9f), strokeWidth = 1.5f)
                        }
                    }
                    TileType.VILLAGE_LANTERN -> {
                        // Warm Glowing Lantern Post
                        val ironColor = Color(0xFF212121)
                        // Post
                        drawScope.drawRect(color = ironColor, topLeft = Offset(tx + 14f, ty + 8f), size = Size(4f, 24f))
                        // Lantern Housing
                        drawScope.drawRoundRect(
                            color = ironColor,
                            topLeft = Offset(tx + 9f, ty + 4f),
                            size = Size(14f, 14f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                        // Warm Candle Glow & Flicker
                        val flicker = sin(time * 8f + c) * 2f
                        drawScope.drawCircle(color = Color(0x33FFB300), radius = 18f + flicker, center = Offset(tx + 16f, ty + 11f))
                        drawScope.drawCircle(color = Color(0xFFFFD54F), radius = 5f, center = Offset(tx + 16f, ty + 11f))
                        drawScope.drawCircle(color = Color.White, radius = 2f, center = Offset(tx + 16f, ty + 11f))
                    }
                    TileType.RUIN_STONE, TileType.RUIN_CRUMBLE_BLOCK -> {
                        // Ancient Mossy Ruin Stone Blocks
                        val stoneBase = Color(0xFF37474F)
                        val stoneHighlight = Color(0xFF546E7A)
                        val mossGreen = Color(0xFF2E7D32)
                        drawScope.drawRect(color = stoneBase, topLeft = Offset(tx, ty), size = Size(ts, ts))
                        drawScope.drawRect(color = stoneHighlight, topLeft = Offset(tx + 2f, ty + 2f), size = Size(ts - 4f, ts - 4f))

                        // Cracks & Glyphs
                        drawScope.drawLine(color = Color(0xFF1E293B), start = Offset(tx + 6f, ty + 4f), end = Offset(tx + 14f, ty + 18f), strokeWidth = 1.2f)
                        drawScope.drawLine(color = Color(0xFF1E293B), start = Offset(tx + 14f, ty + 18f), end = Offset(tx + 26f, ty + 12f), strokeWidth = 1.2f)

                        // Overgrown Moss
                        drawScope.drawOval(color = mossGreen, topLeft = Offset(tx + 2f, ty + 1f), size = Size(12f, 6f))
                        drawScope.drawOval(color = mossGreen, topLeft = Offset(tx + 18f, ty + ts - 8f), size = Size(10f, 6f))

                        if (tile == TileType.RUIN_CRUMBLE_BLOCK) {
                            // Missing rubble corner
                            drawScope.drawRect(color = Color(0x44000000), topLeft = Offset(tx + ts - 10f, ty), size = Size(10f, 10f))
                        }
                    }
                    TileType.SNOW_TOP -> {
                        // Mountain Permafrost & Crystalline Snow Cap
                        val dirtBase = Color(0xFF2D3748)
                        val snowWhite = Color(0xFFF8FAFC)
                        val snowShadow = Color(0xFFCBD5E1)
                        drawScope.drawRect(color = dirtBase, topLeft = Offset(tx, ty + 8f), size = Size(ts, ts - 8f))
                        // Snow Cap with Soft Drifts
                        val snowPath = Path().apply {
                            moveTo(tx, ty + 10f)
                            cubicTo(tx + 8f, ty + 12f, tx + 14f, ty + 6f, tx + 20f, ty + 8f)
                            cubicTo(tx + 26f, ty + 10f, tx + 28f, ty + 4f, tx + ts, ty + 8f)
                            lineTo(tx + ts, ty)
                            lineTo(tx, ty)
                            close()
                        }
                        drawScope.drawPath(snowPath, color = snowShadow)
                        drawScope.drawRect(color = snowWhite, topLeft = Offset(tx, ty), size = Size(ts, 6f))
                        // Glistening Ice Sparkles
                        if ((c * 3) % 2 == 0) {
                            drawScope.drawCircle(color = Color(0xFF38BDF8), radius = 1.2f, center = Offset(tx + 12f, ty + 3f))
                        }
                    }
                    TileType.ICE_BLOCK -> {
                        // Glacial Ice Block with Translucent Depth
                        val iceBase = Color(0xFF0284C7)
                        val iceLight = Color(0xFF38BDF8)
                        val iceHighlight = Color(0xFFE0F2FE)
                        drawScope.drawRect(color = iceBase, topLeft = Offset(tx, ty), size = Size(ts, ts))
                        drawScope.drawRect(color = iceLight, topLeft = Offset(tx + 2f, ty + 2f), size = Size(ts - 4f, ts - 4f))
                        // Internal crystalline fractures
                        drawScope.drawLine(color = iceHighlight, start = Offset(tx + 4f, ty + 4f), end = Offset(tx + 24f, ty + 28f), strokeWidth = 1.5f)
                        drawScope.drawLine(color = iceHighlight, start = Offset(tx + 18f, ty + 6f), end = Offset(tx + 8f, ty + 22f), strokeWidth = 1.2f)
                    }
                    TileType.SHRINE_ALTAR -> {
                        // Mystic Sacred Shrine Altar
                        val altarGold = Color(0xFFFFD700)
                        val altarStone = Color(0xFF1E1B4B)
                        val runeCyan = Color(0xFF00E5FF)
                        drawScope.drawRect(color = altarStone, topLeft = Offset(tx, ty), size = Size(ts, ts))
                        drawScope.drawRoundRect(
                            color = Color(0xFF312E81),
                            topLeft = Offset(tx + 2f, ty + 4f),
                            size = Size(ts - 4f, ts - 8f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                        )
                        // Inscribed Glowing Sacred Runes
                        drawScope.drawLine(color = runeCyan, start = Offset(tx + 8f, ty + 16f), end = Offset(tx + 24f, ty + 16f), strokeWidth = 2f)
                        drawScope.drawCircle(color = runeCyan, radius = 3.5f, center = Offset(tx + 16f, ty + 16f))
                        drawScope.drawCircle(color = Color.White, radius = 1.5f, center = Offset(tx + 16f, ty + 16f))
                        // Gold Trim
                        drawScope.drawRect(color = altarGold, topLeft = Offset(tx, ty + 2f), size = Size(ts, 2f))
                    }
                    TileType.SHRINE_RUNE_PILLAR -> {
                        // Fluted Marble Pillar with Cyan Arcane Spiral
                        val pillarBase = Color(0xFF475569)
                        val pillarMarble = Color(0xFF64748B)
                        val runeCyan = Color(0xFF38BDF8)
                        drawScope.drawRect(color = pillarBase, topLeft = Offset(tx + 4f, ty), size = Size(ts - 8f, ts))
                        drawScope.drawRect(color = pillarMarble, topLeft = Offset(tx + 6f, ty), size = Size(ts - 12f, ts))
                        // Spiral Glowing Glyphs
                        val runeY = ty + (time * 12f + c * 8f) % ts
                        drawScope.drawCircle(color = runeCyan, radius = 2.5f, center = Offset(tx + 16f, runeY))
                        drawScope.drawCircle(color = Color.White, radius = 1f, center = Offset(tx + 16f, runeY))
                    }
                    TileType.CASTLE_WALL -> {
                        // Dark Iron Castle Ashlar Masonry
                        val castleDark = Color(0xFF0F172A)
                        val castleStone = Color(0xFF1E293B)
                        val castleHighlight = Color(0xFF334155)
                        drawScope.drawRect(color = castleDark, topLeft = Offset(tx, ty), size = Size(ts, ts))
                        // 2 rows of large stone bricks
                        drawScope.drawRoundRect(color = castleStone, topLeft = Offset(tx + 2f, ty + 2f), size = Size(13f, 12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f))
                        drawScope.drawRoundRect(color = castleStone, topLeft = Offset(tx + 17f, ty + 2f), size = Size(13f, 12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f))
                        drawScope.drawRoundRect(color = castleHighlight, topLeft = Offset(tx + 4f, ty + 17f), size = Size(24f, 12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f))
                    }
                    TileType.CASTLE_BANNER -> {
                        // Royal Dragon Crimson & Gold Banner
                        val castleStone = Color(0xFF1E293B)
                        drawScope.drawRect(color = castleStone, topLeft = Offset(tx, ty), size = Size(ts, ts))
                        // Hanging Banner
                        val bannerCrimson = Color(0xFF881337)
                        val bannerGold = Color(0xFFF59E0B)
                        val bannerSway = sin(time * 3f + c) * 2f
                        val bannerPath = Path().apply {
                            moveTo(tx + 6f, ty + 2f)
                            lineTo(tx + 26f, ty + 2f)
                            lineTo(tx + 26f + bannerSway, ty + ts)
                            lineTo(tx + 16f + bannerSway, ty + ts - 6f)
                            lineTo(tx + 6f + bannerSway, ty + ts)
                            close()
                        }
                        drawScope.drawPath(bannerPath, color = bannerCrimson)
                        drawScope.drawCircle(color = bannerGold, radius = 3.5f, center = Offset(tx + 16f + bannerSway * 0.5f, ty + 14f))
                    }
                    TileType.WATERFALL -> {
                        // Animated Rushing Crystalline Waterfall
                        val waterDeep = Color(0xCC0284C7)
                        val waterLight = Color(0xCC38BDF8)
                        val foamWhite = Color(0xEEF0F9FF)
                        drawScope.drawRect(color = waterDeep, topLeft = Offset(tx, ty), size = Size(ts, ts))

                        // Falling Water Ribbons
                        val flowOffset = (time * 120f) % ts
                        for (i in 0..3) {
                            val rx = tx + 3f + i * 7f
                            val ry1 = ty + (flowOffset + i * 8f) % ts
                            drawScope.drawLine(
                                color = waterLight,
                                start = Offset(rx, ry1),
                                end = Offset(rx, (ry1 + 10f).coerceAtMost(ty + ts)),
                                strokeWidth = 2.5f,
                                cap = StrokeCap.Round
                            )
                        }
                        // Splash Foam Mist
                        drawScope.drawOval(
                            color = foamWhite,
                            topLeft = Offset(tx + sin(time * 10f) * 3f, ty + ts - 6f),
                            size = Size(ts, 6f)
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    private fun drawBladeTuft(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color
    ) {
        val blade = Path().apply {
            moveTo(x, y + 2f)
            lineTo(x + width * 0.5f, y - height)
            lineTo(x + width, y + 2f)
            close()
        }
        drawScope.drawPath(blade, color = color)
    }

    fun drawForegroundAtmosphere(
        drawScope: DrawScope,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        // LAYER 5: Foreground Silhouette Branches & Leaves (Parallax 1.35x)
        val fgOffset = (camX * 1.35f) % (viewW * 1.2f)
        val fgColor = Color(0xCC061410)

        // Hanging Foreground Canopy Leaves (Top)
        for (i in 0..3) {
            val lx = camX + i * 260f - fgOffset
            val ly = camY - 10f
            drawScope.drawOval(
                color = fgColor,
                topLeft = Offset(lx, ly),
                size = Size(90f, 45f)
            )
            // Hanging vine fronds
            drawScope.drawLine(
                color = fgColor,
                start = Offset(lx + 45f, ly + 40f),
                end = Offset(lx + 40f, ly + 75f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}
