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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameEngine
import com.example.game.entities.Collectible
import com.example.game.entities.CollectibleType
import com.example.game.entities.Particle
import com.example.game.entities.ParticleType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameRenderer {

    private val npcRenderer = NPCRenderer()

    fun render(
        drawScope: DrawScope,
        engine: GameEngine,
        textMeasurer: TextMeasurer
    ) {
        val canvasW = drawScope.size.width
        val canvasH = drawScope.size.height

        // Close-up camera zoom scale so player character is ~15.5% of the screen height
        val targetPlayerHeightRatio = 0.155f
        val cameraScale = (canvasH * targetPlayerHeightRatio / engine.player.height).coerceIn(1.2f, 3.5f)

        val viewW = canvasW / cameraScale
        val viewH = canvasH / cameraScale

        engine.screenWidth = viewW
        engine.screenHeight = viewH

        // Camera render position includes clamped screen shake strictly bounded within the level
        val camX = engine.camera.renderPosition.x
        val camY = engine.camera.renderPosition.y

        drawScope.withTransform({
            scale(cameraScale, cameraScale, Offset.Zero)
            translate(left = -camX, top = -camY)
        }) {
            // 1. Multi-Layer Parallax Backgrounds (Deep Celestial Sky, High Clouds, Distant Mountain Ridges, Mid-Background)
            ParallaxRenderer.drawBackgroundLayers(this, engine, camX, camY, viewW, viewH, engine.gameTime)

            // 2. Near-Midground Scenery Layer (Gnarled Trees, Ancient Pillars, Cavern Columns, Banners at 0.68x)
            ParallaxRenderer.drawMidgroundLayer(this, engine, camX, camY, viewW, viewH, engine.gameTime)

            // 3. High-Detail Hand-Painted 2D World Tiles (Grass, Earth, Stone, Wood, Snow, Crystals - 1.00x Gameplay Plane)
            EnvironmentRenderer.drawWorldTiles(this, engine, camX, camY, viewW, viewH, engine.gameTime)

            // 4. Puzzle Mechanisms (Moving Platforms, Pressure Plates, Levers, Gates, Torches, Walls)
            PuzzleRenderer.renderPuzzles(this, engine.world, engine.gameTime)

            // 5. Interactive NPCs (Elder, Blacksmith, Merchant, Guard, Traveler, Explorer, Scholar, Hermit)
            npcRenderer.renderNPCs(
                drawScope = this,
                npcs = engine.npcs,
                cameraPos = com.example.game.model.Vector2D(0f, 0f),
                screenWidth = Float.MAX_VALUE,
                screenHeight = Float.MAX_VALUE,
                time = engine.gameTime,
                playerPos = engine.player.pos
            )

            // 6. Collectibles (Mana Crystals, Gold Coins, Chests, Keys, Heart Fragments, Lore Tablets, Secrets)
            drawCollectibles(this, engine.collectibles, engine.gameTime)

            // 7. Enemies & Bosses (Moss Slime, Wolf, Wurm, Lich, Stone Giant, Skeleton Knight, Ruin Colossus)
            for (enemy in engine.enemies) {
                EnemyRenderer.draw(this, enemy, 0f, 0f, engine.gameTime)
            }

            // 8. Human Fantasy Adventurer Player Character (Detailed Anatomy, Armor & Weapon)
            PlayerCharacterRenderer.draw(this, engine.player, 0f, 0f, engine.gameTime)

            // 9. Near Foreground Layer (Overhanging Canopies, Moss Fronds, Foreground Pillars at 1.25x in front of Player)
            ParallaxRenderer.drawNearForegroundLayer(this, engine, camX, camY, viewW, viewH, engine.gameTime)

            // 10. Extreme Foreground Layer (Close Silhouette Foliage & High-Speed Velocity Drifting Particles at 1.55x)
            ParallaxRenderer.drawExtremeForegroundLayer(this, engine, camX, camY, viewW, viewH, engine.gameTime)

            // 11. Particles & Floating Combat Damage Numbers
            drawParticles(this, engine.particles, textMeasurer)
        }
    }

    private fun drawCollectibles(
        drawScope: DrawScope,
        collectibles: List<Collectible>,
        time: Float
    ) {
        for (c in collectibles) {
            if (c.isCollected && c.type != CollectibleType.CHEST && c.type != CollectibleType.SECRET_CHEST && c.type != CollectibleType.CHECKPOINT && c.type != CollectibleType.LORE_TABLET) continue

            val cx = c.pos.x
            val cy = c.pos.y

            when (c.type) {
                CollectibleType.COIN -> {
                    // Spinning Gold Coin with Shimmer Gleam
                    val spin = abs(cos(time * 6f + c.animTimer))
                    val coinW = c.width * spin.coerceAtLeast(0.2f)
                    val left = cx + (c.width - coinW) * 0.5f

                    drawScope.drawOval(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(left, cy),
                        size = Size(coinW, c.height)
                    )
                    drawScope.drawOval(
                        color = Color(0xFFFF8F00),
                        topLeft = Offset(left + 1.5f, cy + 1.5f),
                        size = Size((coinW - 3f).coerceAtLeast(1f), c.height - 3f),
                        style = Stroke(width = 1.5f)
                    )
                }

                CollectibleType.MANA_CRYSTAL -> {
                    // Radiant Mana Crystal Shard
                    val crystalGlow = sin(time * 4f + c.animTimer) * 2f
                    drawScope.drawCircle(
                        color = Color(0x3300E5FF),
                        radius = 16f + crystalGlow,
                        center = Offset(cx + c.width * 0.5f, cy + c.height * 0.5f)
                    )
                    val crystalPath = Path().apply {
                        moveTo(cx + c.width * 0.5f, cy)
                        lineTo(cx + c.width, cy + c.height * 0.4f)
                        lineTo(cx + c.width * 0.5f, cy + c.height)
                        lineTo(cx, cy + c.height * 0.4f)
                        close()
                    }
                    drawScope.drawPath(
                        path = crystalPath,
                        brush = Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF0097A7)))
                    )
                }

                CollectibleType.HEALTH_POTION -> {
                    // Crimson Elixir Flask with Glass Shimmer
                    drawScope.drawCircle(
                        color = Color(0xFFD50000),
                        radius = 9f,
                        center = Offset(cx + c.width * 0.5f, cy + c.height * 0.6f)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFF5252),
                        radius = 4f,
                        center = Offset(cx + c.width * 0.5f - 2f, cy + c.height * 0.55f)
                    )
                    drawScope.drawRect(
                        color = Color(0xFFECEFF1),
                        topLeft = Offset(cx + c.width * 0.5f - 4f, cy + 2f),
                        size = Size(8f, 7f)
                    )
                    drawScope.drawRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(cx + c.width * 0.5f - 3f, cy),
                        size = Size(6f, 3f)
                    )
                }

                CollectibleType.ANCIENT_KEY -> {
                    // Golden Ornate Citadel Key with Magic Pulsing Sparkles
                    val keyGlow = sin(time * 5f) * 3f
                    drawScope.drawCircle(
                        color = Color(0x55FFD700),
                        radius = 14f + keyGlow,
                        center = Offset(cx + c.width * 0.5f, cy + 8f)
                    )
                    // Key Ring Top
                    drawScope.drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 7f,
                        center = Offset(cx + c.width * 0.5f, cy + 7f),
                        style = Stroke(width = 3.5f)
                    )
                    // Key Stem
                    drawScope.drawRect(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(cx + c.width * 0.5f - 2f, cy + 12f),
                        size = Size(4f, 16f)
                    )
                    // Key Teeth
                    drawScope.drawRect(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(cx + c.width * 0.5f + 2f, cy + 20f),
                        size = Size(6f, 3f)
                    )
                    drawScope.drawRect(
                        color = Color(0xFFFFD700),
                        topLeft = Offset(cx + c.width * 0.5f + 2f, cy + 25f),
                        size = Size(5f, 3f)
                    )
                }

                CollectibleType.HEART_FRAGMENT -> {
                    // Radiant Heart of Aether Fragment (Pulsing Divine Relic)
                    val heartCenter = Offset(cx + c.width * 0.5f, cy + c.height * 0.5f)
                    val pulse = sin(time * 6f) * 3f

                    // Divine Aura Rays
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xAA00E5FF), Color(0x44FFD700), Color.Transparent),
                            center = heartCenter,
                            radius = 24f + pulse
                        ),
                        radius = 24f + pulse,
                        center = heartCenter
                    )

                    // Crystalline Heart Shard Path
                    val shardPath = Path().apply {
                        moveTo(heartCenter.x, heartCenter.y - 12f)
                        cubicTo(heartCenter.x + 12f, heartCenter.y - 18f, heartCenter.x + 16f, heartCenter.y - 4f, heartCenter.x, heartCenter.y + 14f)
                        cubicTo(heartCenter.x - 16f, heartCenter.y - 4f, heartCenter.x - 12f, heartCenter.y - 18f, heartCenter.x, heartCenter.y - 12f)
                        close()
                    }
                    drawScope.drawPath(
                        path = shardPath,
                        brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFF00E5FF), Color(0xFF7C4DFF)))
                    )
                    drawScope.drawPath(path = shardPath, color = Color(0xFFFFD700), style = Stroke(width = 2f))
                }

                CollectibleType.LORE_TABLET -> {
                    // Ancient Weathered Runic Obelisk
                    val tabletPath = Path().apply {
                        moveTo(cx + 4f, cy + c.height)
                        lineTo(cx + 6f, cy + 6f)
                        lineTo(cx + c.width * 0.5f, cy)
                        lineTo(cx + c.width - 6f, cy + 6f)
                        lineTo(cx + c.width - 4f, cy + c.height)
                        close()
                    }
                    drawScope.drawPath(tabletPath, color = Color(0xFF334155))
                    drawScope.drawPath(tabletPath, color = Color(0xFF94A3B8), style = Stroke(width = 1.5f))

                    // Glowing Inscribed Runes
                    val runeGlow = sin(time * 3f + c.animTimer) * 0.3f + 0.7f
                    drawScope.drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = runeGlow),
                        radius = 2.5f,
                        center = Offset(cx + c.width * 0.5f, cy + 12f)
                    )
                    drawScope.drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = runeGlow),
                        start = Offset(cx + 8f, cy + 18f),
                        end = Offset(cx + c.width - 8f, cy + 18f),
                        strokeWidth = 1.5f
                    )
                    drawScope.drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = runeGlow),
                        start = Offset(cx + 9f, cy + 24f),
                        end = Offset(cx + c.width - 9f, cy + 24f),
                        strokeWidth = 1.5f
                    )
                }

                CollectibleType.CHEST, CollectibleType.SECRET_CHEST -> {
                    val isSecret = c.type == CollectibleType.SECRET_CHEST
                    val woodCol = if (isSecret) Color(0xFF263238) else Color(0xFF4E342E)
                    val trimCol = if (isSecret) Color(0xFF00E5FF) else Color(0xFFFFD700)

                    if (c.isOpened) {
                        // Opened Treasure Chest with Glow
                        drawScope.drawRoundRect(
                            color = woodCol,
                            topLeft = Offset(cx, cy + 12f),
                            size = Size(c.width, c.height - 12f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                        drawScope.drawRect(
                            color = if (isSecret) Color(0xFF37474F) else Color(0xFF6D4C41),
                            topLeft = Offset(cx - 2f, cy + 2f),
                            size = Size(c.width + 4f, 8f)
                        )
                        drawScope.drawOval(
                            color = trimCol,
                            topLeft = Offset(cx + 4f, cy + 8f),
                            size = Size(c.width - 8f, 6f)
                        )
                    } else {
                        // Closed Chest with Filigree Bands & Lock
                        drawScope.drawRoundRect(
                            color = woodCol,
                            topLeft = Offset(cx, cy + 6f),
                            size = Size(c.width, c.height - 6f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                        )
                        drawScope.drawRect(
                            color = if (isSecret) Color(0xFF37474F) else Color(0xFF6D4C41),
                            topLeft = Offset(cx, cy + 2f),
                            size = Size(c.width, 10f)
                        )
                        drawScope.drawRect(color = trimCol, topLeft = Offset(cx + 4f, cy + 2f), size = Size(3.5f, c.height - 2f))
                        drawScope.drawRect(color = trimCol, topLeft = Offset(cx + c.width - 7.5f, cy + 2f), size = Size(3.5f, c.height - 2f))
                        drawScope.drawCircle(color = trimCol, radius = 4f, center = Offset(cx + c.width * 0.5f, cy + 13f))
                    }
                }

                CollectibleType.BRASS_COMPASS -> {
                    // Brass Navigator Compass
                    val needleAngle = time * 2f
                    drawScope.drawCircle(color = Color(0xFFB8860B), radius = 10f, center = Offset(cx + 10f, cy + 10f))
                    drawScope.drawCircle(color = Color(0xFFFFD700), radius = 8f, center = Offset(cx + 10f, cy + 10f))
                    drawScope.drawLine(
                        color = Color(0xFFD50000),
                        start = Offset(cx + 10f, cy + 10f),
                        end = Offset(cx + 10f + cos(needleAngle) * 6f, cy + 10f + sin(needleAngle) * 6f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }

                CollectibleType.MAP_PARCHMENT -> {
                    // Rolled Ancient Map Parchment
                    drawScope.drawRoundRect(
                        color = Color(0xFFD7CCC8),
                        topLeft = Offset(cx + 2f, cy + 4f),
                        size = Size(18f, 14f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                    drawScope.drawLine(color = Color(0xFF8D6E63), start = Offset(cx + 5f, cy + 8f), end = Offset(cx + 17f, cy + 8f), strokeWidth = 1.5f)
                    drawScope.drawLine(color = Color(0xFF8D6E63), start = Offset(cx + 5f, cy + 12f), end = Offset(cx + 14f, cy + 12f), strokeWidth = 1.5f)
                    drawScope.drawCircle(color = Color(0xFFD50000), radius = 2f, center = Offset(cx + 16f, cy + 12f))
                }

                CollectibleType.GUARD_BADGE -> {
                    // Royal Sentry Crest
                    drawScope.drawCircle(color = Color(0xFF78909C), radius = 9f, center = Offset(cx + 10f, cy + 10f))
                    drawScope.drawCircle(color = Color(0xFF00E5FF), radius = 4f, center = Offset(cx + 10f, cy + 10f))
                }

                CollectibleType.RUNIC_ORE -> {
                    // Raw Runic Crystal Ore
                    val orePath = Path().apply {
                        moveTo(cx + 10f, cy + 2f)
                        lineTo(cx + 18f, cy + 8f)
                        lineTo(cx + 16f, cy + 18f)
                        lineTo(cx + 4f, cy + 16f)
                        lineTo(cx + 2f, cy + 8f)
                        close()
                    }
                    drawScope.drawPath(orePath, color = Color(0xFF455A64))
                    drawScope.drawCircle(color = Color(0xFF00E5FF), radius = 3f, center = Offset(cx + 10f, cy + 10f))
                }

                CollectibleType.RELIC_CHALICE -> {
                    // Golden Sacred Chalice
                    drawScope.drawOval(color = Color(0xFFFFD700), topLeft = Offset(cx + 3f, cy + 2f), size = Size(16f, 8f))
                    drawScope.drawRect(color = Color(0xFFFFD700), topLeft = Offset(cx + 9f, cy + 10f), size = Size(4f, 8f))
                    drawScope.drawOval(color = Color(0xFFFFD700), topLeft = Offset(cx + 5f, cy + 17f), size = Size(12f, 4f))
                    drawScope.drawCircle(color = Color(0xFF00E5FF), radius = 2f, center = Offset(cx + 11f, cy + 6f))
                }

                CollectibleType.CHECKPOINT -> {
                    // Ancient Runic Monolith Pillar
                    val stoneColor = if (c.isActivated) Color(0xFF2E7D32) else Color(0xFF37474F)
                    val runeColor = if (c.isActivated) Color(0xFF00E676) else Color(0xFF78909C)

                    drawScope.drawRect(color = Color(0xFF212121), topLeft = Offset(cx - 4f, cy + c.height - 10f), size = Size(c.width + 8f, 10f))
                    drawScope.drawRoundRect(
                        color = stoneColor,
                        topLeft = Offset(cx + 4f, cy + 8f),
                        size = Size(c.width - 8f, c.height - 16f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )

                    val crystalY = cy - 2f + sin(time * 4f) * 3.5f
                    val topPath = Path().apply {
                        moveTo(cx + c.width * 0.5f, crystalY)
                        lineTo(cx + c.width * 0.5f + 8f, crystalY + 12f)
                        lineTo(cx + c.width * 0.5f, crystalY + 20f)
                        lineTo(cx + c.width * 0.5f - 8f, crystalY + 12f)
                        close()
                    }
                    drawScope.drawPath(topPath, color = runeColor)
                    if (c.isActivated) {
                        drawScope.drawCircle(
                            color = Color(0x4400E676),
                            radius = 26f + sin(time * 5f) * 4f,
                            center = Offset(cx + c.width * 0.5f, crystalY + 10f)
                        )
                    }
                }

                CollectibleType.VICTORY_PORTAL -> {
                    val portalCenter = Offset(cx + c.width * 0.5f, cy + c.height * 0.5f)

                    drawScope.drawArc(
                        color = Color(0xFF37474F),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(cx, cy),
                        size = Size(c.width, c.height),
                        style = Stroke(width = 9f)
                    )

                    val portalGlow = Brush.radialGradient(
                        listOf(Color.White, Color(0xFF7C4DFF), Color(0xFF00E5FF), Color(0x00000000)),
                        center = portalCenter,
                        radius = c.width * 0.55f
                    )
                    drawScope.drawCircle(brush = portalGlow, radius = c.width * 0.5f, center = portalCenter)

                    for (i in 0..5) {
                        val angle = time * 2.5f + (i * PI.toFloat() / 3f)
                        val rx = portalCenter.x + cos(angle) * (c.width * 0.36f)
                        val ry = portalCenter.y + sin(angle) * (c.height * 0.36f)
                        drawScope.drawCircle(color = Color(0xFF00E5FF), radius = 3.2f, center = Offset(rx, ry))
                    }
                }
            }
        }
    }

    private fun drawParticles(
        drawScope: DrawScope,
        particles: List<Particle>,
        textMeasurer: TextMeasurer
    ) {
        for (p in particles) {
            val px = p.pos.x
            val py = p.pos.y

            when (p.type) {
                ParticleType.DAMAGE_TEXT -> {
                    val alpha = p.alpha
                    val textLayoutResult = textMeasurer.measure(
                        text = p.text,
                        style = TextStyle(
                            color = p.color.copy(alpha = alpha),
                            fontSize = p.size.sp,
                            fontWeight = if (p.isCritical) FontWeight.ExtraBold else FontWeight.Bold
                        )
                    )
                    drawScope.drawText(
                        textMeasurer = textMeasurer,
                        text = p.text,
                        topLeft = Offset(px + 1f, py + 1f),
                        style = TextStyle(
                            color = Color.Black.copy(alpha = alpha * 0.75f),
                            fontSize = p.size.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawScope.drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(px, py)
                    )
                }

                ParticleType.SLASH_SPARK -> {
                    val alpha = p.alpha
                    drawScope.drawLine(
                        color = p.color.copy(alpha = alpha),
                        start = Offset(px, py),
                        end = Offset(px + p.velocity.x * 0.08f, py + p.velocity.y * 0.08f),
                        strokeWidth = p.size,
                        cap = StrokeCap.Round
                    )
                }

                ParticleType.COIN_SPARKLE, ParticleType.BLOOD_SPLOTCH, ParticleType.DEATH_POOF -> {
                    val alpha = p.alpha
                    drawScope.drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (p.life / p.maxLife),
                        center = Offset(px, py)
                    )
                }

                else -> {
                    val alpha = p.alpha
                    drawScope.drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (p.life / p.maxLife),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
