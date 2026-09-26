package com.example.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.engine.GameEngine
import com.example.game.entities.Player
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, multi-layer 2D parallax rendering system.
 *
 * Implements distinct scrolling depth planes based on player and camera movement:
 * - Background Layers (0.04x - 0.36x): Far celestial skies, distant mountain ranges, and mid-background landscapes.
 * - Middle Layers (0.68x): Near-midground scenery (pillars, arches, tree trunks, lanterns) placed right behind gameplay platforms.
 * - Gameplay Plane (1.00x): World tiles, puzzles, enemies, NPCs, and player.
 * - Near Foreground (1.25x): Lush hanging canopy fronds, stalactites, and pillar silhouettes scrolling faster in front of player.
 * - Extreme Foreground (1.55x): Large corner silhouette foliage and high-speed drifting particles close to the camera lens.
 */
object ParallaxRenderer {

    // Region width in world coordinates (320 cols * 32px = 10,240px total across 8 regions)
    private const val REGION_WIDTH = 1280f

    // =========================================================================
    // 1. BACKGROUND PARALLAX LAYERS (Far Sky, Distant Mountains, Mid-Background)
    // =========================================================================

    fun drawBackgroundLayers(
        drawScope: DrawScope,
        engine: GameEngine,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        val player = engine.player
        val worldProgress = (camX / (engine.world.width - viewW).coerceAtLeast(1f)).coerceIn(0f, 1f)
        val regionIdx = ((camX + viewW * 0.5f) / REGION_WIDTH).toInt().coerceIn(0, 7)

        // --- LAYER 1A: Sky Gradient & Celestial Bodies (0.04x Parallax) ---
        drawSkyAndCelestial(drawScope, camX, camY, viewW, viewH, time, regionIdx, worldProgress)

        // --- LAYER 1B: High-Altitude Clouds & Atmospheric Mist (0.10x Parallax + Wind) ---
        drawHighClouds(drawScope, camX, camY, viewW, viewH, time)

        // --- LAYER 2: Distant Mountain Ranges, Citadels & Horizons (0.16x Parallax) ---
        drawDistantMountains(drawScope, camX, camY, viewW, viewH, time, regionIdx)

        // --- LAYER 3: Mid-Background Scenery & Volumetric God Rays (0.36x Parallax) ---
        drawMidBackgroundScenery(drawScope, camX, camY, viewW, viewH, time, regionIdx, worldProgress)
    }

