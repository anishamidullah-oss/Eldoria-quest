package com.example.game.model

enum class GameScreen {
    TITLE,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY,
    CONTROLS_GUIDE,
    CREDITS
}

data class PlayerStats(
    val coins: Int = 0,
    val crystals: Int = 0,
    val score: Int = 0,
    val health: Int = 100,
    val maxHealth: Int = 100,
    val enemiesDefeated: Int = 0,
    val checkpointsReached: Int = 0,
    val gameTimeSeconds: Float = 0f
)
