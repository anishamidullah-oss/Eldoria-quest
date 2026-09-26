package com.example.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.entities.Enemy
import com.example.game.entities.EnemyAIState
import com.example.game.entities.EnemyType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object EnemyRenderer {

    fun draw(
        drawScope: DrawScope,
        enemy: Enemy,
        camX: Float,
        camY: Float,
        time: Float
    ) {
        if (enemy.aiState == EnemyAIState.DEAD) return

        val ex = enemy.pos.x - camX
        val ey = enemy.pos.y - camY
        val isHurt = enemy.hurtTimer > 0f
        val facingRight = enemy.facingRight
        val dir = if (facingRight) 1f else -1f
        val w = enemy.width
        val h = enemy.height
        val cx = ex + w * 0.5f

        when (enemy.type) {
            EnemyType.GRASSWALKER, EnemyType.BUSH_BEETLE -> {
                // ICONIC GRASSWALKER & BUSH BEETLE (Spider-like crawler with leafy bush shell and glowing eyes)
                val isBeetle = enemy.type == EnemyType.BUSH_BEETLE
                val walkCycle = sin(time * 18f)
                val legCycle1 = sin(time * 16f) * 6f
                val legCycle2 = sin(time * 16f + PI.toFloat() / 2f) * 6f
                val legCycle3 = sin(time * 16f + PI.toFloat()) * 6f

                val bodyColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF121214)
                val shellColor = if (isBeetle) Color(0xFF2E7D32) else Color(0xFF43A047)
                val shellAccent = if (isBeetle) Color(0xFF1B5E20) else Color(0xFF2E7D32)

                // 1. Jointed Spindly Crawler Spider Legs
                val legPairs = listOf(
                    Triple(-10f, 6f, legCycle1),
                    Triple(-4f, 8f, legCycle2),
                    Triple(4f, 8f, legCycle3),
                    Triple(10f, 6f, -legCycle1)
                )

                for ((lx, lHeight, anim) in legPairs) {
                    val legBaseX = cx + lx * dir
                    val legBaseY = ey + h * 0.55f
                    val legKneeX = legBaseX + (lx * 0.7f * dir) + anim * 0.5f
                    val legKneeY = ey + 2f
                    val legFootX = legBaseX + (lx * 1.3f * dir) + anim
                    val legFootY = ey + h

                    // Thigh
                    drawScope.drawLine(
                        color = Color(0xFF1E1E24),
                        start = Offset(legBaseX, legBaseY),
                        end = Offset(legKneeX, legKneeY),
                        strokeWidth = 2.8f,
                        cap = StrokeCap.Round
                    )
                    // Calf
                    drawScope.drawLine(
                        color = Color(0xFF121214),
                        start = Offset(legKneeX, legKneeY),
                        end = Offset(legFootX, legFootY),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                    // Foot claw
                    drawScope.drawCircle(
                        color = Color(0xFF2C3E50),
                        radius = 1.2f,
                        center = Offset(legFootX, legFootY)
                    )
                }

                // 2. Shiny Black Thorax Body
                drawScope.drawOval(
                    color = bodyColor,
                    topLeft = Offset(cx - w * 0.4f, ey + h * 0.25f),
                    size = Size(w * 0.8f, h * 0.65f)
                )
                // Body Gloss Highlight
                drawScope.drawOval(
                    color = Color(0x33FFFFFF),
                    topLeft = Offset(cx - w * 0.25f, ey + h * 0.3f),
                    size = Size(w * 0.5f, h * 0.2f)
                )

                // 3. CAMOUFLAGE LEAFY BUSH SHELL ON BACK (Signature Bush Beetle Feature)
                val bushPath = Path().apply {
                    moveTo(cx - w * 0.45f, ey + h * 0.35f)
                    cubicTo(cx - w * 0.5f, ey - 4f, cx - w * 0.15f, ey - 7f, cx, ey - 6f)
                    cubicTo(cx + w * 0.15f, ey - 7f, cx + w * 0.5f, ey - 4f, cx + w * 0.45f, ey + h * 0.35f)
                    lineTo(cx, ey + h * 0.45f)
                    close()
                }
                drawScope.drawPath(bushPath, color = shellColor)

                // Bush Leaf Tufts sticking out
                val tufts = listOf(
                    Triple(-8f, -4f, -6f),
                    Triple(-2f, -8f, 0f),
                    Triple(4f, -7f, 4f),
                    Triple(9f, -3f, 7f)
                )
                for ((tx, ty, tilt) in tufts) {
                    val leaf = Path().apply {
                        moveTo(cx + tx * dir, ey + h * 0.2f)
                        lineTo(cx + (tx + tilt) * dir, ey + ty)
                        lineTo(cx + (tx + 3f) * dir, ey + h * 0.2f)
                        close()
                    }
                    drawScope.drawPath(leaf, color = shellAccent)
                }

                // 4. BIG GLOWING WHITE BEETLE EYES
                val eyeX1 = cx + (w * 0.2f * dir)
                val eyeX2 = cx + (w * 0.38f * dir)
                val eyeY = ey + h * 0.45f

                // White Sclera
                drawScope.drawOval(
                    color = Color.White,
                    topLeft = Offset(eyeX1 - 2.5f, eyeY - 2.5f),
                    size = Size(5f, 5f)
                )
                drawScope.drawOval(
                    color = Color.White,
                    topLeft = Offset(eyeX2 - 2f, eyeY - 2f),
                    size = Size(4.2f, 4.2f)
                )

                // Black Inward Pupil
                drawScope.drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 1.3f,
                    center = Offset(eyeX1 + 0.6f * dir, eyeY)
                )
                drawScope.drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 1.1f,
                    center = Offset(eyeX2 + 0.5f * dir, eyeY)
                )
            }

            EnemyType.STONE_GIANT -> {
                // ICONIC STONE GIANT / ROCK GOLEM (Interlocking stone masonry, boulder fists, glowing rune eyes)
                val rockColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF546E7A)
                val rockDark = Color(0xFF37474F)
                val rockLight = Color(0xFF78909C)
                val runeEye = Color(0xFFFFD600)
                val stomp = sin(time * 6f) * 4f

                // Massive Stone Legs / Pillars
                drawScope.drawRoundRect(
                    color = rockDark,
                    topLeft = Offset(cx - 14f, ey + h - 18f + stomp * 0.5f),
                    size = Size(10f, 18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                drawScope.drawRoundRect(
                    color = rockColor,
                    topLeft = Offset(cx + 4f, ey + h - 18f - stomp * 0.5f),
                    size = Size(10f, 18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )

                // Torso (Heavy Stone Monolith with Rock Mortar Lines)
                drawScope.drawRoundRect(
                    color = rockColor,
                    topLeft = Offset(cx - 16f, ey + 12f),
                    size = Size(32f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Rock Brick Texture Lines
                drawScope.drawLine(
                    color = rockDark,
                    start = Offset(cx - 14f, ey + 20f),
                    end = Offset(cx + 14f, ey + 20f),
                    strokeWidth = 1.5f
                )
                drawScope.drawLine(
                    color = rockDark,
                    start = Offset(cx - 14f, ey + 28f),
                    end = Offset(cx + 14f, ey + 28f),
                    strokeWidth = 1.5f
                )
                drawScope.drawLine(
                    color = rockDark,
                    start = Offset(cx - 2f, ey + 12f),
                    end = Offset(cx - 2f, ey + 20f),
                    strokeWidth = 1.5f
                )
                drawScope.drawLine(
                    color = rockDark,
                    start = Offset(cx + 4f, ey + 20f),
                    end = Offset(cx + 4f, ey + 28f),
                    strokeWidth = 1.5f
                )

                // Massive Boulder Arms & Stone Fists
                val armSwing = sin(time * 6f) * 8f
                drawScope.drawCircle(
                    color = rockLight,
                    radius = 7f,
                    center = Offset(cx - 18f * dir, ey + 18f - armSwing * dir)
                )
                drawScope.drawCircle(
                    color = rockDark,
                    radius = 8f,
                    center = Offset(cx + 18f * dir, ey + 22f + armSwing * dir)
                )

                // Stone Head
                drawScope.drawRoundRect(
                    color = rockDark,
                    topLeft = Offset(cx - 10f, ey + 2f),
                    size = Size(20f, 14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Glowing Rune Eyes (Swordigo Stone Golem Style)
                drawScope.drawCircle(
                    color = runeEye,
                    radius = 2.5f,
                    center = Offset(cx - 3.5f + dir, ey + 8f)
                )
                drawScope.drawCircle(
                    color = runeEye,
                    radius = 2.5f,
                    center = Offset(cx + 3.5f + dir, ey + 8f)
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 1f,
                    center = Offset(cx - 3.5f + dir, ey + 8f)
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 1f,
                    center = Offset(cx + 3.5f + dir, ey + 8f)
                )
            }

            EnemyType.MOSS_SLIME -> {
                // 1. MOSS SLIME with Gelatinous Translucency, Core, Bubbles & Mushroom Cap
                val squish = if (enemy.isGrounded) sin(time * 8f) * 0.18f else -0.15f
                val sw = w * (1f + squish)
                val sh = h * (1f - squish)
                val sLeft = cx - sw * 0.5f
                val sTop = ey + (h - sh)

                val outerColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF2E7D32)
                val coreColor = if (isHurt) Color(0xFFFF8A80) else Color(0xFF76FF03)

                // Outer Translucent Slime Body
                val slimeBrush = Brush.radialGradient(
                    colors = listOf(coreColor, outerColor, Color(0xFF1B5E20)),
                    center = Offset(cx, sTop + sh * 0.5f),
                    radius = sw * 0.6f
                )
                drawScope.drawOval(
                    brush = slimeBrush,
                    topLeft = Offset(sLeft, sTop),
                    size = Size(sw, sh)
                )

                // Inner Glowing Core / Nucleus
                drawScope.drawCircle(
                    color = Color(0xCCB2FF59),
                    radius = (sh * 0.25f),
                    center = Offset(cx + 2f * dir, sTop + sh * 0.55f)
                )

                // Slime Specular Bubble Highlights
                drawScope.drawCircle(
                    color = Color(0xCCFFFFFF),
                    radius = 2.2f,
                    center = Offset(sLeft + sw * 0.3f, sTop + sh * 0.3f)
                )
                drawScope.drawCircle(
                    color = Color(0x99FFFFFF),
                    radius = 1.4f,
                    center = Offset(sLeft + sw * 0.4f, sTop + sh * 0.25f)
                )

                // Moss & Mushroom on top
                drawScope.drawOval(
                    color = Color(0xFF33691E),
                    topLeft = Offset(cx - 6f, sTop - 2f),
                    size = Size(12f, 5f)
                )
                // Red Mushroom Cap
                drawScope.drawOval(
                    color = Color(0xFFD32F2F),
                    topLeft = Offset(cx - 3f, sTop - 6f),
                    size = Size(6f, 4f)
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 0.8f,
                    center = Offset(cx - 1.5f, sTop - 4.5f)
                )

                // Menacing Slime Eyes
                val eyeX1 = cx + (3f * dir) - 4f
                val eyeX2 = cx + (3f * dir) + 3f
                val eyeY = sTop + sh * 0.4f
                drawScope.drawOval(
                    color = Color(0xFF1B0000),
                    topLeft = Offset(eyeX1 - 2.5f, eyeY - 2.5f),
                    size = Size(5f, 5f)
                )
                drawScope.drawOval(
                    color = Color(0xFF1B0000),
                    topLeft = Offset(eyeX2 - 2.5f, eyeY - 2.5f),
                    size = Size(5f, 5f)
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = 1.2f,
                    center = Offset(eyeX1 + 0.6f * dir, eyeY)
                )
                drawScope.drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = 1.2f,
                    center = Offset(eyeX2 + 0.6f * dir, eyeY)
                )
            }

            EnemyType.CAVE_BAT -> {
                // 2. CAVE BAT with Leathery Wing Membranes, Claws & Crimson Gaze
                val flap = sin(time * 18f) * 22f
                val batBody = if (isHurt) Color(0xFFFF5252) else Color(0xFF26182C)
                val batWing = if (isHurt) Color(0xFFFF8A80) else Color(0xFF4A154B)

                // Left Wing Membrane
                val leftWing = Path().apply {
                    moveTo(cx - 2f, ey + h * 0.5f)
                    cubicTo(
                        cx - 8f, ey + flap * 0.5f,
                        cx - 16f, ey - 4f + flap,
                        cx - 18f, ey + 4f + flap
                    )
                    lineTo(cx - 14f, ey + h * 0.6f + flap * 0.3f)
                    lineTo(cx - 4f, ey + h * 0.7f)
                    close()
                }
                // Right Wing Membrane
                val rightWing = Path().apply {
                    moveTo(cx + 2f, ey + h * 0.5f)
                    cubicTo(
                        cx + 8f, ey + flap * 0.5f,
                        cx + 16f, ey - 4f + flap,
                        cx + 18f, ey + 4f + flap
                    )
                    lineTo(cx + 14f, ey + h * 0.6f + flap * 0.3f)
                    lineTo(cx + 4f, ey + h * 0.7f)
                    close()
                }

                drawScope.drawPath(leftWing, color = batWing)
                drawScope.drawPath(leftWing, color = Color(0xFF1E0B24), style = Stroke(width = 1f))
                drawScope.drawPath(rightWing, color = batWing)
                drawScope.drawPath(rightWing, color = Color(0xFF1E0B24), style = Stroke(width = 1f))

                // Wing Bone Struts
                drawScope.drawLine(
                    color = Color(0xFF6A1B9A),
                    start = Offset(cx - 2f, ey + h * 0.5f),
                    end = Offset(cx - 18f, ey + 4f + flap),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color(0xFF6A1B9A),
                    start = Offset(cx + 2f, ey + h * 0.5f),
                    end = Offset(cx + 18f, ey + 4f + flap),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )

                // Furry Bat Body & Pointed Ears
                drawScope.drawOval(
                    color = batBody,
                    topLeft = Offset(cx - 5f, ey + 3f),
                    size = Size(10f, 16f)
                )
                // Bat Ears
                val earL = Path().apply {
                    moveTo(cx - 4f, ey + 5f); lineTo(cx - 6f, ey - 2f); lineTo(cx - 2f, ey + 3f); close()
                }
                val earR = Path().apply {
                    moveTo(cx + 4f, ey + 5f); lineTo(cx + 6f, ey - 2f); lineTo(cx + 2f, ey + 3f); close()
                }
                drawScope.drawPath(earL, color = batBody)
                drawScope.drawPath(earR, color = batBody)

                // Glowing Crimson Eyes & Fangs
                drawScope.drawCircle(
                    color = Color(0xFFFF1744),
                    radius = 1.8f,
                    center = Offset(cx - 2.5f, ey + 8f)
                )
                drawScope.drawCircle(
                    color = Color(0xFFFF1744),
                    radius = 1.8f,
                    center = Offset(cx + 2.5f, ey + 8f)
                )
                // White Fangs
                drawScope.drawRect(color = Color.White, topLeft = Offset(cx - 1.8f, ey + 13f), size = Size(1f, 2f))
                drawScope.drawRect(color = Color.White, topLeft = Offset(cx + 0.8f, ey + 13f), size = Size(1f, 2f))
            }

            EnemyType.SHADOW_GOBLIN -> {
                // 3. SHADOW GOBLIN with Armor, Wicked Scimitar & Pointed Ears
                val gobSkin = if (isHurt) Color(0xFFFF5252) else Color(0xFF33691E)
                val armorLeather = Color(0xFF4E342E)
                val legCycle = sin(time * 12f) * 6f

                // Goblin Legs
                drawScope.drawRect(
                    color = Color(0xFF2E1C0C),
                    topLeft = Offset(cx - 5f - legCycle, ey + h - 14f),
                    size = Size(4.5f, 14f)
                )
                drawScope.drawRect(
                    color = Color(0xFF2E1C0C),
                    topLeft = Offset(cx + 1f + legCycle, ey + h - 14f),
                    size = Size(4.5f, 14f)
                )

                // Leather Studded Cuirass
                drawScope.drawRoundRect(
                    color = armorLeather,
                    topLeft = Offset(cx - 7f, ey + 14f),
                    size = Size(14f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
                // Spiked Shoulder Guard
                drawScope.drawCircle(
                    color = Color(0xFF37474F),
                    radius = 3.5f,
                    center = Offset(cx - 6f * dir, ey + 15f)
                )

                // Goblin Head with Pointed Ears
                drawScope.drawOval(
                    color = gobSkin,
                    topLeft = Offset(cx - 6f, ey + 2f),
                    size = Size(12f, 14f)
                )
                // Pointed Ears
                val earPath = Path().apply {
                    moveTo(cx - 4f * dir, ey + 8f)
                    lineTo(cx - 12f * dir, ey + 2f)
                    lineTo(cx - 4f * dir, ey + 12f)
                    close()
                }
                drawScope.drawPath(earPath, color = gobSkin)

                // Sinister Yellow Eyes & Red Warpaint
                val gobEyeX = cx + 2.5f * dir
                drawScope.drawOval(
                    color = Color(0xFFFFEA00),
                    topLeft = Offset(gobEyeX - 2f, ey + 6f),
                    size = Size(4f, 3f)
                )
                drawScope.drawCircle(
                    color = Color(0xFFD50000),
                    radius = 0.9f,
                    center = Offset(gobEyeX + 0.5f * dir, ey + 7.5f)
                )
                // Warpaint Stripe
                drawScope.drawLine(
                    color = Color(0xFFD50000),
                    start = Offset(cx, ey + 6f),
                    end = Offset(cx + 5f * dir, ey + 10f),
                    strokeWidth = 1.2f
                )

                // Wicked Curved Scimitar Dagger
                val daggerProgress = if (enemy.aiState == EnemyAIState.ATTACK) sin(time * 16f).coerceAtLeast(0f) else 0f
                val daggerX = cx + 8f * dir + (daggerProgress * 12f * dir)
                val daggerY = ey + 18f - (daggerProgress * 4f)

                val bladePath = Path().apply {
                    moveTo(daggerX - 2f * dir, daggerY)
                    cubicTo(
                        daggerX + 6f * dir, daggerY - 4f,
                        daggerX + 12f * dir, daggerY + 2f,
                        daggerX + 16f * dir, daggerY - 2f
                    )
                    lineTo(daggerX + 4f * dir, daggerY + 4f)
                    close()
                }
                drawScope.drawPath(bladePath, color = Color(0xFFECEFF1))
                drawScope.drawPath(bladePath, color = Color(0xFF607D8B), style = Stroke(width = 1f))
            }

            EnemyType.SKELETON_KNIGHT -> {
                // 4. SKELETON KNIGHT BOSS with Gothic Black Iron Armor, Horned Helm, Burning Eyes & Greatsword
                val gothicArmor = if (isHurt) Color(0xFFFF5252) else Color(0xFF263238)
                val goldFiligree = Color(0xFFFFD700)
                val flameEye = Color(0xFF00E5FF)

                // Tattered Boss Cape (Blood Crimson with ragged fringe)
                val capeSway = sin(time * 6f) * 6f
                val bossCape = Path().apply {
                    moveTo(cx - 10f * dir, ey + 14f)
                    cubicTo(
                        cx - 20f * dir, ey + 28f + capeSway,
                        cx - 24f * dir - capeSway, ey + h - 6f,
                        cx - 18f * dir, ey + h
                    )
                    lineTo(cx + 4f * dir, ey + 20f)
                    close()
                }
                drawScope.drawPath(bossCape, color = Color(0xFF880E4F))

                // Heavy Gothic Greaves (Legs)
                drawScope.drawRoundRect(
                    color = gothicArmor,
                    topLeft = Offset(cx - 9f, ey + h - 22f),
                    size = Size(8f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
                drawScope.drawRoundRect(
                    color = gothicArmor,
                    topLeft = Offset(cx + 1f, ey + h - 22f),
                    size = Size(8f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )

                // Massive Plate Chestplate & Spiked Pauldrons
                drawScope.drawRoundRect(
                    color = gothicArmor,
                    topLeft = Offset(cx - 11f, ey + 14f),
                    size = Size(22f, 24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Gold Royal Trim on Chest
                drawScope.drawRect(
                    color = goldFiligree,
                    topLeft = Offset(cx - 10f, ey + 24f),
                    size = Size(20f, 2f)
                )

                // Horned Skull Greathelm
                drawScope.drawRoundRect(
                    color = gothicArmor,
                    topLeft = Offset(cx - 9f, ey + 2f),
                    size = Size(18f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Spiked Horns
                val hornL = Path().apply {
                    moveTo(cx - 8f, ey + 6f); lineTo(cx - 16f, ey - 4f); lineTo(cx - 5f, ey + 3f); close()
                }
                val hornR = Path().apply {
                    moveTo(cx + 8f, ey + 6f); lineTo(cx + 16f, ey - 4f); lineTo(cx + 5f, ey + 3f); close()
                }
                drawScope.drawPath(hornL, color = Color(0xFF455A64))
                drawScope.drawPath(hornR, color = Color(0xFF455A64))

                // Burning Ethereal Cyan Eye-Flames inside Visor Slit
                drawScope.drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(cx - 7f, ey + 8f),
                    size = Size(14f, 4f)
                )
                drawScope.drawCircle(
                    color = flameEye,
                    radius = 2.2f + sin(time * 10f) * 0.5f,
                    center = Offset(cx - 2.5f + dir, ey + 10f)
                )
                drawScope.drawCircle(
                    color = flameEye,
                    radius = 2.2f + sin(time * 10f) * 0.5f,
                    center = Offset(cx + 3.5f + dir, ey + 10f)
                )

                // Spiked Kite Shield & Flaming Greatsword
                if (enemy.aiState == EnemyAIState.ATTACK) {
                    // Massive Sweeping Greatsword
                    val sAngle = sin(time * 12f) * 45f
                    val rad = (sAngle + (if (facingRight) 20f else -20f)) * PI.toFloat() / 180f
                    val bladeTip = Offset(cx + cos(rad) * 48f * dir, ey + 20f + sin(rad) * 48f)

                    drawScope.drawLine(
                        color = Color(0xFFFF3D00),
                        start = Offset(cx, ey + 20f),
                        end = bladeTip,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawScope.drawLine(
                        color = Color(0xFFFFEA00),
                        start = Offset(cx, ey + 20f),
                        end = bladeTip,
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                } else {
                    // Spiked Tower Shield in front
                    val shieldX = cx + 8f * dir - 6f
                    drawScope.drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(shieldX, ey + 16f),
                        size = Size(12f, 28f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                    )
                    drawScope.drawRect(
                        color = goldFiligree,
                        topLeft = Offset(shieldX + 1f, ey + 18f),
                        size = Size(10f, 24f),
                        style = Stroke(width = 1.2f)
                    )
                }

                // Boss Header HP Bar
                val hpRatio = (enemy.health.toFloat() / enemy.maxHealth).coerceIn(0f, 1f)
                val barW = 56f
                val barX = cx - barW * 0.5f
                val barY = ey - 16f
                drawScope.drawRoundRect(
                    color = Color(0xCC000000),
                    topLeft = Offset(barX - 2f, barY - 2f),
                    size = Size(barW + 4f, 9f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFFF1744),
                    topLeft = Offset(barX, barY),
                    size = Size(barW * hpRatio, 5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            EnemyType.CAVE_CRAWLER -> {
                // CAVE BEAST (Subterranean razor-clawed quadruped predator)
                val beastColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF1A1A24)
                val spineColor = Color(0xFF7C4DFF)
                val legCycle = sin(time * 16f) * 6f

                // 4 Muscular Quadruped Claws
                drawScope.drawLine(
                    color = Color(0xFF101018),
                    start = Offset(cx - 10f * dir, ey + h * 0.6f),
                    end = Offset(cx - 14f * dir - legCycle, ey + h),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color(0xFF101018),
                    start = Offset(cx - 2f * dir, ey + h * 0.6f),
                    end = Offset(cx - 4f * dir + legCycle, ey + h),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color(0xFF101018),
                    start = Offset(cx + 6f * dir, ey + h * 0.6f),
                    end = Offset(cx + 4f * dir - legCycle, ey + h),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = Color(0xFF101018),
                    start = Offset(cx + 12f * dir, ey + h * 0.6f),
                    end = Offset(cx + 16f * dir + legCycle, ey + h),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )

                // Razor Paw Claws
                for (fx in listOf(cx - 14f * dir - legCycle, cx - 4f * dir + legCycle, cx + 4f * dir - legCycle, cx + 16f * dir + legCycle)) {
                    drawScope.drawCircle(color = Color(0xFFECEFF1), radius = 1.6f, center = Offset(fx, ey + h))
                }

                // Muscular Spiked Body
                drawScope.drawOval(
                    color = beastColor,
                    topLeft = Offset(cx - w * 0.45f, ey + h * 0.25f),
                    size = Size(w * 0.9f, h * 0.55f)
                )

                // Dorsal Spikes
                for (i in -2..2) {
                    val spX = cx + i * 5f * dir
                    val spY = ey + h * 0.25f
                    val spikePath = Path().apply {
                        moveTo(spX - 2f, spY + 2f)
                        lineTo(spX, spY - 6f - kotlin.math.abs(i) * 2f)
                        lineTo(spX + 2f, spY + 2f)
                        close()
                    }
                    drawScope.drawPath(spikePath, color = spineColor)
                }

                // Predatory Beast Head & Jaws
                val headX = cx + 10f * dir
                drawScope.drawOval(
                    color = beastColor,
                    topLeft = Offset(headX - 6f, ey + h * 0.15f),
                    size = Size(14f, 13f)
                )

                // Glowing Crimson Eyes
                val eyeX = headX + 3f * dir
                drawScope.drawCircle(color = Color(0xFFFF1744), radius = 1.8f, center = Offset(eyeX, ey + h * 0.25f))
                drawScope.drawCircle(color = Color(0xFFFF5252), radius = 1.2f, center = Offset(eyeX + 3f * dir, ey + h * 0.22f))

                // Fangs
                drawScope.drawLine(
                    color = Color.White,
                    start = Offset(headX + 4f * dir, ey + h * 0.42f),
                    end = Offset(headX + 5f * dir, ey + h * 0.52f),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )

                // Tail with Spiked Tip
                val tailPath = Path().apply {
                    moveTo(cx - 12f * dir, ey + h * 0.4f)
                    cubicTo(
                        cx - 18f * dir, ey + h * 0.3f,
                        cx - 22f * dir, ey + h * 0.15f,
                        cx - 24f * dir, ey + h * 0.05f
                    )
                }
                drawScope.drawPath(tailPath, color = beastColor, style = Stroke(width = 3f))
                drawScope.drawCircle(color = spineColor, radius = 2.5f, center = Offset(cx - 24f * dir, ey + h * 0.05f))
            }

            EnemyType.STONE_GIANT -> {
                // STONE GIANT (Large Guardian with ancient moss and rune fist)
                val stoneColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF455A64)
                val darkStone = Color(0xFF263238)
                val runeGlow = Color(0xFF00E5FF)

                // Massive Stone Legs
                val legCycle = sin(time * 8f) * 4f
                drawScope.drawRoundRect(
                    color = darkStone,
                    topLeft = Offset(cx - 16f - legCycle, ey + h - 20f),
                    size = Size(12f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                drawScope.drawRoundRect(
                    color = darkStone,
                    topLeft = Offset(cx + 4f + legCycle, ey + h - 20f),
                    size = Size(12f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )

                // Massive Chiseled Torso
                drawScope.drawRoundRect(
                    color = stoneColor,
                    topLeft = Offset(cx - 18f, ey + 14f),
                    size = Size(36f, 28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )

                // Ancient Rune Carving on Chest
                drawScope.drawLine(
                    color = runeGlow,
                    start = Offset(cx - 8f, ey + 24f),
                    end = Offset(cx + 8f, ey + 24f),
                    strokeWidth = 2f
                )
                drawScope.drawLine(
                    color = runeGlow,
                    start = Offset(cx, ey + 18f),
                    end = Offset(cx, ey + 34f),
                    strokeWidth = 2f
                )
                drawScope.drawCircle(color = Color(0xFFE0F7FA), radius = 2f, center = Offset(cx, ey + 26f))

                // Moss Patches
                drawScope.drawOval(color = Color(0x9933691E), topLeft = Offset(cx - 14f, ey + 16f), size = Size(10f, 6f))
                drawScope.drawOval(color = Color(0x9933691E), topLeft = Offset(cx + 4f, ey + 30f), size = Size(12f, 7f))

                // Stone Head
                drawScope.drawRoundRect(
                    color = darkStone,
                    topLeft = Offset(cx - 10f, ey + 2f),
                    size = Size(20f, 16f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
                // Single Glowing Cyclopean Rune Eye
                drawScope.drawCircle(color = runeGlow, radius = 3.5f, center = Offset(cx + 2f * dir, ey + 10f))
                drawScope.drawCircle(color = Color.White, radius = 1.2f, center = Offset(cx + 2f * dir, ey + 10f))

                // Massive Boulder Fist
                val fistProgress = if (enemy.aiState == EnemyAIState.ATTACK) sin(time * 12f).coerceAtLeast(0f) else 0f
                val fistX = cx + 16f * dir + (fistProgress * 14f * dir)
                val fistY = ey + 22f + (fistProgress * 8f)
                drawScope.drawCircle(color = stoneColor, radius = 10f, center = Offset(fistX, fistY))
                drawScope.drawCircle(color = darkStone, radius = 10f, center = Offset(fistX, fistY), style = Stroke(width = 2f))
                drawScope.drawCircle(color = runeGlow, radius = 3f, center = Offset(fistX, fistY))
            }

            EnemyType.RUIN_COLOSSUS -> {
                // THE FIRST MINI-BOSS: GORGAROTH THE RUIN COLOSSUS
                val isStaggered = enemy.aiState == EnemyAIState.STAGGERED
                val isEnraged = enemy.isEnraged
                val bossColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF1E293B)
                val darkIron = Color(0xFF0F172A)
                val coreColor = if (isEnraged) Color(0xFFFF3D00) else Color(0xFF00E5FF)
                val coreInner = if (isEnraged) Color(0xFFFFEA00) else Color(0xFFE0F7FA)

                // Enraged Ambient Flame Aura
                if (isEnraged) {
                    for (i in 0..6) {
                        val fx = cx + sin(time * 8f + i) * 28f
                        val fy = ey + h * 0.5f - ((time * 40f + i * 15f) % (h * 0.8f))
                        drawScope.drawCircle(
                            color = Color(0x66FF5722),
                            radius = 4f + sin(time * 10f + i) * 2f,
                            center = Offset(fx, fy)
                        )
                    }
                }

                // Massive Basalt Armored Legs
                val legCycle = sin(time * 8f) * 6f
                drawScope.drawRoundRect(
                    color = darkIron,
                    topLeft = Offset(cx - 24f - legCycle, ey + h - 28f),
                    size = Size(18f, 28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawScope.drawRoundRect(
                    color = darkIron,
                    topLeft = Offset(cx + 6f + legCycle, ey + h - 28f),
                    size = Size(18f, 28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )

                // Massive Forged Pauldrons & Torso
                drawScope.drawRoundRect(
                    color = bossColor,
                    topLeft = Offset(cx - 26f, ey + 20f),
                    size = Size(52f, 38f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFFFD700),
                    topLeft = Offset(cx - 26f, ey + 20f),
                    size = Size(52f, 38f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                    style = Stroke(width = 2f)
                )

                // Spiked Shoulder Pauldrons
                val pauldronL = Path().apply {
                    moveTo(cx - 24f, ey + 24f); lineTo(cx - 38f, ey + 10f); lineTo(cx - 22f, ey + 36f); close()
                }
                val pauldronR = Path().apply {
                    moveTo(cx + 24f, ey + 24f); lineTo(cx + 38f, ey + 10f); lineTo(cx + 22f, ey + 36f); close()
                }
                drawScope.drawPath(pauldronL, color = darkIron)
                drawScope.drawPath(pauldronR, color = darkIron)

                // Glowing Ancient Arcane Chest Core
                val corePulse = (sin(time * 6f) * 3f)
                drawScope.drawCircle(color = coreColor.copy(alpha = 0.5f), radius = 12f + corePulse, center = Offset(cx, ey + 36f))
                drawScope.drawCircle(color = coreColor, radius = 9f, center = Offset(cx, ey + 36f))
                drawScope.drawCircle(color = coreInner, radius = 5f, center = Offset(cx, ey + 36f))

                // Colossus Helm & Spiked Crown Horns
                drawScope.drawRoundRect(
                    color = darkIron,
                    topLeft = Offset(cx - 16f, ey + 2f),
                    size = Size(32f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                )
                // Horns
                val hornLeft = Path().apply {
                    moveTo(cx - 12f, ey + 6f); cubicTo(cx - 22f, ey - 2f, cx - 28f, ey - 14f, cx - 26f, ey - 20f); lineTo(cx - 8f, ey + 2f); close()
                }
                val hornRight = Path().apply {
                    moveTo(cx + 12f, ey + 6f); cubicTo(cx + 22f, ey - 2f, cx + 28f, ey - 14f, cx + 26f, ey - 20f); lineTo(cx + 8f, ey + 2f); close()
                }
                drawScope.drawPath(hornLeft, color = darkIron)
                drawScope.drawPath(hornRight, color = darkIron)

                // Visor Slit Eye
                drawScope.drawRect(color = Color(0xFF020617), topLeft = Offset(cx - 10f, ey + 10f), size = Size(20f, 5f))
                drawScope.drawCircle(color = coreColor, radius = 3f, center = Offset(cx + 3f * dir, ey + 12.5f))
                drawScope.drawCircle(color = Color.White, radius = 1.2f, center = Offset(cx + 3f * dir, ey + 12.5f))

                // Boss Weapon: Gigantic Runic War Cleaver
                val isAttacking = enemy.aiState == EnemyAIState.ATTACK
                val swordAngle = if (isAttacking) {
                    if (enemy.bossAttackPattern == 0) sin(time * 14f) * 60f else sin(time * 12f) * 45f
                } else {
                    -25f
                }
                val swordRad = swordAngle * PI.toFloat() / 180f
                val hiltPos = Offset(cx + 18f * dir, ey + 30f)
                val tipPos = Offset(hiltPos.x + cos(swordRad) * 64f * dir, hiltPos.y + sin(swordRad) * 64f)

                drawScope.drawLine(
                    color = darkIron,
                    start = hiltPos,
                    end = tipPos,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
                drawScope.drawLine(
                    color = coreColor,
                    start = hiltPos,
                    end = tipPos,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Staggered Dizzy Stars Indicator
                if (isStaggered) {
                    for (i in 0..4) {
                        val starAngle = time * 4f + i * (PI.toFloat() * 2f / 5f)
                        val sx = cx + cos(starAngle) * 22f
                        val sy = ey - 8f + sin(starAngle) * 8f
                        drawScope.drawCircle(color = Color(0xFFFFD700), radius = 3f, center = Offset(sx, sy))
                        drawScope.drawCircle(color = Color.White, radius = 1.2f, center = Offset(sx, sy))
                    }
                }
            }

            EnemyType.DREADFANG_WOLF -> {
                // OPTIONAL BOSS: DREADFANG SHADOW WOLF
                val wolfColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF181824)
                val furMane = Color(0xFF311B92)
                val eyeGlow = Color(0xFFFF3D00)
                val runCycle = sin(time * 16f) * 8f

                // Muscular wolf legs
                drawScope.drawLine(color = wolfColor, start = Offset(cx - 14f * dir, ey + h * 0.5f), end = Offset(cx - 18f * dir - runCycle, ey + h), strokeWidth = 4f, cap = StrokeCap.Round)
                drawScope.drawLine(color = wolfColor, start = Offset(cx - 4f * dir, ey + h * 0.5f), end = Offset(cx - 6f * dir + runCycle, ey + h), strokeWidth = 4f, cap = StrokeCap.Round)
                drawScope.drawLine(color = wolfColor, start = Offset(cx + 8f * dir, ey + h * 0.5f), end = Offset(cx + 6f * dir - runCycle, ey + h), strokeWidth = 4f, cap = StrokeCap.Round)
                drawScope.drawLine(color = wolfColor, start = Offset(cx + 18f * dir, ey + h * 0.5f), end = Offset(cx + 20f * dir + runCycle, ey + h), strokeWidth = 4f, cap = StrokeCap.Round)

                // Wolf Torso
                drawScope.drawOval(color = wolfColor, topLeft = Offset(cx - w * 0.45f, ey + h * 0.2f), size = Size(w * 0.9f, h * 0.6f))

                // Shadow Mane Spikes
                for (i in -2..2) {
                    val spX = cx + i * 6f * dir
                    val spY = ey + h * 0.2f
                    val manePath = Path().apply {
                        moveTo(spX - 3f, spY + 4f); lineTo(spX, spY - 8f); lineTo(spX + 3f, spY + 4f); close()
                    }
                    drawScope.drawPath(manePath, color = furMane)
                }

                // Wolf Snout & Glowing Eyes
                val headX = cx + 14f * dir
                drawScope.drawOval(color = wolfColor, topLeft = Offset(headX - 8f, ey + h * 0.1f), size = Size(18f, 16f))
                drawScope.drawCircle(color = eyeGlow, radius = 2.5f, center = Offset(headX + 4f * dir, ey + h * 0.22f))
                drawScope.drawCircle(color = Color.White, radius = 1f, center = Offset(headX + 4f * dir, ey + h * 0.22f))

                // Pointed Ears
                val earL = Path().apply {
                    moveTo(headX - 2f * dir, ey + h * 0.1f); lineTo(headX - 6f * dir, ey - 6f); lineTo(headX + 2f * dir, ey + 2f); close()
                }
                drawScope.drawPath(earL, color = wolfColor)

                // Long Bushy Shadow Tail
                val tailPath = Path().apply {
                    moveTo(cx - 16f * dir, ey + h * 0.4f)
                    cubicTo(cx - 24f * dir, ey + h * 0.2f, cx - 28f * dir, ey + h * 0.5f, cx - 32f * dir, ey + h * 0.3f)
                }
                drawScope.drawPath(tailPath, color = furMane, style = Stroke(width = 4f, cap = StrokeCap.Round))
            }

            EnemyType.FROST_WURM -> {
                // OPTIONAL BOSS: GLACIAL FROST WURM
                val iceBody = if (isHurt) Color(0xFFFF5252) else Color(0xFF006064)
                val iceRidge = Color(0xFF80DEEA)
                val glowEye = Color(0xFF00E5FF)
                val slither = sin(time * 10f) * 6f

                // Segmented Glacial Chitin Rings
                for (i in 0..4) {
                    val segX = cx - (i * 9f * dir)
                    val segY = ey + h * 0.4f + sin(time * 8f + i) * 5f
                    drawScope.drawCircle(color = iceBody, radius = (16f - i * 2f).coerceAtLeast(6f), center = Offset(segX, segY))
                    drawScope.drawCircle(color = iceRidge, radius = (16f - i * 2f).coerceAtLeast(6f), center = Offset(segX, segY), style = Stroke(width = 1.8f))
                }

                // Wurm Mandibles & Head
                val headX = cx + 12f * dir
                val headY = ey + h * 0.35f + slither
                drawScope.drawCircle(color = Color(0xFF004D40), radius = 18f, center = Offset(headX, headY))
                drawScope.drawCircle(color = glowEye, radius = 3.5f, center = Offset(headX + 5f * dir, headY - 4f))
                drawScope.drawCircle(color = glowEye, radius = 3.5f, center = Offset(headX + 5f * dir, headY + 4f))
                drawScope.drawCircle(color = Color.White, radius = 1.2f, center = Offset(headX + 5f * dir, headY - 4f))
                drawScope.drawCircle(color = Color.White, radius = 1.2f, center = Offset(headX + 5f * dir, headY + 4f))

                // Ice Spikes on Head
                for (a in -1..1) {
                    val spike = Path().apply {
                        moveTo(headX - 6f, headY + a * 8f)
                        lineTo(headX + 18f * dir, headY + a * 14f)
                        lineTo(headX, headY + a * 12f)
                        close()
                    }
                    drawScope.drawPath(spike, color = iceRidge)
                }
            }

            EnemyType.SHADOW_LICH -> {
                // OPTIONAL BOSS: MALAKOR THE SHADOW LICH
                val robeColor = if (isHurt) Color(0xFFFF5252) else Color(0xFF311B92)
                val skullBone = Color(0xFFECEFF1)
                val soulFlame = Color(0xFFE040FB)
                val floatBob = sin(time * 5f) * 7f

                // Floating Tattered Robes
                val robePath = Path().apply {
                    moveTo(cx - 14f, ey + 18f + floatBob)
                    cubicTo(cx - 18f, ey + h - 4f + floatBob, cx - 10f, ey + h + 8f + floatBob, cx, ey + h + 4f + floatBob)
                    cubicTo(cx + 10f, ey + h + 8f + floatBob, cx + 18f, ey + h - 4f + floatBob, cx + 14f, ey + 18f + floatBob)
                    close()
                }
                drawScope.drawPath(robePath, color = robeColor)

                // Levitating Skull & Horned Necro-Crown
                drawScope.drawCircle(color = skullBone, radius = 11f, center = Offset(cx, ey + 10f + floatBob))
                drawScope.drawCircle(color = Color(0xFF0F172A), radius = 2.5f, center = Offset(cx - 4f + dir, ey + 10f + floatBob))
                drawScope.drawCircle(color = Color(0xFF0F172A), radius = 2.5f, center = Offset(cx + 4f + dir, ey + 10f + floatBob))
                drawScope.drawCircle(color = soulFlame, radius = 1.8f, center = Offset(cx - 4f + dir, ey + 10f + floatBob))
                drawScope.drawCircle(color = soulFlame, radius = 1.8f, center = Offset(cx + 4f + dir, ey + 10f + floatBob))

                // Arcane Necro-Staff
                val staffX = cx + 18f * dir
                val staffY = ey + 12f + floatBob
                drawScope.drawLine(color = Color(0xFF4E342E), start = Offset(staffX, staffY - 14f), end = Offset(staffX, staffY + 36f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawScope.drawCircle(color = soulFlame, radius = 6f + sin(time * 8f) * 2f, center = Offset(staffX, staffY - 14f))
                drawScope.drawCircle(color = Color.White, radius = 2f, center = Offset(staffX, staffY - 14f))
            }
        }

        // Mini HP Bar for normal enemies
        if (enemy.health < enemy.maxHealth && !enemy.isBoss) {
            val hpRatio = (enemy.health.toFloat() / enemy.maxHealth).coerceIn(0f, 1f)
            val barW = 28f
            val barX = cx - barW * 0.5f
            val barY = ey - 10f
            drawScope.drawRect(color = Color(0xAA000000), topLeft = Offset(barX - 1f, barY - 1f), size = Size(barW + 2f, 6f))
            drawScope.drawRect(color = Color(0xFF00E676), topLeft = Offset(barX, barY), size = Size(barW * hpRatio, 4f))
        }
    }
}
