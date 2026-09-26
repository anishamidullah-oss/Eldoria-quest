package com.example.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.game.engine.GameEngine
import com.example.game.model.GameScreen
import com.example.game.renderer.GameRenderer

@Composable
fun GameApp(
    modifier: Modifier = Modifier,
    enableGameLoop: Boolean = true
) {
    val context = LocalContext.current
    val engine = remember { GameEngine(context) }
    val renderer = remember { GameRenderer() }
    val textMeasurer = rememberTextMeasurer()

    var showGuideDialog by remember { mutableStateOf(false) }
    var showCharacterSheetDialog by remember { mutableStateOf(false) }
    var frameTick by remember { mutableStateOf(0L) }

    // High-performance 60 FPS game tick loop
    if (enableGameLoop) {
        LaunchedEffect(Unit) {
            var lastTimeNanos = System.nanoTime()
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    val dt = ((frameTimeNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    lastTimeNanos = frameTimeNanos

                    engine.update(dt)
                    frameTick = frameTimeNanos
                }
            }
        }
    }

    // Back button handling
    BackHandler {
        when {
            showGuideDialog -> showGuideDialog = false
            showCharacterSheetDialog -> showCharacterSheetDialog = false
            engine.activeDialogue != null -> {
                engine.activeDialogue = null
                engine.activeDialogueIndex = 0
            }
            engine.activeLoreText != null -> engine.activeLoreText = null
            engine.currentScreen == GameScreen.PLAYING -> engine.currentScreen = GameScreen.PAUSED
            engine.currentScreen == GameScreen.PAUSED -> engine.currentScreen = GameScreen.PLAYING
            engine.currentScreen == GameScreen.GAME_OVER || engine.currentScreen == GameScreen.VICTORY -> {
                engine.currentScreen = GameScreen.TITLE
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Game Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick
            renderer.render(this, engine, textMeasurer)
        }

        // Gameplay HUD & Touch Controls (When in PLAYING state)
        if (engine.currentScreen == GameScreen.PLAYING) {
            GameHud(
                engine = engine,
                onPauseClick = { engine.currentScreen = GameScreen.PAUSED }
            )

            val contextualLabel = when {
                engine.activeDialogue != null -> "CONTINUE"
                engine.activeLoreText != null -> "DISMISS"
                engine.nearbyLever != null -> if (engine.nearbyLever!!.isOn) "DEACTIVATE LEVER" else "FLIP LEVER"
                engine.nearbyNPC != null -> "TALK"
                engine.nearbyLore != null -> "READ TABLET"
                engine.nearbyChest != null && !engine.nearbyChest!!.isOpened -> "OPEN CHEST"
                else -> null
            }

            TouchControls(
                onMove = { x, y ->
                    engine.inputX = x
                    engine.inputY = y
                },
                onJump = {
                    engine.onJumpPressed()
                },
                onJumpRelease = {
                    engine.onJumpReleased()
                },
                onAttack = {
                    engine.onAttackPressed()
                },
                interactLabel = contextualLabel,
                onInteract = {
                    engine.interactWithNearby()
                }
            )
        }

        // Overlay Menus
        when (engine.currentScreen) {
            GameScreen.TITLE -> {
                TitleMenu(
                    engine = engine,
                    onStartGame = { engine.startNewGame() },
                    onContinueGame = { engine.continueSavedGame() },
                    onOpenGuide = { showGuideDialog = true },
                    onOpenCharacterSheet = { showCharacterSheetDialog = true }
                )
            }
            GameScreen.PAUSED -> {
                PauseMenu(
                    engine = engine,
                    onResume = { engine.currentScreen = GameScreen.PLAYING },
                    onRestartCheckpoint = { engine.respawnPlayer() },
                    onRestartFull = { engine.startNewGame() },
                    onOpenGuide = { showGuideDialog = true },
                    onOpenCharacterSheet = { showCharacterSheetDialog = true },
                    onExitToTitle = { engine.currentScreen = GameScreen.TITLE }
                )
            }
            GameScreen.GAME_OVER -> {
                GameOverMenu(
                    engine = engine,
                    onRespawnCheckpoint = { engine.respawnPlayer() },
                    onRestartFull = { engine.startNewGame() },
                    onExitToTitle = { engine.currentScreen = GameScreen.TITLE }
                )
            }
            GameScreen.VICTORY -> {
                VictoryMenu(
                    engine = engine,
                    onPlayAgain = { engine.startNewGame() },
                    onExitToTitle = { engine.currentScreen = GameScreen.TITLE }
                )
            }
            else -> {}
        }

        // Adventurer's Guide Dialog
        if (showGuideDialog) {
            GuideDialog(onClose = { showGuideDialog = false })
        }

        // Character Stats, Inventory & Quest Log Dialog
        if (showCharacterSheetDialog) {
            CharacterAndQuestDialog(
                engine = engine,
                onClose = { showCharacterSheetDialog = false }
            )
        }
    }
}
