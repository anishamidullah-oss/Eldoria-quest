package com.example.game.save

import android.content.Context
import android.content.SharedPreferences

data class PlayerSaveState(
    val hasSavedGame: Boolean = false,
    val posX: Float = 80f,
    val posY: Float = 400f,
    val checkpointName: String = "Forest Entrance",
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 100,
    val health: Int = 100,
    val maxHealth: Int = 100,
    val bonusDamage: Int = 0,
    val coins: Int = 0,
    val crystals: Int = 0,
    val score: Int = 0,
    val potions: Int = 2,
    val citadelKeysCount: Int = 0,
    val hasCitadelKey: Boolean = false,
    val hasAncientRelic: Boolean = false,
    val hasHeartFragment1: Boolean = false,
    val hasHeartFragment2: Boolean = false,
    val hasHeartFragment3: Boolean = false,
    val hasMasterKey: Boolean = false,
    val hasBrassCompass: Boolean = false,
    val hasMapParchment: Boolean = false,
    val runicOreCount: Int = 0,
    val hasGuardBadge: Boolean = false,
    val secretsFoundCount: Int = 0,
    val solvedPuzzles: Set<String> = emptySet(),
    val openedChests: Set<String> = emptySet(),
    val defeatedBosses: Set<String> = emptySet(),
    val collectedSecretRelics: Set<String> = emptySet(),
    val acceptedSideQuestIds: Set<String> = emptySet(),
    val completedSideQuestIds: Set<String> = emptySet(),
    val sideQuestProgressMap: Map<String, Int> = emptyMap(),
    val equippedWeapon: String = "Steel Broadsword",
    val equippedArmor: String = "Adventurer Tunic",
    val equippedAccessory: String = "Warrior Ring",
    val unlockedAreas: Set<String> = setOf("The Whispering Woods"),
    val activeQuestId: Int = 1,
    val activeQuestProgress: Int = 0,
    val completedQuestIds: Set<String> = emptySet(),
    val questBossDefeated: Boolean = false,
    val questSlimesDefeated: Int = 0,
    val questCrystalsCollected: Int = 0,
    val questChestsOpened: Int = 0,
    val gameTime: Float = 0f
)

class SaveManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("eldoria_offline_save_data", Context.MODE_PRIVATE)

    var hasSavedGame: Boolean
        get() = prefs.getBoolean("has_saved_game", false)
        set(value) = prefs.edit().putBoolean("has_saved_game", value).apply()

    var highScore: Int
        get() = prefs.getInt("high_score", 0)
        set(value) = prefs.edit().putInt("high_score", value).apply()

    var totalCoinsCollected: Int
        get() = prefs.getInt("total_lifetime_coins", 0)
        set(value) = prefs.edit().putInt("total_lifetime_coins", value).apply()

    var bestTimeSeconds: Float
        get() = prefs.getFloat("best_time", 999999f)
        set(value) = prefs.edit().putFloat("best_time", value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var isScreenShakeEnabled: Boolean
        get() = prefs.getBoolean("screen_shake_enabled", true)
        set(value) = prefs.edit().putBoolean("screen_shake_enabled", value).apply()

    // Save complete game state locally to device storage (100% offline)
    fun saveGameState(state: PlayerSaveState) {
        val editor = prefs.edit()
            .putBoolean("has_saved_game", true)
            .putFloat("player_pos_x", state.posX)
            .putFloat("player_pos_y", state.posY)
            .putString("checkpoint_name", state.checkpointName)
            .putInt("player_level", state.level)
            .putInt("player_xp", state.xp)
            .putInt("player_xp_next", state.xpToNextLevel)
            .putInt("player_health", state.health)
            .putInt("player_max_health", state.maxHealth)
            .putInt("player_bonus_damage", state.bonusDamage)
            .putInt("player_coins", state.coins)
            .putInt("player_crystals", state.crystals)
            .putInt("player_score", state.score)
            .putInt("player_potions", state.potions)
            .putInt("citadel_keys_count", state.citadelKeysCount)
            .putBoolean("has_citadel_key", state.hasCitadelKey)
            .putBoolean("has_ancient_relic", state.hasAncientRelic)
            .putBoolean("has_heart_frag_1", state.hasHeartFragment1)
            .putBoolean("has_heart_frag_2", state.hasHeartFragment2)
            .putBoolean("has_heart_frag_3", state.hasHeartFragment3)
            .putBoolean("has_master_key", state.hasMasterKey)
            .putBoolean("has_brass_compass", state.hasBrassCompass)
            .putBoolean("has_map_parchment", state.hasMapParchment)
            .putInt("runic_ore_count", state.runicOreCount)
            .putBoolean("has_guard_badge", state.hasGuardBadge)
            .putInt("secrets_found_count", state.secretsFoundCount)
            .putStringSet("solved_puzzles", state.solvedPuzzles)
            .putStringSet("opened_chests", state.openedChests)
            .putStringSet("defeated_bosses", state.defeatedBosses)
            .putStringSet("collected_secret_relics", state.collectedSecretRelics)
            .putStringSet("accepted_side_quests", state.acceptedSideQuestIds)
            .putStringSet("completed_side_quests", state.completedSideQuestIds)
            .putString("equipped_weapon", state.equippedWeapon)
            .putString("equipped_armor", state.equippedArmor)
            .putString("equipped_accessory", state.equippedAccessory)
            .putStringSet("unlocked_areas", state.unlockedAreas)
            .putInt("active_quest_id", state.activeQuestId)
            .putInt("active_quest_progress", state.activeQuestProgress)
            .putStringSet("completed_quest_ids", state.completedQuestIds)
            .putBoolean("quest_boss_defeated", state.questBossDefeated)
            .putInt("quest_slimes_defeated", state.questSlimesDefeated)
            .putInt("quest_crystals_collected", state.questCrystalsCollected)
            .putInt("quest_chests_opened", state.questChestsOpened)
            .putFloat("game_time", state.gameTime)

        // Save side quest progress keys
        for ((qId, prog) in state.sideQuestProgressMap) {
            editor.putInt("sq_prog_$qId", prog)
        }

        editor.apply()
    }

    // Load complete game state locally from device storage
    fun loadGameState(): PlayerSaveState {
        if (!hasSavedGame) return PlayerSaveState()

        val acceptedSideQuests = prefs.getStringSet("accepted_side_quests", emptySet()) ?: emptySet()
        val sideQuestProgressMap = mutableMapOf<String, Int>()
        for (qId in acceptedSideQuests) {
            sideQuestProgressMap[qId] = prefs.getInt("sq_prog_$qId", 0)
        }

        return PlayerSaveState(
            hasSavedGame = true,
            posX = prefs.getFloat("player_pos_x", 80f),
            posY = prefs.getFloat("player_pos_y", 400f),
            checkpointName = prefs.getString("checkpoint_name", "Forest Entrance") ?: "Forest Entrance",
            level = prefs.getInt("player_level", 1),
            xp = prefs.getInt("player_xp", 0),
            xpToNextLevel = prefs.getInt("player_xp_next", 100),
            health = prefs.getInt("player_health", 100),
            maxHealth = prefs.getInt("player_max_health", 100),
            bonusDamage = prefs.getInt("player_bonus_damage", 0),
            coins = prefs.getInt("player_coins", 0),
            crystals = prefs.getInt("player_crystals", 0),
            score = prefs.getInt("player_score", 0),
            potions = prefs.getInt("player_potions", 2),
            citadelKeysCount = prefs.getInt("citadel_keys_count", 0),
            hasCitadelKey = prefs.getBoolean("has_citadel_key", false),
            hasAncientRelic = prefs.getBoolean("has_ancient_relic", false),
            hasHeartFragment1 = prefs.getBoolean("has_heart_frag_1", false),
            hasHeartFragment2 = prefs.getBoolean("has_heart_frag_2", false),
            hasHeartFragment3 = prefs.getBoolean("has_heart_frag_3", false),
            hasMasterKey = prefs.getBoolean("has_master_key", false),
            hasBrassCompass = prefs.getBoolean("has_brass_compass", false),
            hasMapParchment = prefs.getBoolean("has_map_parchment", false),
            runicOreCount = prefs.getInt("runic_ore_count", 0),
            hasGuardBadge = prefs.getBoolean("has_guard_badge", false),
            secretsFoundCount = prefs.getInt("secrets_found_count", 0),
            solvedPuzzles = prefs.getStringSet("solved_puzzles", emptySet()) ?: emptySet(),
            openedChests = prefs.getStringSet("opened_chests", emptySet()) ?: emptySet(),
            defeatedBosses = prefs.getStringSet("defeated_bosses", emptySet()) ?: emptySet(),
            collectedSecretRelics = prefs.getStringSet("collected_secret_relics", emptySet()) ?: emptySet(),
            acceptedSideQuestIds = acceptedSideQuests,
            completedSideQuestIds = prefs.getStringSet("completed_side_quests", emptySet()) ?: emptySet(),
            sideQuestProgressMap = sideQuestProgressMap,
            equippedWeapon = prefs.getString("equipped_weapon", "Steel Broadsword") ?: "Steel Broadsword",
            equippedArmor = prefs.getString("equipped_armor", "Adventurer Tunic") ?: "Adventurer Tunic",
            equippedAccessory = prefs.getString("equipped_accessory", "Warrior Ring") ?: "Warrior Ring",
            unlockedAreas = prefs.getStringSet("unlocked_areas", setOf("The Whispering Woods")) ?: setOf("The Whispering Woods"),
            activeQuestId = prefs.getInt("active_quest_id", 1),
            activeQuestProgress = prefs.getInt("active_quest_progress", 0),
            completedQuestIds = prefs.getStringSet("completed_quest_ids", emptySet()) ?: emptySet(),
            questBossDefeated = prefs.getBoolean("quest_boss_defeated", false),
            questSlimesDefeated = prefs.getInt("quest_slimes_defeated", 0),
            questCrystalsCollected = prefs.getInt("quest_crystals_collected", 0),
            questChestsOpened = prefs.getInt("quest_chests_opened", 0),
            gameTime = prefs.getFloat("game_time", 0f)
        )
    }

    fun clearSavedGame() {
        prefs.edit()
            .putBoolean("has_saved_game", false)
            .remove("player_pos_x")
            .remove("player_pos_y")
            .remove("checkpoint_name")
            .apply()
    }

    fun recordGameFinished(score: Int, coins: Int, timeSec: Float) {
        if (score > highScore) {
            highScore = score
        }
        totalCoinsCollected += coins
        if (timeSec < bestTimeSeconds) {
            bestTimeSeconds = timeSec
        }
    }
}
