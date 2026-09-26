package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.effects.StatusEffect
import com.example.game.effects.StatusEffectType
import com.example.game.engine.GameEngine
import kotlin.math.min

@Composable
fun GameHud(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.player
    val targetEnemy = engine.targetedEnemy
    val activeQuest = engine.questManager.currentQuest

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // 1. TOP LEFT: Mana Crystals, Gold Coins & Hero Level
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Blue Crystal Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC0F172A))
                    .border(1.2.dp, Color(0x8800E5FF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width
                    val h = size.height
                    val crystal = Path().apply {
                        moveTo(w * 0.5f, 1f)
                        lineTo(w * 0.9f, h * 0.35f)
                        lineTo(w * 0.5f, h - 1f)
                        lineTo(w * 0.1f, h * 0.35f)
                        close()
                    }
                    drawPath(crystal, color = Color(0xFF00E5FF))
                    drawLine(Color(0xCCFFFFFF), Offset(w * 0.5f, 1f), Offset(w * 0.5f, h - 1f), strokeWidth = 1.2f)
                }

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${player.crystals}",
                    color = Color(0xFFE0F7FA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Gold Coin Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC0F172A))
                    .border(1.2.dp, Color(0x88FFD700), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawCircle(color = Color(0xFFFFD700), radius = 6.5f, center = Offset(size.width * 0.5f, size.height * 0.5f))
                    drawCircle(color = Color(0xFFFF8F00), radius = 4f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1f))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${player.coins}",
                    color = Color(0xFFFFF9C4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Hero Level Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xCC1E1B4B))
                    .border(1.2.dp, Color(0x88A855F7), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Lv.${player.level}",
                    color = Color(0xFFE9D5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Key Items (Citadel Keys)
            if (player.citadelKeysCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xCC14532D))
                        .border(1.2.dp, Color(0xFF86EFAC), RoundedCornerShape(14.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Keys",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${player.citadelKeysCount}/3",
                        color = Color(0xFFDCFCE7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. TOP CENTER: Active Mission Tracker & Area Banner
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Region Notification Banner
            AnimatedVisibility(
                visible = engine.areaBannerTimer > 0f,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x000F172A), Color(0xEE0F172A), Color(0xEE0F172A), Color(0x000F172A))
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = engine.currentAreaName,
                        color = Color(0xFFFFF8E1),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Active Quest Widget
            if (activeQuest != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xDD0B132B))
                        .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(12.dp))
                        .clickable { onPauseClick() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Mission",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(13.dp)
                        )
                        Column {
                            Text(
                                text = "M${activeQuest.id}: ${activeQuest.title}",
                                color = Color(0xFFFDE68A),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = activeQuest.objective,
                                color = Color(0xFFCBD5E1),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Mission Completed Banner Fanfare
            AnimatedVisibility(
                visible = engine.questCompletionBanner != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                val bannerText = engine.questCompletionBanner ?: ""
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xDD78350F), Color(0xFFD97706), Color(0xDD78350F))
                            )
                        )
                        .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Complete",
                            tint = Color(0xFFFFFBEB),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = bannerText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 3. TOP RIGHT: 3D Heart health containers, Potion, Audio, Pause
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Heart Containers
            val totalHearts = (player.maxHealth / 20).coerceAtLeast(3)
            val hpPerHeart = player.maxHealth.toFloat() / totalHearts.toFloat()

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                for (i in 0 until totalHearts) {
                    val heartMinHp = i * hpPerHeart
                    val heartMaxHp = (i + 1) * hpPerHeart
                    val fillRatio = ((player.health.toFloat() - heartMinHp) / (heartMaxHp - heartMinHp)).coerceIn(0f, 1f)
                    Heart3DView(fillRatio = fillRatio)
                }
            }

            // Quick Potion Button
            if (player.potions > 0) {
                Box(
                    modifier = Modifier
                        .testTag("potion_button")
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (player.health < player.maxHealth) Color(0xDD991B1B) else Color(0xAA374151))
                        .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(14.dp))
                        .clickable { engine.usePotion() }
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heal",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "x${player.potions}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Swiftness Potion Button (Speed Buff)
            if (player.swiftnessPotions > 0) {
                val isSpeedActive = player.hasStatusEffect(StatusEffectType.SPEED_BUFF)
                Box(
                    modifier = Modifier
                        .testTag("swiftness_potion_button")
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSpeedActive) Color(0xDD006064) else Color(0xCC00838F))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
                        .clickable { engine.useSwiftnessPotion() }
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            val w = size.width
                            val h = size.height
                            val bolt = Path().apply {
                                moveTo(w * 0.6f, 0f)
                                lineTo(w * 0.12f, h * 0.52f)
                                lineTo(w * 0.52f, h * 0.52f)
                                lineTo(w * 0.4f, h)
                                lineTo(w * 0.88f, h * 0.48f)
                                lineTo(w * 0.48f, h * 0.48f)
                                close()
                            }
                            drawPath(bolt, color = Color(0xFF00E5FF))
                        }
                        Text(
                            text = "x${player.swiftnessPotions}",
                            color = Color(0xFFE0F7FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Audio Mute Toggle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("sound_toggle")
                    .clip(CircleShape)
                    .background(Color(0xCC1E293B))
                    .border(1.dp, Color(0x44FFFFFF), CircleShape)
                    .clickable { engine.toggleSound() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (engine.saveManager.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle Sound",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Pause Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .testTag("pause_button")
                    .clip(CircleShape)
                    .background(Color(0xCC1E293B))
                    .border(1.dp, Color(0x44FFFFFF), CircleShape)
                    .clickable { onPauseClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause Game",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // 3b. ACTIVE STATUS EFFECTS STRIP (Top Right below stats)
        val activeEffects = player.statusEffects.activeEffects
        if (activeEffects.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 42.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (effect in activeEffects) {
                    StatusEffectHudChip(effect = effect)
                }
            }
        }

        // 4. BOTTOM CENTER: Interactive NPC Dialogue Box
        val dialog = engine.activeDialogue
        if (dialog != null && engine.activeDialogueIndex in dialog.indices) {
            val currentLine = dialog[engine.activeDialogueIndex]
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 68.dp)
                    .fillMaxWidth(0.85f)
                    .testTag("dialogue_box")
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFA1E293B), Color(0xFA0F172A))
                        )
                    )
                    .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { engine.interactWithNearby() })
                    }
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Speaker Name & Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Speaker",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = currentLine.speakerName,
                                color = Color(0xFFFDE68A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Text(
                            text = "${engine.activeDialogueIndex + 1}/${dialog.size}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Spoken Text
                    Text(
                        text = currentLine.text,
                        color = Color(0xFFF8FAFC),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 17.sp
                    )

                    // Tap to advance prompt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (engine.activeDialogueIndex < dialog.size - 1) "Tap to continue ▶" else "Tap to close ✕",
                            color = Color(0xFFFBBF24),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. ANCIENT LORE TABLET INSCRIPTION MODAL
        val loreText = engine.activeLoreText
        if (loreText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.8f)
                    .testTag("lore_modal")
                    .shadow(16.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFA1E1B4B), Color(0xFA0F172A))
                        )
                    )
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Lore",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ANCIENT RUNIC SCRIPTURE",
                            color = Color(0xFFBAE6FD),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = loreText,
                        color = Color(0xFFF0F9FF),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0284C7))
                            .clickable { engine.interactWithNearby() }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Store In Journal",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Heart3DView(fillRatio: Float) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.82f)
            cubicTo(w * 0.15f, h * 0.55f, 0f, h * 0.35f, 0f, h * 0.22f)
            cubicTo(0f, h * 0.05f, w * 0.22f, 0f, w * 0.5f, h * 0.22f)
            cubicTo(w * 0.78f, 0f, w, h * 0.05f, w, h * 0.22f)
            cubicTo(w, h * 0.35f, w * 0.85f, h * 0.55f, w * 0.5f, h * 0.82f)
            close()
        }

        drawPath(heartPath, color = Color(0xFF263238), style = Fill)
        drawPath(heartPath, color = Color(0xFF78909C), style = Stroke(width = 1.2f, join = StrokeJoin.Round))

        if (fillRatio > 0f) {
            val clipRect = androidx.compose.ui.geometry.Rect(0f, 0f, w * fillRatio, h)
            drawContext.canvas.save()
            drawContext.canvas.clipRect(clipRect)

            val heartBrush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF5252), Color(0xFFD50000), Color(0xFF880E4F)),
                center = Offset(w * 0.4f, h * 0.35f),
                radius = w * 0.65f
            )
            drawPath(heartPath, brush = heartBrush, style = Fill)
            drawCircle(color = Color(0xAAFFFFFF), radius = 1.8f, center = Offset(w * 0.32f, h * 0.22f))
            drawContext.canvas.restore()
        }

        drawPath(heartPath, color = Color(0xFFCFD8DC), style = Stroke(width = 1.0f, join = StrokeJoin.Round))
    }
}

@Composable
private fun StatusEffectHudChip(effect: StatusEffect) {
    val (effectColor, label) = when (effect.type) {
        StatusEffectType.POISON -> Pair(Color(0xFF00E676), "POISON")
        StatusEffectType.BURNING -> Pair(Color(0xFFFF5722), "BURNING")
        StatusEffectType.SPEED_BUFF -> Pair(Color(0xFF00E5FF), "HASTE +45%")
        StatusEffectType.REGENERATION -> Pair(Color(0xFF69F0AE), "REGEN")
        StatusEffectType.FROST_CHILL -> Pair(Color(0xFF80D8FF), "FROST -40%")
        StatusEffectType.STRENGTH_BUFF -> Pair(Color(0xFFFF1744), "MIGHT +50%")
    }

    val remainingSec = String.format("%.1fs", effect.durationSeconds)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xEE111827))
            .border(1.2.dp, effectColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Animated indicator dot
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = effectColor, radius = size.minDimension * 0.45f)
            }

            Text(
                text = label,
                color = effectColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = remainingSec,
                color = Color(0xFFF3F4F6),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

