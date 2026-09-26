package com.example.game.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.effects.StatusEffect
import com.example.game.effects.StatusEffectType
import com.example.game.entities.Player
import com.example.game.entities.PlayerState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object PlayerCharacterRenderer {

    fun draw(
        drawScope: DrawScope,
        player: Player,
        camX: Float,
        camY: Float,
        time: Float
    ) {
        val px = player.pos.x - camX
        val py = player.pos.y - camY

        // Invulnerability flash
        if (player.isInvulnerable && ((time * 20f).toInt() % 2 == 0)) {
            return
        }

        val facingRight = player.facingRight
        val dir = if (facingRight) 1f else -1f
        val w = player.width
        val h = player.height
        val cx = px + w * 0.5f

        val isDead = player.state == PlayerState.DEAD
        val isHurt = player.state == PlayerState.HURT
        val isAttacking = player.isAttacking()
        val isJumping = player.state == PlayerState.JUMP
        val isFalling = player.state == PlayerState.FALL
        val isRunning = player.state == PlayerState.RUN
        val isClimbing = player.state == PlayerState.CLIMB_LADDER

        // 0. STATUS EFFECT BACKGROUND AURAS & SHADERS
        drawStatusEffectAuras(drawScope, player, cx, py + h * 0.5f, dir, isRunning, time)

        // 1. DYNAMIC FLOWING SAGE CAPE (Behind character)
        val capeWave = sin(time * 12f) * 4f
        val capeTrailing = when {
            isRunning -> -18f * dir
            isJumping -> -14f * dir
            isFalling -> -8f * dir
            else -> -8f * dir
        }
        val capeLift = when {
            isFalling -> -10f
            isJumping -> 4f
            else -> 0f
        }

        val capePath = Path().apply {
            moveTo(cx - 3f * dir, py + 12f)
            cubicTo(
                cx + capeTrailing * 0.5f, py + 22f + capeLift * 0.5f,
                cx + capeTrailing + capeWave * dir, py + h * 0.7f + capeLift,
                cx + capeTrailing + capeWave * 1.5f * dir, py + h - 2f + capeLift
            )
            lineTo(cx + capeTrailing * 0.6f, py + h - 4f)
            lineTo(cx + 3f * dir, py + 16f)
            close()
        }

        // Emerald / Forest Cape Gradient with Gold Trim
        val capeBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF0E3813)),
            start = Offset(cx, py + 12f),
            end = Offset(cx + capeTrailing, py + h)
        )
        drawScope.drawPath(capePath, brush = capeBrush)
        drawScope.drawPath(capePath, color = Color(0xFFFFD700), style = Stroke(width = 1.2f, cap = StrokeCap.Round))

        // 2. HUMAN LEGS & LEATHER BOOTS
        val runCycle = if (isRunning) sin(time * 15f) else 0f
        val legLeftOffset = if (isRunning) runCycle * 8f else 0f
        val legRightOffset = if (isRunning) -runCycle * 8f else 0f

        val bootColor = Color(0xFF4E342E)
        val bootSoleColor = Color(0xFF2C1810)
        val pantsColor = Color(0xFF2E7D32) // Green adventure trousers
        val strapColor = Color(0xFFE65100)

        // Left (Back) Leg
        val backLegX = cx - 5f * dir + (if (facingRight) legLeftOffset else legRightOffset)
        val backLegTopY = py + h - 20f
        drawScope.drawRoundRect(
            color = Color(0xFF1B5E20),
            topLeft = Offset(backLegX - 3f, backLegTopY),
            size = Size(6f, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        // Back Boot
        drawScope.drawRoundRect(
            color = bootColor,
            topLeft = Offset(backLegX - 3.5f, backLegTopY + 9f),
            size = Size(8f * dir.coerceAtLeast(0.8f), 11f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        // Sole
        drawScope.drawRect(
            color = bootSoleColor,
            topLeft = Offset(backLegX - 4f, backLegTopY + 18f),
            size = Size(9f, 3f)
        )

        // Right (Front) Leg
        val frontLegX = cx + 3f * dir + (if (facingRight) legRightOffset else legLeftOffset)
        val frontLegTopY = py + h - 20f
        drawScope.drawRoundRect(
            color = pantsColor,
            topLeft = Offset(frontLegX - 3f, frontLegTopY),
            size = Size(6.5f, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        // Front Boot & Straps
        drawScope.drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(frontLegX - 3.5f, frontLegTopY + 9f),
            size = Size(8.5f * dir.coerceAtLeast(0.8f), 11f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        drawScope.drawRect(
            color = strapColor,
            topLeft = Offset(frontLegX - 3.5f, frontLegTopY + 12f),
            size = Size(7f, 2f)
        )
        drawScope.drawRect(
            color = bootSoleColor,
            topLeft = Offset(frontLegX - 4f, frontLegTopY + 18f),
            size = Size(9f, 3f)
        )

        // 3. ATHLETIC TORSO & ADVENTURER TUNIC (Emerald Green with Orange/Red Flame Tabard)
        val breathOffset = if (!isRunning && !isAttacking) sin(time * 3.5f) * 1.2f else 0f
        val torsoY = py + 14f + breathOffset
        val torsoHeight = 18f

        // Emerald Green Tunic with Shading
        val tunicBrush = Brush.horizontalGradient(
            colors = if (facingRight) listOf(Color(0xFF43A047), Color(0xFF2E7D32)) else listOf(Color(0xFF2E7D32), Color(0xFF43A047)),
            startX = cx - 9f,
            endX = cx + 9f
        )
        drawScope.drawRoundRect(
            brush = tunicBrush,
            topLeft = Offset(cx - 7.5f, torsoY),
            size = Size(15f, torsoHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )

        // Orange-Red Flame Vest / Tabard
        drawScope.drawRoundRect(
            color = Color(0xFFE64A19),
            topLeft = Offset(cx - 5.5f, torsoY + 1f),
            size = Size(11f, torsoHeight - 4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        // Golden Flame Emblem on Chest/Back
        drawScope.drawOval(
            color = Color(0xFFFFD700),
            topLeft = Offset(cx - 2f * dir - 2f, torsoY + 4f),
            size = Size(4f, 6f)
        )
        drawScope.drawCircle(
            color = Color(0xFFFFEB3B),
            radius = 1.2f,
            center = Offset(cx - 2f * dir, torsoY + 6f)
        )

        // Gold Trim Collar & V-Neck
        val collarPath = Path().apply {
            moveTo(cx - 4f, torsoY)
            lineTo(cx, torsoY + 5f)
            lineTo(cx + 4f, torsoY)
        }
        drawScope.drawPath(collarPath, color = Color(0xFFFFD700), style = Stroke(width = 1.5f, cap = StrokeCap.Round))

        // Leather Cross-Belt & Buckle
        val beltPath = Path().apply {
            moveTo(cx - 6f * dir, torsoY + 2f)
            lineTo(cx + 5f * dir, torsoY + torsoHeight - 3f)
        }
        drawScope.drawPath(beltPath, color = Color(0xFF3E2723), style = Stroke(width = 2.8f, cap = StrokeCap.Round))
        // Orange/Brown Waist Belt
        drawScope.drawRect(
            color = Color(0xFFD84315),
            topLeft = Offset(cx - 7.5f, torsoY + torsoHeight - 4f),
            size = Size(15f, 4f)
        )
        // Polished Gold Buckle
        drawScope.drawRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(cx - 2.5f, torsoY + torsoHeight - 4.5f),
            size = Size(5f, 5f)
        )
        drawScope.drawRect(
            color = Color(0xFF263238),
            topLeft = Offset(cx - 1.5f, torsoY + torsoHeight - 3.5f),
            size = Size(3f, 3f)
        )

        // Adventurer's Side Satchel
        drawScope.drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(cx - 8f * dir - 2f, torsoY + torsoHeight - 3f),
            size = Size(6f, 6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
        )

        // 4. NECK & DETAILED HUMAN HEAD / FACE
        val headY = py + 2f + breathOffset * 0.7f

        // Neck
        drawScope.drawRect(
            color = Color(0xFFFFCC80),
            topLeft = Offset(cx - 2.5f, headY + 11f),
            size = Size(5f, 4f)
        )

        // Human Head
        val skinBrush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80), Color(0xFFFFA726)),
            center = Offset(cx + 1f * dir, headY + 6f),
            radius = 9f
        )
        drawScope.drawOval(
            brush = skinBrush,
            topLeft = Offset(cx - 5.5f, headY + 2f),
            size = Size(11f, 12f)
        )

        // SIGNATURE SPIKY VIBRANT COBALT-BLUE HAIR (Hero Ponytail)
        val hairPrimary = Color(0xFF1E88E5)
        val hairHighlight = Color(0xFF00E5FF)
        val hairShadow = Color(0xFF0D47A1)

        // Upward Spiky High Ponytail
        val hairSpikesPath = Path().apply {
            moveTo(cx - 5f * dir, headY + 4f)
            // Spike 1 (Top high backward spike)
            lineTo(cx - 12f * dir, headY - 8f - capeWave * 0.4f)
            lineTo(cx - 8f * dir, headY - 2f)
            // Spike 2 (Middle majestic spike)
            lineTo(cx - 16f * dir, headY - 1f - capeWave * 0.6f)
            lineTo(cx - 10f * dir, headY + 4f)
            // Spike 3 (Bottom trailing spike)
            lineTo(cx - 14f * dir, headY + 10f)
            lineTo(cx - 4f * dir, headY + 7f)
            close()
        }
        drawScope.drawPath(hairSpikesPath, color = hairShadow)
        drawScope.drawPath(hairSpikesPath, color = hairPrimary)
        drawScope.drawPath(hairSpikesPath, color = hairHighlight, style = Stroke(width = 1.2f))

        // Front Hair Bangs & Cap
        val hairFrontPath = Path().apply {
            moveTo(cx - 6f, headY + 4f)
            cubicTo(cx - 7f, headY - 2f, cx - 2f, headY - 4f, cx + 1f, headY - 3f)
            lineTo(cx + 6f * dir, headY - 1f)
            lineTo(cx + 6f * dir, headY + 4f)
            lineTo(cx + 2f * dir, headY + 1f)
            close()
        }
        drawScope.drawPath(hairFrontPath, color = hairPrimary)
        drawScope.drawPath(hairFrontPath, color = hairHighlight, style = Stroke(width = 1f))

        // Hair Tie Band (Crimson Red)
        drawScope.drawCircle(
            color = Color(0xFFD32F2F),
            radius = 2.4f,
            center = Offset(cx - 6f * dir, headY + 2f)
        )

        // Expressive Human Face: Eye, Brow, and Nose
        val eyeCenterX = cx + 2.2f * dir
        val eyeCenterY = headY + 7f
        // Eyebrow
        drawScope.drawLine(
            color = Color(0xFF0D47A1),
            start = Offset(eyeCenterX - 2f * dir, eyeCenterY - 2.2f),
            end = Offset(eyeCenterX + 2.5f * dir, eyeCenterY - 1.8f),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
        )
        // Sclera (Eye White)
        drawScope.drawOval(
            color = Color.White,
            topLeft = Offset(eyeCenterX - 2f, eyeCenterY - 1.2f),
            size = Size(4f, 2.8f)
        )
        // Iris & Pupil (Deep Blue)
        drawScope.drawCircle(
            color = Color(0xFF0D47A1),
            radius = 1.2f,
            center = Offset(eyeCenterX + 0.5f * dir, eyeCenterY)
        )
        // Eye Gleam
        drawScope.drawCircle(
            color = Color.White,
            radius = 0.5f,
            center = Offset(eyeCenterX + 0.2f * dir, eyeCenterY - 0.4f)
        )

        // Nose Bridge
        drawScope.drawLine(
            color = Color(0xFFE65100),
            start = Offset(cx + 3.5f * dir, headY + 7f),
            end = Offset(cx + 4.5f * dir, headY + 9f),
            strokeWidth = 0.8f,
            cap = StrokeCap.Round
        )

        // 5. ARMS, FORGED BROADSWORD & COMBAT SLASH ARCS
        if (isAttacking) {
            drawAttackAnimation(drawScope, player, cx, py, headY, torsoY, dir, facingRight, time)
        } else {
            drawIdleOrMovingArms(drawScope, player, cx, py, torsoY, dir, facingRight, isRunning, runCycle, breathOffset)
        }

        // Hurt aura / flash effect
        if (isHurt) {
            drawScope.drawRoundRect(
                color = Color(0x66FF1744),
                topLeft = Offset(px - 4f, py - 4f),
                size = Size(w + 8f, h + 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }

        // 12. STATUS EFFECT FOREGROUND OVERLAYS & OVERHEAD ICONS
        drawStatusEffectOverlays(drawScope, player, cx, py + h * 0.5f, time)
        drawOverheadStatusBadges(drawScope, player, cx, py, time)
    }

    private fun drawAttackAnimation(
        drawScope: DrawScope,
        player: Player,
        cx: Float,
        py: Float,
        headY: Float,
        torsoY: Float,
        dir: Float,
        facingRight: Boolean,
        time: Float
    ) {
        val frame = player.attackFrame
        val isPeakFrame = player.isAttackHitboxActive
        val combo = player.comboStep

        // Frame-based keyframe angle interpolation:
        // Frame 0: Windup / Chamber
        // Frame 1: Forward Drive
        // Frame 2: Peak Apex Impact (Hitbox Active!)
        // Frame 3: Follow-through / Recovery
        val frameProgress = when (frame) {
            0 -> 0.08f
            1 -> 0.40f
            2 -> 0.78f
            else -> 1.0f
        }

        // Dynamic angle calculation based on combo 1 vs combo 2 vs air attack
        val (startAngle, sweepAngle) = when {
            player.state == PlayerState.ATTACK_AIR -> Pair(-160f, 320f)
            combo % 2 == 1 -> Pair(-85f, 170f) // Downward Cleave
            else -> Pair(75f, -160f) // Upward Rising Slash
        }

        val currentAngle = (startAngle + sweepAngle * frameProgress) * (if (facingRight) 1f else -1f)
        val shoulderX = cx + (if (frame == 0) -1f else 3f) * dir
        val shoulderY = torsoY + (if (frame == 2) 5f else 3f)

        val swordLength = if (isPeakFrame) 48f else 44f
        val rad = currentAngle * PI.toFloat() / 180f
        val swordTip = Offset(shoulderX + cos(rad) * swordLength, shoulderY + sin(rad) * swordLength)

        // Multi-Layered Luminous Energy Slash Arc Trail (Burst at Peak Apex Frame 2)
        if (frame >= 1) {
            val arcRadius = swordLength + (if (isPeakFrame) 12f else 6f)
            val arcPath = Path().apply {
                moveTo(shoulderX, shoulderY)
                val radStart = (startAngle * (if (facingRight) 1f else -1f)) * PI.toFloat() / 180f
                val radCur = currentAngle * PI.toFloat() / 180f
                val steps = 8
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val angleStep = (startAngle + (currentAngle - startAngle) * t) * PI.toFloat() / 180f
                    val r = arcRadius * (0.85f + 0.15f * t)
                    val ax = shoulderX + cos(angleStep) * r
                    val ay = shoulderY + sin(angleStep) * r
                    if (i == 0) lineTo(ax, ay) else lineTo(ax, ay)
                }
                close()
            }

            // Arc Glow (Intensified at peak frame)
            val arcColor = when {
                isPeakFrame && combo % 2 == 1 -> Color(0xBB00E5FF)
                isPeakFrame -> Color(0xCCFFD700)
                combo % 2 == 1 -> Color(0x5500E5FF)
                else -> Color(0x55FFD700)
            }
            drawScope.drawPath(arcPath, color = arcColor)
            drawScope.drawPath(
                arcPath,
                color = if (isPeakFrame) Color.White else Color(0xAAFFFFFF),
                style = Stroke(width = if (isPeakFrame) 3.5f else 2.0f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // At peak frame, add energetic impact crescent
            if (isPeakFrame) {
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = swordTip
                )
                drawScope.drawCircle(
                    color = if (combo % 2 == 1) Color(0xFF00E5FF) else Color(0xFFFFD700),
                    radius = 9f,
                    center = swordTip,
                    style = Stroke(width = 2f)
                )
            }
        }

        // Hero's Extended Sword Arm (Bracer & Glove)
        val armElbow = Offset(shoulderX + cos(rad) * 12f, shoulderY + sin(rad) * 12f)
        drawScope.drawLine(
            color = Color(0xFF1976D2), // Sleeve
            start = Offset(shoulderX, shoulderY),
            end = armElbow,
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        drawScope.drawLine(
            color = Color(0xFF5D4037), // Leather Vambrace
            start = armElbow,
            end = Offset(shoulderX + cos(rad) * 18f, shoulderY + sin(rad) * 18f),
            strokeWidth = 4.2f,
            cap = StrokeCap.Round
        )

        // FORGED STEEL BROADSWORD
        val hiltCenter = Offset(shoulderX + cos(rad) * 18f, shoulderY + sin(rad) * 18f)

        // Crossguard (Golden Brass)
        val guardAngle = rad + PI.toFloat() / 2f
        val guardLen = 8f
        drawScope.drawLine(
            color = Color(0xFFFFD700),
            start = Offset(hiltCenter.x - cos(guardAngle) * guardLen, hiltCenter.y - sin(guardAngle) * guardLen),
            end = Offset(hiltCenter.x + cos(guardAngle) * guardLen, hiltCenter.y + sin(guardAngle) * guardLen),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        // Steel Blade with Fuller & Fiery Flame Wrap
        drawScope.drawLine(
            color = Color(0xFFECEFF1), // Blade Body
            start = hiltCenter,
            end = swordTip,
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
        drawScope.drawLine(
            color = Color(0xFFFFFFFF), // Razor Sharp Edge
            start = hiltCenter,
            end = swordTip,
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        // Fuller Groove
        drawScope.drawLine(
            color = Color(0xFF90A4AE),
            start = Offset(hiltCenter.x + cos(rad) * 3f, hiltCenter.y + sin(rad) * 3f),
            end = Offset(swordTip.x - cos(rad) * 6f, swordTip.y - sin(rad) * 6f),
            strokeWidth = 1f
        )

        // BLAZING SWORD FLAME PARTICLES & TONGUES OF FIRE (Swordigo Reference Flame Sword)
        val flameCount = 7
        for (f in 0 until flameCount) {
            val ft = f.toFloat() / flameCount
            val fx = hiltCenter.x + (swordTip.x - hiltCenter.x) * ft
            val fy = hiltCenter.y + (swordTip.y - hiltCenter.y) * ft
            val flameFlicker = sin(time * 24f + f * 1.5f) * 6f
            val flamePerpAngle = rad + PI.toFloat() / 2f
            val flameOffX = fx + cos(flamePerpAngle) * flameFlicker
            val flameOffY = fy + sin(flamePerpAngle) * flameFlicker - 4f

            // Outer Orange-Red Fire Core
            drawScope.drawCircle(
                color = Color(0xCCFF5722),
                radius = 5.5f + (1f - ft) * 3f,
                center = Offset(flameOffX, flameOffY)
            )
            // Inner Bright Golden Flame
            drawScope.drawCircle(
                color = Color(0xEEFFD600),
                radius = 3.2f + (1f - ft) * 2f,
                center = Offset(flameOffX, flameOffY)
            )
            // White-hot Ember Core
            drawScope.drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 1.6f,
                center = Offset(flameOffX, flameOffY)
            )
        }

        // Blade Tip Flaming Spark
        drawScope.drawCircle(
            color = Color(0xFFFF9100),
            radius = 5f,
            center = swordTip
        )
        drawScope.drawCircle(
            color = Color(0xFFFFFDE7),
            radius = 2.5f,
            center = swordTip
        )
    }

    private fun drawIdleOrMovingArms(
        drawScope: DrawScope,
        player: Player,
        cx: Float,
        py: Float,
        torsoY: Float,
        dir: Float,
        facingRight: Boolean,
        isRunning: Boolean,
        runCycle: Float,
        breathOffset: Float
    ) {
        val shoulderX = cx
        val shoulderY = torsoY + 4f

        // Back Arm / Sheath
        val scabbardAngle = if (facingRight) 130f else 50f
        val sRad = scabbardAngle * PI.toFloat() / 180f
        val scabbardStart = Offset(cx - 3f * dir, torsoY + 2f)
        val scabbardEnd = Offset(scabbardStart.x + cos(sRad) * 28f, scabbardStart.y + sin(sRad) * 28f)

        // Leather Scabbard
        drawScope.drawLine(
            color = Color(0xFF3E2723),
            start = scabbardStart,
            end = scabbardEnd,
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
        drawScope.drawLine(
            color = Color(0xFFFFD700),
            start = scabbardStart,
            end = Offset(scabbardStart.x + cos(sRad) * 6f, scabbardStart.y + sin(sRad) * 6f),
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )

        // Front Arm with Sleeve & Ready Guard
        val armSwing = if (isRunning) -runCycle * 10f else 0f
        val elbowX = cx + 4f * dir + armSwing * 0.4f
        val elbowY = torsoY + 11f + breathOffset
        val handX = cx + 7f * dir + armSwing
        val handY = torsoY + 14f

        // Upper Arm (Emerald Green Sleeve)
        drawScope.drawLine(
            color = Color(0xFF43A047),
            start = Offset(shoulderX + 2f * dir, shoulderY),
            end = Offset(elbowX, elbowY),
            strokeWidth = 4.8f,
            cap = StrokeCap.Round
        )
        // Forearm & Vambrace
        drawScope.drawLine(
            color = Color(0xFF5D4037),
            start = Offset(elbowX, elbowY),
            end = Offset(handX, handY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        // Leather Combat Glove
        drawScope.drawCircle(
            color = Color(0xFF3E2723),
            radius = 2.5f,
            center = Offset(handX, handY)
        )

        // Sword Hilt protruding from scabbard
        val hiltTip = Offset(scabbardStart.x - cos(sRad) * 12f, scabbardStart.y - sin(sRad) * 12f)
        drawScope.drawLine(
            color = Color(0xFF455A64),
            start = scabbardStart,
            end = hiltTip,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        // Pommel
        drawScope.drawCircle(
            color = Color(0xFFFFD700),
            radius = 2.5f,
            center = hiltTip
        )
        // Ember spark on sheathed blade
        drawScope.drawCircle(
            color = Color(0xFFFF9100),
            radius = 1.8f,
            center = hiltTip
        )
    }

    private fun drawStatusEffectAuras(
        drawScope: DrawScope,
        player: Player,
        cx: Float,
        cy: Float,
        dir: Float,
        isRunning: Boolean,
        time: Float
    ) {
        val effects = player.statusEffects.activeEffects
        if (effects.isEmpty()) return

        for (effect in effects) {
            when (effect.type) {
                StatusEffectType.SPEED_BUFF -> {
                    // Sonic haste rings & wind trails around player's feet and body
                    val ringRadius = 18f + sin(time * 16f) * 3f
                    val ringY = cy + 18f
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x4400E5FF), Color(0x1100E5FF), Color.Transparent),
                            center = Offset(cx, ringY),
                            radius = ringRadius
                        ),
                        radius = ringRadius,
                        center = Offset(cx, ringY)
                    )
                    // Trail silhouettes if moving
                    if (isRunning) {
                        drawScope.drawCircle(
                            color = Color(0x2800E5FF),
                            radius = 12f,
                            center = Offset(cx - dir * 14f, cy + 2f)
                        )
                        drawScope.drawCircle(
                            color = Color(0x1400E5FF),
                            radius = 10f,
                            center = Offset(cx - dir * 26f, cy + 4f)
                        )
                    }
                }
                StatusEffectType.BURNING -> {
                    // Intense glowing flame aura
                    val flamePulse = sin(time * 20f) * 3f
                    val flameRadius = 22f + flamePulse
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66FF3D00), Color(0x33FF9100), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = flameRadius
                        ),
                        radius = flameRadius,
                        center = Offset(cx, cy)
                    )
                    // Animated flame tongues licking upwards
                    for (i in 0..4) {
                        val flamePhase = time * 14f + i * 1.3f
                        val fx = cx + (i - 2) * 6f + sin(flamePhase) * 3f
                        val flameH = 14f + sin(flamePhase * 1.5f) * 6f
                        val flamePath = Path().apply {
                            moveTo(fx - 4f, cy + 16f)
                            quadraticTo(fx, cy + 16f - flameH * 1.2f, fx, cy + 16f - flameH)
                            quadraticTo(fx, cy + 16f - flameH * 1.2f, fx + 4f, cy + 16f)
                            close()
                        }
                        drawScope.drawPath(
                            path = flamePath,
                            color = if (i % 2 == 0) Color(0x99FF5722) else Color(0x99FFD600)
                        )
                    }
                }
                StatusEffectType.POISON -> {
                    // Toxic noxious miasma haze
                    val poisonRadius = 20f + sin(time * 6f) * 3f
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x5500E676), Color(0x221B5E20), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = poisonRadius
                        ),
                        radius = poisonRadius,
                        center = Offset(cx, cy)
                    )
                    // Orbiting toxic spores
                    for (i in 0..2) {
                        val angle = time * 3.5f + (i * 2.094f) // 120 degrees apart
                        val sporeDist = 17f + sin(time * 4f + i) * 3f
                        val sx = cx + cos(angle) * sporeDist
                        val sy = cy + sin(angle) * (sporeDist * 0.7f)
                        drawScope.drawCircle(
                            color = Color(0xCC00E676),
                            radius = 2.4f,
                            center = Offset(sx, sy)
                        )
                    }
                }
                StatusEffectType.FROST_CHILL -> {
                    // Glacial frost aura & crystalline glint
                    val frostRadius = 20f + sin(time * 4f) * 2f
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x5580D8FF), Color(0x1800B0FF), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = frostRadius
                        ),
                        radius = frostRadius,
                        center = Offset(cx, cy)
                    )
                    // Frost crystals along feet
                    for (i in -1..1) {
                        val fx = cx + i * 8f
                        val fy = cy + 18f
                        drawScope.drawLine(
                            color = Color(0xCC80D8FF),
                            start = Offset(fx, fy - 4f),
                            end = Offset(fx, fy + 2f),
                            strokeWidth = 2f
                        )
                        drawScope.drawLine(
                            color = Color(0xCC80D8FF),
                            start = Offset(fx - 3f, fy - 1f),
                            end = Offset(fx + 3f, fy - 1f),
                            strokeWidth = 1.8f
                        )
                    }
                }
                StatusEffectType.REGENERATION -> {
                    // Radiant healing blessing glow
                    val regenPulse = sin(time * 8f) * 4f
                    val regenRadius = 24f + regenPulse
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x4469F0AE), Color(0x18B9F6CA), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = regenRadius
                        ),
                        radius = regenRadius,
                        center = Offset(cx, cy)
                    )
                }
                StatusEffectType.STRENGTH_BUFF -> {
                    // Furious crimson warrior rage
                    val rageRadius = 22f + sin(time * 10f) * 3f
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x55FF1744), Color(0x22D50000), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = rageRadius
                        ),
                        radius = rageRadius,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }

    private fun drawStatusEffectOverlays(
        drawScope: DrawScope,
        player: Player,
        cx: Float,
        cy: Float,
        time: Float
    ) {
        val effects = player.statusEffects.activeEffects
        if (effects.isEmpty()) return

        for (effect in effects) {
            when (effect.type) {
                StatusEffectType.POISON -> {
                    // Green noxious tint pulse over player body
                    val alpha = 0.12f + sin(time * 8f) * 0.06f
                    drawScope.drawCircle(
                        color = Color(0xFF00E676).copy(alpha = alpha),
                        radius = 16f,
                        center = Offset(cx, cy)
                    )
                }
                StatusEffectType.BURNING -> {
                    // Fiery red/orange flicker tint over player body
                    val alpha = 0.15f + sin(time * 18f) * 0.08f
                    drawScope.drawCircle(
                        color = Color(0xFFFF5722).copy(alpha = alpha),
                        radius = 16f,
                        center = Offset(cx, cy)
                    )
                }
                StatusEffectType.FROST_CHILL -> {
                    // Frosted icy rime sheen
                    val alpha = 0.14f + sin(time * 5f) * 0.05f
                    drawScope.drawCircle(
                        color = Color(0xFF80D8FF).copy(alpha = alpha),
                        radius = 16f,
                        center = Offset(cx, cy)
                    )
                }
                else -> {}
            }
        }
    }

    private fun drawOverheadStatusBadges(
        drawScope: DrawScope,
        player: Player,
        cx: Float,
        py: Float,
        time: Float
    ) {
        val effects = player.statusEffects.activeEffects
        if (effects.isEmpty()) return

        val count = effects.size
        val badgeSpacing = 19f
        val startX = cx - ((count - 1) * badgeSpacing * 0.5f)
        val bob = sin(time * 4f) * 2f
        val badgeY = py - 18f + bob

        for ((idx, effect) in effects.withIndex()) {
            val bx = startX + idx * badgeSpacing
            val by = badgeY

            // 1. Dark circular backing disc
            drawScope.drawCircle(
                color = Color(0xDD121212),
                radius = 8.5f,
                center = Offset(bx, by)
            )

            // 2. Outer progress ring (shows remaining duration arc)
            val ringColor = when (effect.type) {
                StatusEffectType.POISON -> Color(0xFF00E676)
                StatusEffectType.BURNING -> Color(0xFFFF5722)
                StatusEffectType.SPEED_BUFF -> Color(0xFF00E5FF)
                StatusEffectType.REGENERATION -> Color(0xFF69F0AE)
                StatusEffectType.FROST_CHILL -> Color(0xFF80D8FF)
                StatusEffectType.STRENGTH_BUFF -> Color(0xFFFF1744)
            }

            // Dim track
            drawScope.drawCircle(
                color = ringColor.copy(alpha = 0.25f),
                radius = 8.5f,
                center = Offset(bx, by),
                style = Stroke(width = 1.8f)
            )

            // Active sweep
            val sweep = 360f * effect.progressPercent
            drawScope.drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(bx - 8.5f, by - 8.5f),
                size = Size(17f, 17f),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )

            // 3. Status Icon in Center
            when (effect.type) {
                StatusEffectType.POISON -> {
                    // Poison droplet / bubble
                    val dropPath = Path().apply {
                        moveTo(bx, by - 4.5f)
                        quadraticTo(bx + 3.2f, by, bx + 3.2f, by + 1.8f)
                        quadraticTo(bx + 3.2f, by + 4f, bx, by + 4f)
                        quadraticTo(bx - 3.2f, by + 4f, bx - 3.2f, by + 1.8f)
                        quadraticTo(bx - 3.2f, by, bx, by - 4.5f)
                        close()
                    }
                    drawScope.drawPath(path = dropPath, color = Color(0xFF00E676))
                }
                StatusEffectType.BURNING -> {
                    // Flame crest
                    val flamePath = Path().apply {
                        moveTo(bx, by - 4.8f)
                        quadraticTo(bx + 3.5f, by - 1f, bx + 2.8f, by + 3.8f)
                        quadraticTo(bx, by + 4.5f, bx - 2.8f, by + 3.8f)
                        quadraticTo(bx - 3.5f, by - 1f, bx, by - 4.8f)
                        close()
                    }
                    drawScope.drawPath(path = flamePath, color = Color(0xFFFF5722))
                    drawScope.drawCircle(color = Color(0xFFFFD600), radius = 1.5f, center = Offset(bx, by + 1f))
                }
                StatusEffectType.SPEED_BUFF -> {
                    // Swift lightning chevron
                    val speedPath = Path().apply {
                        moveTo(bx + 0.8f, by - 4.5f)
                        lineTo(bx - 3.2f, by - 0.2f)
                        lineTo(bx, by - 0.2f)
                        lineTo(bx - 0.8f, by + 4.5f)
                        lineTo(bx + 3.2f, by + 0.2f)
                        lineTo(bx, by + 0.2f)
                        close()
                    }
                    drawScope.drawPath(path = speedPath, color = Color(0xFF00E5FF))
                }
                StatusEffectType.REGENERATION -> {
                    // Healing cross
                    drawScope.drawLine(
                        color = Color(0xFF69F0AE),
                        start = Offset(bx, by - 4f),
                        end = Offset(bx, by + 4f),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                    drawScope.drawLine(
                        color = Color(0xFF69F0AE),
                        start = Offset(bx - 4f, by),
                        end = Offset(bx + 4f, by),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                }
                StatusEffectType.FROST_CHILL -> {
                    // Snowflake asterisk
                    for (a in 0 until 3) {
                        val rad = a * (PI.toFloat() / 3f)
                        drawScope.drawLine(
                            color = Color(0xFF80D8FF),
                            start = Offset(bx - cos(rad) * 4f, by - sin(rad) * 4f),
                            end = Offset(bx + cos(rad) * 4f, by + sin(rad) * 4f),
                            strokeWidth = 1.8f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                StatusEffectType.STRENGTH_BUFF -> {
                    // Sword icon
                    drawScope.drawLine(
                        color = Color(0xFFFF1744),
                        start = Offset(bx - 3f, by + 3f),
                        end = Offset(bx + 3f, by - 3f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                    drawScope.drawLine(
                        color = Color(0xFFFF8A80),
                        start = Offset(bx - 1f, by - 1f),
                        end = Offset(bx - 3f, by + 1f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
