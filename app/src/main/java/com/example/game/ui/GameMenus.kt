package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.effects.StatusEffectType
import com.example.game.engine.GameEngine

@Composable
fun TitleMenu(
    engine: GameEngine,
    onStartGame: () -> Unit,
    onContinueGame: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenCharacterSheet: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = "Eldoria Hero Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x99000000),
                            Color(0xBB0F172A),
                            Color(0xEE0A0F1D)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1.15f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x99064E3B))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% OFFLINE • LOCAL SAVE READY",
                        color = Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "THE LOST KINGDOM",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700),
                    letterSpacing = 2.sp
                )

                Text(
                    text = "The Blade of Ancient Realms",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF81D4FA)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Explore the vast realm of Aether, uncover hidden caves, solve puzzle mechanisms, defeat optional bosses, and complete side quests.",
                    fontSize = 12.sp,
                    color = Color(0xFFCFD8DC),
                    lineHeight = 17.sp,
                    modifier = Modifier.widthIn(max = 420.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Developer: AnishAmidullah",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB74D)
                )

                if (engine.saveManager.highScore > 0 || engine.saveManager.totalCoinsCollected > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "High Score: ${engine.saveManager.highScore}  |  Lifetime Coins: ${engine.saveManager.totalCoinsCollected}",
                        fontSize = 11.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.85f)
                    .padding(start = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (engine.saveManager.hasSavedGame) {
                    val savedState = remember { engine.saveManager.loadGameState() }
                    Button(
                        onClick = onContinueGame,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_adventure_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONTINUE (Lv.${savedState.level} • ${savedState.checkpointName})",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_adventure_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (engine.saveManager.hasSavedGame) "NEW ADVENTURE" else "START ADVENTURE",
                        color = Color(0xFF0F172A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenCharacterSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("character_quests_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9800))))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHARACTER & QUEST LOG",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD700)
                    )
                }

                OutlinedButton(
                    onClick = onOpenGuide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("controls_guide_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFF81D4FA), Color(0xFF0288D1))))
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = null,
                        tint = Color(0xFF81D4FA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOW TO PLAY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { engine.toggleSound() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (engine.saveManager.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (engine.saveManager.isSoundEnabled) "SOUND: ON" else "SOUND: MUTED",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PauseMenu(
    engine: GameEngine,
    onResume: () -> Unit,
    onRestartCheckpoint: () -> Unit,
    onRestartFull: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenCharacterSheet: () -> Unit,
    onExitToTitle: () -> Unit
) {
    var saveFeedbackText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC0A0F1D)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFD4AF37), Color(0xFF0288D1))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )

                Text(
                    text = "Area: ${engine.currentAreaName} | Checkpoint: ${engine.lastActiveCheckpointName}",
                    fontSize = 12.sp,
                    color = Color(0xFF90A4AE)
                )

                if (saveFeedbackText != null) {
                    Text(
                        text = saveFeedbackText!!,
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("resume_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text(text = "RESUME GAME", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenCharacterSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("pause_character_quests_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "CHARACTER & QUEST LOG", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        engine.saveCurrentProgress()
                        saveFeedbackText = "Progress Saved Locally to Device!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("save_progress_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "SAVE GAME PROGRESS", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onRestartCheckpoint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("restart_checkpoint_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "RETRY FROM CHECKPOINT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // STATUS EFFECTS TEST LAB & MODIFIERS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STATUS EFFECTS LAB",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700)
                            )
                            val activeCount = engine.player.statusEffects.activeEffects.size
                            Text(
                                text = if (activeCount > 0) "$activeCount Active" else "None Active",
                                fontSize = 11.sp,
                                color = if (activeCount > 0) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }

                        // Modifier Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val speedMult = (engine.player.statusEffects.speedMultiplier * 100f).toInt()
                            val dmgMult = (engine.player.statusEffects.damageMultiplier * 100f).toInt()
                            Text(
                                text = "Speed: $speedMult%",
                                fontSize = 11.sp,
                                color = if (speedMult != 100) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Attack: $dmgMult%",
                                fontSize = 11.sp,
                                color = if (dmgMult != 100) Color(0xFFFF1744) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick Test Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { engine.testApplyStatusEffect(StatusEffectType.POISON, 6f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(text = "+Poison", fontSize = 10.sp, color = Color(0xFFB9F6CA), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { engine.testApplyStatusEffect(StatusEffectType.BURNING, 5f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF360C)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(text = "+Burn", fontSize = 10.sp, color = Color(0xFFFFCCBC), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { engine.testApplyStatusEffect(StatusEffectType.SPEED_BUFF, 8f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(text = "+Haste", fontSize = 10.sp, color = Color(0xFF80DEEA), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { engine.testApplyStatusEffect(StatusEffectType.REGENERATION, 6f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(text = "+Regen", fontSize = 10.sp, color = Color(0xFFA7FFEB), fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { engine.testApplyStatusEffect(StatusEffectType.FROST_CHILL, 5f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01579B)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(text = "+Frost", fontSize = 10.sp, color = Color(0xFF80D8FF), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { engine.testApplyStatusEffect(StatusEffectType.STRENGTH_BUFF, 8f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(text = "+Might", fontSize = 10.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { engine.testClearAllStatusEffects() },
                                modifier = Modifier.weight(1f).height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(text = "Clear All", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenGuide,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                    ) {
                        Text(text = "GUIDE", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onExitToTitle,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                    ) {
                        Text(text = "MAIN MENU", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterAndQuestDialog(
    engine: GameEngine,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val player = engine.player

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD0A0F1D))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF0288D1))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADVENTURER'S JOURNAL",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // 3 Tabs: Stats & Gear, Main Quests, Side Quests & Secrets
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFFFFD700),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFFFD700)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("STATS & GEAR", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("MAIN STORY", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("SIDE QUESTS & SECRETS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                when (selectedTab) {
                    0 -> {
                        // TAB 0: Character Stats, Equipment, Key Artifacts & Secrets
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "HERO LEVEL ${player.level}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "Experience: ${player.xp} / ${player.xpToNextLevel} XP",
                                        fontSize = 12.sp,
                                        color = Color(0xFF81D4FA)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Vitality: ${player.health}/${player.maxHealth} HP",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252)
                                    )
                                    Text(
                                        text = "Bonus Attack: +${player.bonusDamage} DMG",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF69F0AE)
                                    )
                                }
                            }

                            // ACTIVE STATUS EFFECTS & MODIFIERS
                            val activeEffects = player.statusEffects.activeEffects
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ACTIVE MODIFIERS",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB74D)
                                )
                                val statusSummary = if (activeEffects.isEmpty()) {
                                    "No Active Afflictions or Buffs"
                                } else {
                                    activeEffects.joinToString(", ") { "${it.type.name} (${String.format("%.1f", it.durationSeconds)}s)" }
                                }
                                Text(
                                    text = statusSummary,
                                    fontSize = 11.sp,
                                    color = if (activeEffects.isEmpty()) Color(0xFF94A3B8) else Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(text = "EQUIPPED GEAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GearCard(title = "WEAPON", item = player.equippedWeapon, modifier = Modifier.weight(1f))
                                GearCard(title = "ARMOR", item = player.equippedArmor, modifier = Modifier.weight(1f))
                                GearCard(title = "ACCESSORY", item = player.equippedAccessory, modifier = Modifier.weight(1f))
                            }

                            Text(text = "KEY ARTIFACTS & RELICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GearCard(title = "CITADEL KEYS", item = "${player.citadelKeysCount} / 3 Found", modifier = Modifier.weight(1f))
                                GearCard(title = "HEART SHARDS", item = if (player.hasHeartFragment1) "Fragment I Recovered" else "Not Claimed", modifier = Modifier.weight(1f))
                                GearCard(title = "SECRETS FOUND", item = "${player.secretsFoundCount} Secrets", modifier = Modifier.weight(1f))
                            }

                            Text(text = "QUEST ITEMS & MATERIALS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GearCard(title = "BRASS COMPASS", item = if (player.hasBrassCompass) "In Bag" else "Missing", modifier = Modifier.weight(1f))
                                GearCard(title = "RUNIC ORE", item = "${player.runicOreCount}/3 Chunks", modifier = Modifier.weight(1f))
                                GearCard(title = "GUARD CREST", item = if (player.hasGuardBadge) "Recovered" else "Missing", modifier = Modifier.weight(1f))
                            }

                            Text(text = "DISCOVERED REALM REGIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                            Text(text = player.unlockedAreas.joinToString(" • "), fontSize = 11.5.sp, color = Color(0xFFCFD8DC))
                        }
                    }
                    1 -> {
                        // TAB 1: Complete 10-Mission Story Quest Log
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (q in engine.questManager.quests) {
                                val isCompleted = q.isCompleted || player.completedQuestIds.contains(q.id)
                                val isActive = engine.questManager.currentQuest?.id == q.id && !isCompleted

                                val statusText = when {
                                    isCompleted -> "COMPLETED"
                                    isActive -> "ACTIVE (${q.currentProgress}/${q.targetCount})"
                                    else -> "UPCOMING"
                                }

                                QuestItemRow(
                                    title = "Mission ${q.id}: ${q.title}",
                                    description = "${q.objective}\nReward: +${q.reward.xp} XP, +${q.reward.coins} Gold, ${q.reward.rewardItemName}",
                                    progressText = statusText,
                                    isComplete = isCompleted,
                                    isActive = isActive
                                )
                            }
                        }
                    }
                    2 -> {
                        // TAB 2: Side Quests & Secrets
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Secrets & Puzzles Overview Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                    Text(text = "Realm Secrets: ${player.secretsFoundCount} Discovered", color = Color(0xFFBAE6FD), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(18.dp))
                                    Text(text = "Puzzles: ${player.solvedPuzzles.size} Solved", color = Color(0xFFBBF7D0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(text = "SIDE MISSIONS (10+ ADVENTURES)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))

                            for (sq in engine.sideQuestManager.sideQuests) {
                                val isCompleted = sq.isCompleted || player.completedSideQuestIds.contains(sq.id)
                                val isAccepted = sq.isAccepted || player.acceptedSideQuestIds.contains(sq.id)

                                val statusText = when {
                                    isCompleted -> "COMPLETED"
                                    isAccepted -> "IN PROGRESS (${sq.currentProgress}/${sq.targetCount})"
                                    else -> "TALK TO ${sq.npcName.uppercase()}"
                                }

                                QuestItemRow(
                                    title = "${sq.title} [NPC: ${sq.npcName}]",
                                    description = "${sq.description}\nObjective: ${sq.objectiveText}\nReward: +${sq.reward.xp} XP, +${sq.reward.coins} Gold, +${sq.reward.maxHealthBonus} Max HP",
                                    progressText = statusText,
                                    isComplete = isCompleted,
                                    isActive = isAccepted && !isCompleted
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF90A4AE),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Offline Game: All level, inventory, quest, and secret data are saved locally on this device.",
                        fontSize = 11.sp,
                        color = Color(0xFF90A4AE)
                    )
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                ) {
                    Text(text = "BACK TO GAME", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun GearCard(title: String, item: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = title, fontSize = 9.5.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = item, fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuestItemRow(
    title: String,
    description: String,
    progressText: String,
    isComplete: Boolean,
    isActive: Boolean = false
) {
    val bgColor = when {
        isComplete -> Color(0x3310B981)
        isActive -> Color(0x33F59E0B)
        else -> Color(0xFF0F172A)
    }
    val borderColor = when {
        isComplete -> Color(0xFF10B981)
        isActive -> Color(0xFFF59E0B)
        else -> Color(0xFF334155)
    }
    val titleColor = when {
        isComplete -> Color(0xFF34D399)
        isActive -> Color(0xFFFFD700)
        else -> Color(0xFF94A3B8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.5.sp,
                color = Color(0xFFCFD8DC),
                lineHeight = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = progressText,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = when {
                isComplete -> Color(0xFF34D399)
                isActive -> Color(0xFFFBBF24)
                else -> Color(0xFF64748B)
            }
        )
    }
}

@Composable
fun GameOverMenu(
    engine: GameEngine,
    onRespawnCheckpoint: () -> Unit,
    onRestartFull: () -> Unit,
    onExitToTitle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD1B0000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFFF1744), Color(0xFF880E4F))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DEFEATED",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF5252),
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Your vitality has waned, but the realm needs you!",
                    fontSize = 13.sp,
                    color = Color(0xFFCFD8DC),
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(text = "Coins: ${engine.player.coins}", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Crystals: ${engine.player.crystals}", color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Defeated: ${engine.player.enemiesDefeated}", color = Color(0xFFFF8A80), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRespawnCheckpoint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("respawn_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "REVIVE AT CHECKPOINT", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRestartFull,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "RESTART LEVEL", fontSize = 13.sp)
                }

                Button(
                    onClick = onExitToTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                ) {
                    Text(text = "RETURN TO TITLE", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun VictoryMenu(
    engine: GameEngine,
    onPlayAgain: () -> Unit,
    onExitToTitle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD001A2C)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00E5FF))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "KINGDOM RESTORED!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700),
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "You have conquered the Whispering Woods, navigated the Deeproot Caverns, defeated the Ruin Colossus, and recovered the Heart of Aether!",
                    fontSize = 13.sp,
                    color = Color(0xFFB0BEC5),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "COINS", fontSize = 11.sp, color = Color(0xFF90A4AE))
                        Text(text = "${engine.player.coins}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SECRETS", fontSize = 11.sp, color = Color(0xFF90A4AE))
                        Text(text = "${engine.player.secretsFoundCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "ENEMIES", fontSize = 11.sp, color = Color(0xFF90A4AE))
                        Text(text = "${engine.player.enemiesDefeated}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SCORE", fontSize = 11.sp, color = Color(0xFF90A4AE))
                        Text(text = "${engine.player.score}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF69F0AE))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("play_again_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "PLAY AGAIN", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExitToTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(text = "MAIN MENU", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun GuideDialog(
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD0A0F1D))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF81D4FA), Color(0xFF0288D1))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ADVENTURER'S GUIDE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700)
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                GuideSection(
                    title = "TOUCH CONTROLS",
                    content = "• Left/Right Buttons: Run across the kingdom.\n• Sword Button: Perform swift melee slashes with your blade.\n• Jump Button: Leap across obstacles and platforms.\n• Contextual Button (TALK / FLIP LEVER / OPEN / READ): Appears when near NPCs, levers, chests, or tablets."
                )

                GuideSection(
                    title = "PUZZLES & EXPLORATION",
                    content = "• Pressure Plates: Step on blue plates to open linked heavy gates.\n• Lever Switches: Approach and flip to unlock secret paths.\n• Runic Torches: Strike magical braziers with your sword to ignite them.\n• Crumbling Walls: Strike cracked stone walls to reveal hidden secret chambers!\n• Moving Platforms: Step onto levitating platforms to traverse chasms."
                )

                GuideSection(
                    title = "OPTIONAL CONTENT & SECRETS",
                    content = "• 10+ Side Missions: Meet NPCs across the kingdom for unique quests and rich rewards.\n• Secret Bosses: Challenge Dreadfang Wolf, Frost Wurm, and Shadow Lich.\n• Relic Collectibles: Find the Brass Compass, Ancient Map, Sentry Crest, and Holy Chalice.\n• 100% Offline Save: All progress is preserved automatically."
                )

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                ) {
                    Text(text = "UNDERSTOOD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun GuideSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81D4FA))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = content, fontSize = 11.5.sp, color = Color(0xFFECEFF1), lineHeight = 16.sp)
    }
}