    private fun drawSkyAndCelestial(
        drawScope: DrawScope,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float,
        regionIdx: Int,
        worldProgress: Float
    ) {
        // Biome-specific atmospheric sky gradient
        val (skyTop, skyBottom) = when (regionIdx) {
            0 -> Pair(Color(0xFF0B1D28), Color(0xFF1E4B3E)) // 1. Whispering Woods: Twilight Forest Emerald
            1 -> Pair(Color(0xFF152636), Color(0xFFC97A3E)) // 2. Oakhaven Hamlet: Golden Dawn Amber
            2 -> Pair(Color(0xFF060810), Color(0xFF181528)) // 3. Deeproot Caverns: Subterranean Obsidian Amethyst
            3 -> Pair(Color(0xFF180F26), Color(0xFF6B3A5A)) // 4. Ancient Ruins: Violet Sunset Rose
            4 -> Pair(Color(0xFF0A1428), Color(0xFF22445E)) // 5. Frostpeak Summit: Alpine Indigo
            5 -> Pair(Color(0xFF070318), Color(0xFF2A1648)) // 6. Lunar Shrine: Deep Mystic Cosmos
            6 -> Pair(Color(0xFF0F1218), Color(0xFF322842)) // 7. Stormgate Castle: Tempest Slate Violet
            else -> Pair(Color(0xFF1A0609), Color(0xFF751E13)) // 8. Colossus Throne: Molten Crimson Abyss
        }

        // Sky rect pinned to camera viewport
        drawScope.drawRect(
            brush = Brush.verticalGradient(listOf(skyTop, skyBottom)),
            topLeft = Offset(camX, camY),
            size = Size(viewW, viewH)
        )

        // Celestial bodies move with ultra-slow parallax (0.04x horizontal, 0.02x vertical)
        val celestialParallaxX = ParallaxConfig.FACTOR_FAR_SKY_X
        val celestialParallaxY = ParallaxConfig.FACTOR_FAR_SKY_Y
        val celestialLoopW = viewW * 1.8f
        val celestialScrollX = ParallaxConfig.calculateLayerScrollOffset(camX, celestialParallaxX, celestialLoopW)
        val celestialBaseY = camY + viewH * 0.16f - camY * celestialParallaxY

        when (regionIdx) {
            0, 1 -> {
                // Radiant Sun with Shimmering Corona Glow (Forest & Hamlet)
                val sunX = camX + viewW * 0.72f - celestialScrollX * 0.4f
                val sunY = celestialBaseY

                drawScope.drawCircle(
                    color = Color(0x33FFD54F),
                    radius = 48f + sin(time * 2f) * 3f,
                    center = Offset(sunX, sunY)
                )
                drawScope.drawCircle(
                    color = Color(0x66FFE082),
                    radius = 32f,
                    center = Offset(sunX, sunY)
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFECB3),
                    radius = 20f,
                    center = Offset(sunX, sunY)
                )
            }
            4 -> {
                // Frostpeak Summit: Aurora Borealis undulating energy ribbons
                for (a in 0..2) {
                    val auroraPath = Path()
                    val aBaseY = camY + viewH * (0.08f + a * 0.07f) - camY * celestialParallaxY
                    auroraPath.moveTo(camX, aBaseY)
                    var px = 0f
                    while (px <= viewW) {
                        val wave = sin(time * 1.5f + px * 0.012f + a * 1.2f) * 18f
                        auroraPath.lineTo(camX + px, aBaseY + wave)
                        px += 25f
                    }
                    auroraPath.lineTo(camX + viewW, camY + viewH * 0.4f)
                    auroraPath.lineTo(camX, camY + viewH * 0.4f)
                    auroraPath.close()

                    val auroraColor = if (a == 0) Color(0x2E00E5FF) else if (a == 1) Color(0x2869F0AE) else Color(0x207C4DFF)
                    drawScope.drawPath(auroraPath, color = auroraColor)
                }
            }
            5 -> {
                // Lunar Shrine: Shimmering Crescent Moon & Constellations
                val moonX = camX + viewW * 0.68f - celestialScrollX * 0.35f
                val moonY = celestialBaseY - 15f

                // Outer sacred moon glow
                drawScope.drawCircle(
                    color = Color(0x26B388FF),
                    radius = 44f + sin(time * 2.5f) * 2f,
                    center = Offset(moonX, moonY)
                )
                // Moon disk
                drawScope.drawCircle(
                    color = Color(0xFFEDE7F6),
                    radius = 24f,
                    center = Offset(moonX, moonY)
                )
                // Shadow creating luminous crescent
                drawScope.drawCircle(
                    color = skyTop,
                    radius = 20f,
                    center = Offset(moonX + 9f, moonY - 5f)
                )

                // Twinkling Starlight Field
                for (s in 0..24) {
                    val starX = camX + ((s * 137f + 45f) % viewW)
                    val starY = camY + ((s * 79f + 20f) % (viewH * 0.45f)) - camY * celestialParallaxY
                    val twinkle = (sin(time * 3f + s * 1.7f) * 0.5f + 0.5f).coerceIn(0.2f, 1f)
                    drawScope.drawCircle(
                        color = Color(0xFFEDE7F6).copy(alpha = twinkle * 0.85f),
                        radius = if (s % 4 == 0) 1.8f else 1.1f,
                        center = Offset(starX, starY)
                    )
                }
            }
            6 -> {
                // Stormgate Castle: Distant Lightning Strike Flashes
                val flashIntensity = (sin(time * 7f) * cos(time * 11f)).coerceIn(0f, 1f)
                if (flashIntensity > 0.88f) {
                    drawScope.drawRect(
                        color = Color(0x44D1C4E9),
                        topLeft = Offset(camX, camY),
                        size = Size(viewW, viewH * 0.6f)
                    )
                }
            }
            else -> {}
        }
    }

    private fun drawHighClouds(
        drawScope: DrawScope,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        val windDrift = time * 14f
        val cloudLoopW = viewW * 1.4f
        val scrollOffset = (camX * 0.10f + windDrift) % cloudLoopW
        val cloudScrollX = if (scrollOffset < 0f) scrollOffset + cloudLoopW else scrollOffset

        for (k in -1..2) {
            val baseCloudX = camX + k * cloudLoopW - cloudScrollX
            val cloudY1 = camY + viewH * 0.10f - camY * 0.04f
            val cloudY2 = camY + viewH * 0.19f - camY * 0.05f

            // Upper Whispy Cloud Formation
            drawCloudPuff(drawScope, baseCloudX + 40f, cloudY1, 1.1f, Color(0x28FFFFFF))
            drawCloudPuff(drawScope, baseCloudX + viewW * 0.60f, cloudY2, 0.85f, Color(0x1FFFFFFF))
            drawCloudPuff(drawScope, baseCloudX + viewW * 1.10f, cloudY1 + 10f, 1.25f, Color(0x22ECEFF1))
        }
    }

    private fun drawCloudPuff(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        scale: Float,
        color: Color
    ) {
        drawScope.drawOval(
            color = color,
            topLeft = Offset(x, y + 8f * scale),
            size = Size(130f * scale, 32f * scale)
        )
        drawScope.drawCircle(
            color = color,
            radius = 22f * scale,
            center = Offset(x + 36f * scale, y + 12f * scale)
        )
        drawScope.drawCircle(
            color = color,
            radius = 28f * scale,
            center = Offset(x + 68f * scale, y + 6f * scale)
        )
        drawScope.drawCircle(
            color = color,
            radius = 18f * scale,
            center = Offset(x + 98f * scale, y + 14f * scale)
        )
    }

    private fun drawDistantMountains(
        drawScope: DrawScope,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float,
        regionIdx: Int
    ) {
        val mFactorX = ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_X
        val mFactorY = ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_Y

        val mountainLoopW = viewW * 1.25f
        val mScrollX = ParallaxConfig.calculateLayerScrollOffset(camX, mFactorX, mountainLoopW)
        val mBaseY = camY + viewH * 0.40f - camY * mFactorY

        val ridgeColor = when (regionIdx) {
            0 -> Color(0x40122E26)
            1 -> Color(0x442C2A3E)
            2 -> Color(0x48110F20)
            3 -> Color(0x48241733)
            4 -> Color(0x551E334D)
            5 -> Color(0x4A1E143E)
            6 -> Color(0x551C1A2E)
            else -> Color(0x5A3A1310)
        }

        for (k in -1..2) {
            val mx = camX + k * mountainLoopW - mScrollX
            val peakPath = Path().apply {
                moveTo(mx, mBaseY + viewH * 0.20f)
                lineTo(mx + mountainLoopW * 0.22f, mBaseY - 20f)
                lineTo(mx + mountainLoopW * 0.40f, mBaseY + 15f)
                lineTo(mx + mountainLoopW * 0.65f, mBaseY - 45f) // High Summit
                lineTo(mx + mountainLoopW * 0.85f, mBaseY + 10f)
                lineTo(mx + mountainLoopW, mBaseY + viewH * 0.20f)
                lineTo(mx + mountainLoopW, camY + viewH)
                lineTo(mx, camY + viewH)
                close()
            }
            drawScope.drawPath(peakPath, color = ridgeColor)

            // Snowcap Highlights for Alpine Frostpeak & Lunar Ridge
            if (regionIdx == 4 || regionIdx == 5) {
                val snowCap = Path().apply {
                    moveTo(mx + mountainLoopW * 0.60f, mBaseY - 30f)
                    lineTo(mx + mountainLoopW * 0.65f, mBaseY - 45f)
                    lineTo(mx + mountainLoopW * 0.70f, mBaseY - 28f)
                    lineTo(mx + mountainLoopW * 0.67f, mBaseY - 22f)
                    close()
                }
                drawScope.drawPath(snowCap, color = Color(0x66B0BEC5))
            }
        }
    }

    private fun drawMidBackgroundScenery(
        drawScope: DrawScope,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float,
        regionIdx: Int,
        worldProgress: Float
    ) {
        val bgFactorX = ParallaxConfig.FACTOR_MID_BACKGROUND_X
        val bgFactorY = ParallaxConfig.FACTOR_MID_BACKGROUND_Y

        val loopW = viewW * 0.95f
        val scrollX = ParallaxConfig.calculateLayerScrollOffset(camX, bgFactorX, loopW)
        val sceneryBaseY = camY + viewH * 0.46f - camY * bgFactorY

        val silColor = when (regionIdx) {
            0 -> Color(0x70173626)
            1 -> Color(0x7528343D)
            2 -> Color(0x80181426)
            3 -> Color(0x752B203B)
            4 -> Color(0x80253B4E)
            5 -> Color(0x75241845)
            6 -> Color(0x85242236)
            else -> Color(0x85441512)
        }

        for (k in -1..2) {
            val baseX = camX + k * loopW - scrollX

            when (regionIdx) {
                1 -> {
                    // Oakhaven Hamlet: Distant Windmill & Village Roofs
                    drawHamletWindmill(drawScope, baseX + 140f, sceneryBaseY, time, silColor)
                    drawVillageSilhouettes(drawScope, baseX + 280f, sceneryBaseY + 15f, silColor)
                }
                2 -> {
                    // Deeproot Caverns: Giant Stalactites & Crystal Shards
                    drawCavernPillars(drawScope, baseX + 80f, sceneryBaseY - 40f, viewH, silColor)
                    drawCavernPillars(drawScope, baseX + 260f, sceneryBaseY - 60f, viewH, silColor)
                }
                3, 5 -> {
                    // Ancient Ruins & Lunar Shrine: Crumbling Aqueducts & Torii Arches
                    drawRuinedAqueduct(drawScope, baseX + 50f, sceneryBaseY - 10f, loopW, silColor)
                }
                6 -> {
                    // Stormgate Keep: Fortress Battlements & Iron Spindles
                    drawCastleBattlements(drawScope, baseX + 40f, sceneryBaseY - 20f, loopW, silColor)
                }
                7 -> {
                    // Throne of Colossus: Shattered Monoliths
                    drawShatteredMonoliths(drawScope, baseX + 60f, sceneryBaseY - 30f, loopW, silColor)
                }
                else -> {
                    // Whispering Woods / Frostpeak: Layered Treelines
                    drawPineTreeline(drawScope, baseX, sceneryBaseY, loopW, silColor)
                }
            }
        }

        // Volumetric Sunbeams / God Rays
        if (regionIdx == 0 || regionIdx == 1 || regionIdx == 3) {
            val rayColor = if (regionIdx == 1) Color(0x18FFD54F) else Color(0x1480CBC4)
            for (i in 0..3) {
                val rayOffset = (camX * 0.20f + i * 210f) % (viewW * 1.4f)
                val rayX = camX + rayOffset - 60f
                val rayPath = Path().apply {
                    moveTo(rayX, camY)
                    lineTo(rayX + 50f, camY)
                    lineTo(rayX + 160f, camY + viewH)
                    lineTo(rayX + 30f, camY + viewH)
                    close()
                }
                drawScope.drawPath(rayPath, color = rayColor)
            }
        }
    }

    private fun drawPineTreeline(
        drawScope: DrawScope,
        baseX: Float,
        baseY: Float,
        width: Float,
        color: Color
    ) {
        val numTrees = 5
        val spacing = width / numTrees
        for (i in 0 until numTrees) {
            val tx = baseX + i * spacing + 20f
            val ty = baseY + (i % 3) * 12f

            // Tree trunk
            drawScope.drawRect(
                color = color,
                topLeft = Offset(tx + 14f, ty + 20f),
                size = Size(8f, 60f)
            )
            // Triangular foliage layers
            val tree = Path().apply {
                moveTo(tx + 18f, ty - 25f)
                lineTo(tx + 36f, ty + 25f)
                lineTo(tx, ty + 25f)
                close()
            }
            drawScope.drawPath(tree, color = color)
        }
    }

    private fun drawHamletWindmill(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        time: Float,
        color: Color
    ) {
        // Tower base
        val tower = Path().apply {
            moveTo(x + 12f, y - 45f)
            lineTo(x + 38f, y - 45f)
            lineTo(x + 46f, y + 30f)
            lineTo(x + 4f, y + 30f)
            close()
        }
        drawScope.drawPath(tower, color = color)

        // Rotating sails
        val hubX = x + 25f
        val hubY = y - 40f
        val angle = time * 1.8f
        for (b in 0..3) {
            val bladeAngle = angle + b * (PI.toFloat() * 0.5f)
            val bx = hubX + cos(bladeAngle) * 32f
            val by = hubY + sin(bladeAngle) * 32f
            drawScope.drawLine(
                color = color,
                start = Offset(hubX, hubY),
                end = Offset(bx, by),
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )
        }
    }

    private fun drawVillageSilhouettes(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        color: Color
    ) {
        // Cottage roofs
        val roof = Path().apply {
            moveTo(x, y + 10f)
            lineTo(x + 25f, y - 18f)
            lineTo(x + 50f, y + 10f)
            close()
        }
        drawScope.drawPath(roof, color = color)
        drawScope.drawRect(color = color, topLeft = Offset(x + 5f, y + 10f), size = Size(40f, 25f))
    }

    private fun drawCavernPillars(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        viewH: Float,
        color: Color
    ) {
        // Jagged stalactite coming from ceiling
        val stalactite = Path().apply {
            moveTo(x, y)
            lineTo(x + 35f, y)
            lineTo(x + 18f, y + 90f)
            close()
        }
        drawScope.drawPath(stalactite, color = color)
    }

    private fun drawRuinedAqueduct(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        width: Float,
        color: Color
    ) {
        // Horizontal aqueduct lintel
        drawScope.drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(width * 0.85f, 16f)
        )
        // Supporting arches
        for (i in 0..2) {
            val px = x + i * 110f + 20f
            drawScope.drawRect(
                color = color,
                topLeft = Offset(px, y + 16f),
                size = Size(20f, 50f)
            )
        }
    }

    private fun drawCastleBattlements(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        width: Float,
        color: Color
    ) {
        // Stone wall line
        drawScope.drawRect(color = color, topLeft = Offset(x, y + 15f), size = Size(width * 0.85f, 45f))
        // Crenellations
        for (i in 0..5) {
            drawScope.drawRect(
                color = color,
                topLeft = Offset(x + i * 45f, y),
                size = Size(24f, 15f)
            )
        }
    }

    private fun drawShatteredMonoliths(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        width: Float,
        color: Color
    ) {
        for (i in 0..2) {
            val mx = x + i * 95f
            val mono = Path().apply {
                moveTo(mx, y + 45f)
                lineTo(mx + 10f, y - 25f)
                lineTo(mx + 28f, y - 20f)
                lineTo(mx + 22f, y + 45f)
                close()
            }
            drawScope.drawPath(mono, color = color)
        }
    }

    // =========================================================================
    // 2. MIDDLE PARALLAX LAYER (Near-Midground Scenery behind World Platforms)
    // =========================================================================

    /**
     * Renders the near-midground scenery directly behind the gameplay tiles and platforms.
     * Scrolls at 0.68x horizontal and 0.38x vertical speed.
     */
    fun drawMidgroundLayer(
        drawScope: DrawScope,
        engine: GameEngine,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        val mgFactorX = ParallaxConfig.FACTOR_NEAR_MIDGROUND_X
        val mgFactorY = ParallaxConfig.FACTOR_NEAR_MIDGROUND_Y

        val loopW = viewW * 0.80f
        val scrollX = ParallaxConfig.calculateLayerScrollOffset(camX, mgFactorX, loopW)
        val mgBaseY = camY + viewH * 0.52f - camY * mgFactorY

        val regionIdx = ((camX + viewW * 0.5f) / REGION_WIDTH).toInt().coerceIn(0, 7)
        val mgColor = when (regionIdx) {
            0 -> Color(0xB0142B20) // Forest Deep Gnarled Trunks
            1 -> Color(0xB52B3138) // Hamlet Timber Beams
            2 -> Color(0xC01A172B) // Cavern Rock Columns
            3 -> Color(0xB82C223B) // Ruined Temple Pillars
            4 -> Color(0xC0263645) // Frostpeak Frozen Crags
            5 -> Color(0xB820153D) // Lunar Torii & Relics
            6 -> Color(0xC8232034) // Fortress Iron Portcullis & Stone
            else -> Color(0xD03E1310) // Colossus Ribs & Obelisks
        }

        for (k in -1..2) {
            val baseX = camX + k * loopW - scrollX

            when (regionIdx) {
                0, 1 -> {
                    // Massive Midground Tree Trunks & Hanging Foliage
                    drawMidgroundAncientTree(drawScope, baseX + 60f, mgBaseY - 80f, viewH, mgColor, time)
                    drawMidgroundAncientTree(drawScope, baseX + 280f, mgBaseY - 60f, viewH, mgColor, time + 1f)
                }
                2 -> {
                    // Cavern Stalagmite Columns & Glowing Crystal Clusters
                    drawMidgroundCavernPillars(drawScope, baseX + 80f, mgBaseY - 40f, viewH, mgColor, time)
                }
                3, 5 -> {
                    // Fluted Stone Archway & Classical Columns
                    drawMidgroundStoneColumns(drawScope, baseX + 70f, mgBaseY - 60f, mgColor)
                }
                6 -> {
                    // Castle Parapet with Billowing Crimson Banners
                    drawMidgroundCastleBanner(drawScope, baseX + 100f, mgBaseY - 50f, mgColor, time)
                }
                else -> {
                    // Colossus Skeletal Titan Remains & Giant Spikes
                    drawMidgroundTitanRemains(drawScope, baseX + 80f, mgBaseY - 50f, mgColor)
                }
            }
        }

        // Midground ambient pollen and dust motes reacting to player movement
        drawMidgroundSpores(drawScope, engine.player, camX, camY, viewW, viewH, time, regionIdx)
    }

    private fun drawMidgroundAncientTree(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        viewH: Float,
        color: Color,
        time: Float
    ) {
        // Deep Gnarled Trunk
        drawScope.drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(36f, viewH)
        )
        // Root buttress
        val rootPath = Path().apply {
            moveTo(x, y + 80f)
            lineTo(x - 22f, y + 140f)
            lineTo(x + 10f, y + 140f)
            close()
        }
        drawScope.drawPath(rootPath, color = color)

        // Hanging moss strand swaying gently
        val sway = sin(time * 2f + x * 0.05f) * 4f
        drawScope.drawLine(
            color = color.copy(alpha = 0.8f),
            start = Offset(x + 8f, y + 30f),
            end = Offset(x + 12f + sway, y + 75f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    private fun drawMidgroundCavernPillars(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        viewH: Float,
        color: Color,
        time: Float
    ) {
        // Thick stone pillar
        drawScope.drawRect(color = color, topLeft = Offset(x, y), size = Size(42f, viewH))

        // Embedded radiant mana crystal cluster glowing in the midground
        val glow = sin(time * 3f + x) * 2f
        drawScope.drawCircle(
            color = Color(0x3300E5FF),
            radius = 14f + glow,
            center = Offset(x + 21f, y + 55f)
        )
        val crystalShard = Path().apply {
            moveTo(x + 21f, y + 42f)
            lineTo(x + 29f, y + 55f)
            lineTo(x + 21f, y + 68f)
            lineTo(x + 13f, y + 55f)
            close()
        }
        drawScope.drawPath(crystalShard, color = Color(0xFF80DEEA))
    }

    private fun drawMidgroundStoneColumns(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        color: Color
    ) {
        // Classical fluted column with capital and base
        drawScope.drawRect(color = color, topLeft = Offset(x - 6f, y - 8f), size = Size(40f, 10f)) // Capital
        drawScope.drawRect(color = color, topLeft = Offset(x, y), size = Size(28f, 130f)) // Shaft
        drawScope.drawRect(color = color, topLeft = Offset(x - 4f, y + 130f), size = Size(36f, 12f)) // Base

        // Fluting lines
        drawScope.drawLine(
            color = Color(0x33000000),
            start = Offset(x + 9f, y + 2f),
            end = Offset(x + 9f, y + 128f),
            strokeWidth = 2f
        )
        drawScope.drawLine(
            color = Color(0x33000000),
            start = Offset(x + 19f, y + 2f),
            end = Offset(x + 19f, y + 128f),
            strokeWidth = 2f
        )
    }

    private fun drawMidgroundCastleBanner(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        color: Color,
        time: Float
    ) {
        // Battlements post
        drawScope.drawRect(color = color, topLeft = Offset(x, y), size = Size(32f, 140f))

        // Billowing heraldic banner
        val wave = sin(time * 4f) * 6f
        val banner = Path().apply {
            moveTo(x + 32f, y + 15f)
            lineTo(x + 85f + wave, y + 20f)
            lineTo(x + 75f + wave, y + 60f)
            lineTo(x + 85f + wave, y + 75f)
            lineTo(x + 32f, y + 65f)
            close()
        }
        drawScope.drawPath(banner, color = Color(0xCCB71C1C)) // Crimson banner
    }

    private fun drawMidgroundTitanRemains(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        color: Color
    ) {
        // Curved bone rib of the ancient colossus
        val rib = Path().apply {
            moveTo(x, y + 120f)
            cubicTo(x + 15f, y + 40f, x + 55f, y - 20f, x + 70f, y - 35f)
            lineTo(x + 82f, y - 30f)
            cubicTo(x + 65f, y - 10f, x + 28f, y + 45f, x + 16f, y + 120f)
            close()
        }
        drawScope.drawPath(rib, color = color)
    }

    private fun drawMidgroundSpores(
        drawScope: DrawScope,
        player: Player,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float,
        regionIdx: Int
    ) {
        val sporeFactorX = ParallaxConfig.FACTOR_NEAR_MIDGROUND_X
        val sporeFactorY = ParallaxConfig.FACTOR_NEAR_MIDGROUND_Y
        val sporeColor = if (regionIdx == 2 || regionIdx == 5) Color(0x9900E5FF) else if (regionIdx == 4) Color(0x99B0BEC5) else Color(0x99FFD54F)

        for (i in 0..12) {
            val rawX = (i * 97f + time * 24f - player.vel.x * 0.08f) % (viewW * 1.2f)
            val sx = camX + rawX - (camX * sporeFactorX) % (viewW * 1.2f)
            val sy = camY + (sin((time + i) * 2f) * 25f) + viewH * 0.35f + (i * 28f % (viewH * 0.5f)) - camY * sporeFactorY

            drawScope.drawCircle(
                color = sporeColor,
                radius = 2.0f,
                center = Offset(sx, sy)
            )
        }
    }

    // =========================================================================
    // 3. NEAR FOREGROUND LAYER (Overhanging Canopies, Fronds, Pillars at 1.25x)
    // =========================================================================

    /**
     * Renders near foreground elements in front of the player, platforms, and tiles.
     * Scrolls at 1.25x horizontal and 1.18x vertical speed.
     */
    fun drawNearForegroundLayer(
        drawScope: DrawScope,
        engine: GameEngine,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        val fgFactorX = ParallaxConfig.FACTOR_NEAR_FOREGROUND_X
        val fgFactorY = ParallaxConfig.FACTOR_NEAR_FOREGROUND_Y

        val loopW = viewW * 1.2f
        val scrollX = ParallaxConfig.calculateLayerScrollOffset(camX, fgFactorX, loopW)
        val fgBaseY = camY - camY * (fgFactorY - 1f)

        val regionIdx = ((camX + viewW * 0.5f) / REGION_WIDTH).toInt().coerceIn(0, 7)
        val canopyColor = when (regionIdx) {
            0 -> Color(0xD9061610) // Whispering Woods deep silhouette green
            1 -> Color(0xDD121A16) // Hamlet dark thatched overhangs
            2 -> Color(0xEA080712) // Deeproot dark cavern stalactite silhouettes
            3 -> Color(0xE0130E1F) // Ancient Ruins crumbling stone arch
            4 -> Color(0xDD0D1824) // Frostpeak icy rock silhouettes
            5 -> Color(0xDD0F0824) // Lunar Shrine mystical willow silhouette
            6 -> Color(0xE5100E1A) // Stormgate heavy fortress ramparts
            else -> Color(0xE81A0505) // Colossus jagged volcanic obsidian
        }

        // Top Canopy & Hanging Fronds (Moves faster than world as player runs)
        for (k in -1..2) {
            val baseX = camX + k * loopW - scrollX

            // Hanging lush leafy canopy arches (top of screen)
            for (i in 0..2) {
                val lx = baseX + i * 290f + 30f
                val ly = fgBaseY - 15f

                // Overhanging canopy leaf mass
                drawScope.drawOval(
                    color = canopyColor,
                    topLeft = Offset(lx, ly),
                    size = Size(140f, 65f)
                )

                // Dangling vines / moss tendrils
                val vineSway = sin(time * 3f + i * 1.4f) * 6f
                drawScope.drawLine(
                    color = canopyColor,
                    start = Offset(lx + 45f, ly + 50f),
                    end = Offset(lx + 40f + vineSway, ly + 105f),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = canopyColor,
                    start = Offset(lx + 95f, ly + 45f),
                    end = Offset(lx + 100f + vineSway * 0.7f, ly + 85f),
                    strokeWidth = 2.8f,
                    cap = StrokeCap.Round
                )
            }

            // Foreground framing pillars / crags sliding past swiftly
            if (regionIdx == 3 || regionIdx == 6) {
                // Massive stone pillar in foreground
                val pillarX = baseX + 210f
                drawScope.drawRect(
                    color = canopyColor,
                    topLeft = Offset(pillarX, fgBaseY - 20f),
                    size = Size(38f, viewH + 40f)
                )
            } else if (regionIdx == 4) {
                // Foreground hanging icicles
                for (ice in 0..4) {
                    val ix = baseX + ice * 75f + 40f
                    val icicle = Path().apply {
                        moveTo(ix, fgBaseY)
                        lineTo(ix + 12f, fgBaseY)
                        lineTo(ix + 6f, fgBaseY + 35f + (ice % 3) * 15f)
                        close()
                    }
                    drawScope.drawPath(icicle, color = Color(0xCCCFD8DC))
                }
            }

            // Lower foreground grass tufts & mossy rock mounds along the bottom screen edge
            val bottomY = camY + viewH - 24f - camY * (fgFactorY - 1f)
            val moundPath = Path().apply {
                moveTo(baseX + 40f, bottomY + 30f)
                cubicTo(baseX + 70f, bottomY - 12f, baseX + 120f, bottomY - 18f, baseX + 160f, bottomY + 30f)
                close()
            }
            drawScope.drawPath(moundPath, color = canopyColor)
        }
    }

    // =========================================================================
    // 4. EXTREME FOREGROUND LAYER (High-Speed Drifting Leaves, Bokeh at 1.55x)
    // =========================================================================

    /**
     * Renders close-to-camera cinematic elements that scroll significantly faster
     * than camera/player movement (1.55x horizontal and 1.45x vertical speed).
     */
    fun drawExtremeForegroundLayer(
        drawScope: DrawScope,
        engine: GameEngine,
        camX: Float,
        camY: Float,
        viewW: Float,
        viewH: Float,
        time: Float
    ) {
        val exFactorX = ParallaxConfig.FACTOR_EXTREME_FOREGROUND_X
        val exFactorY = ParallaxConfig.FACTOR_EXTREME_FOREGROUND_Y

        val loopW = viewW * 1.4f
        val scrollX = ParallaxConfig.calculateLayerScrollOffset(camX, exFactorX, loopW)
        val exBaseY = camY - camY * (exFactorY - 1f)

        val player = engine.player
        val regionIdx = ((camX + viewW * 0.5f) / REGION_WIDTH).toInt().coerceIn(0, 7)

        val cornerLeafColor = when (regionIdx) {
            0 -> Color(0xEE040D09)
            1 -> Color(0xF00A100D)
            2 -> Color(0xF5040308)
            3 -> Color(0xF00B0714)
            4 -> Color(0xF0081017)
            5 -> Color(0xF00A051A)
            6 -> Color(0xF5080710)
            else -> Color(0xF5120303)
        }

        // 1. Extreme Close-Up Silhouette Corner Fronds (Vignette Framing)
        for (k in -1..2) {
            val baseX = camX + k * loopW - scrollX

            // Giant foreground fern leaf dipping into top-left
            val fernLeft = Path().apply {
                moveTo(baseX - 20f, exBaseY - 30f)
                cubicTo(baseX + 60f, exBaseY + 10f, baseX + 130f, exBaseY + 50f, baseX + 170f, exBaseY + 95f)
                lineTo(baseX + 150f, exBaseY + 105f)
                cubicTo(baseX + 100f, exBaseY + 65f, baseX + 40f, exBaseY + 25f, baseX - 30f, exBaseY + 15f)
                close()
            }
            drawScope.drawPath(fernLeft, color = cornerLeafColor)

            // Giant foreground fern leaf reaching up from bottom-right
            val bottomY = camY + viewH + 20f - camY * (exFactorY - 1f)
            val fernRight = Path().apply {
                moveTo(baseX + loopW * 0.75f, bottomY + 30f)
                cubicTo(baseX + loopW * 0.70f, bottomY - 60f, baseX + loopW * 0.65f, bottomY - 95f, baseX + loopW * 0.58f, bottomY - 130f)
                lineTo(baseX + loopW * 0.61f, bottomY - 135f)
                cubicTo(baseX + loopW * 0.70f, bottomY - 90f, baseX + loopW * 0.78f, bottomY - 45f, baseX + loopW * 0.85f, bottomY + 30f)
                close()
            }
            drawScope.drawPath(fernRight, color = cornerLeafColor)
        }

        // 2. High-Speed Velocity-Responsive Drifting Particles (Leaves, Embers, Snowflakes)
        // Particles move with player velocity + extreme parallax scroll
        val particleScrollX = ParallaxConfig.calculateLayerScrollOffset(camX, exFactorX, viewW * 1.5f)
        val particleScrollY = camY * (exFactorY - 1f)

        val particleColor = when (regionIdx) {
            4 -> Color(0xCCECEFF1) // Frostpeak snow flurries
            7 -> Color(0xE6FF5722) // Colossus molten fiery embers
            5 -> Color(0xD9B388FF) // Lunar starlight sparks
            2 -> Color(0xD900E5FF) // Crystal cavern luminous sparks
            else -> Color(0xD9FFB74D) // Autumn forest drifting leaves / golden spores
        }

        val velBoostX = -player.vel.x * 0.15f
        val velBoostY = -player.vel.y * 0.12f

        for (p in 0..15) {
            val pxBase = (p * 153f + time * 65f + velBoostX) % (viewW * 1.5f)
            val px = camX + pxBase - particleScrollX
            val py = camY + ((p * 83f + time * 35f + velBoostY) % (viewH * 1.2f)) - particleScrollY - 20f

            // Soft cinematic depth-of-field circle with motion tilt
            val pSize = 3.5f + (p % 3) * 1.8f
            drawScope.drawCircle(
                color = particleColor.copy(alpha = 0.75f),
                radius = pSize,
                center = Offset(px, py)
            )

            // Inner bright core
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = pSize * 0.45f,
                center = Offset(px - 0.5f, py - 0.5f)
            )
        }
    }
}
