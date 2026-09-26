package com.example.game.engine

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.game.audio.FantasySoundEngine
import com.example.game.audio.SoundManager
import com.example.game.effects.StatusEffect
import com.example.game.effects.StatusEffectType
import com.example.game.entities.Collectible
import com.example.game.entities.CollectibleType
import com.example.game.entities.DialogueLine
import com.example.game.entities.Enemy
import com.example.game.entities.EnemyType
import com.example.game.entities.NPC
import com.example.game.entities.NPCType
import com.example.game.entities.Particle
import com.example.game.entities.ParticleType
import com.example.game.entities.Player
import com.example.game.entities.PlayerState
import com.example.game.model.GameScreen
import com.example.game.model.Vector2D
import com.example.game.puzzles.LeverSwitch
import com.example.game.quest.Quest
import com.example.game.quest.QuestManager
import com.example.game.quest.SideQuest
import com.example.game.quest.SideQuestManager
import com.example.game.save.PlayerSaveState
import com.example.game.save.SaveManager
import com.example.game.world.WorldMap
import kotlin.math.hypot
import kotlin.random.Random

class GameEngine(context: Context) {
    val soundManager = SoundManager()
    val soundEngine: SoundManager get() = soundManager
    val saveManager = SaveManager(context)

    var currentScreen = GameScreen.TITLE
    var world = WorldMap()
    val physics = PhysicsEngine(world)
    val combat = CombatEngine(soundEngine)

    var player = Player(80f, 400f)
    val enemies = mutableListOf<Enemy>()
    val collectibles = mutableListOf<Collectible>()
    val particles = mutableListOf<Particle>()
    val npcs = mutableListOf<NPC>()
    val questManager = QuestManager()
    val sideQuestManager = SideQuestManager()

    // Camera Follow System
    val camera = CameraFollowSystem(world, 800f, 450f)
    val cameraPos: Vector2D = camera.renderPosition
    var screenWidth: Float
        get() = camera.viewportWidth
        set(value) {
            camera.updateViewport(value, camera.viewportHeight)
        }
    var screenHeight: Float
        get() = camera.viewportHeight
        set(value) {
            camera.updateViewport(camera.viewportWidth, value)
        }
    var screenShakeAmount: Float
        get() = camera.shakeIntensity
        set(value) {
            camera.shakeIntensity = value
        }

    // Targeted Enemy for HUD Banner
    var targetedEnemy: Enemy? = null

    // Area Banner
    var currentAreaName = "The Whispering Woods"
    var areaBannerTimer = 3f

    // Interactive Dialog & Lore System
    var activeDialogue: List<DialogueLine>? = null
    var activeDialogueIndex = 0
    var activeLoreText: String? = null

    // Quest Completion Notification Banner
    var questCompletionBanner: String? = null
    var questCompletionTimer = 0f

    // Proximity helpers for HUD prompt
    var nearbyNPC: NPC? = null
    var nearbyLore: Collectible? = null
    var nearbyChest: Collectible? = null
    var nearbyLever: LeverSwitch? = null

    // Game stats
    var gameTime = 0f
    var lastActiveCheckpointName = "Forest Entrance"

    // Input state
    var inputX = 0f
    var inputY = 0f

    init {
        soundEngine.isMuted = !saveManager.isSoundEnabled
        startNewGame()
    }

    fun startNewGame() {
        world = WorldMap()
        player = Player(80f, 400f)
        questManager.quests.clear()
        val freshQuestManager = QuestManager()
        questManager.quests.addAll(freshQuestManager.quests)
        questManager.activeQuestIndex = 0

        sideQuestManager.restoreState(emptySet(), emptySet(), emptyMap())

        enemies.clear()
        for (e in world.initialEnemies) {
            enemies.add(
                Enemy(
                    id = e.id,
                    type = e.type,
                    startX = e.startX,
                    startY = e.startY,
                    patrolMinX = e.patrolMinX,
                    patrolMaxX = e.patrolMaxX
                )
            )
        }

        collectibles.clear()
        for (c in world.initialCollectibles) {
            collectibles.add(
                Collectible(
                    id = c.id,
                    pos = Vector2D(c.pos.x, c.pos.y),
                    type = c.type,
                    value = c.value,
                    extraData = c.extraData
                )
            )
        }

        npcs.clear()
        for (npc in world.initialNPCs) {
            npcs.add(
                NPC(
                    id = npc.id,
                    type = npc.type,
                    pos = Vector2D(npc.pos.x, npc.pos.y)
                )
            )
        }

        particles.clear()
        activeDialogue = null
        activeLoreText = null
        questCompletionBanner = null
        questCompletionTimer = 0f

        gameTime = 0f
        currentAreaName = "The Whispering Woods"
        areaBannerTimer = 3.5f
        lastActiveCheckpointName = "Forest Entrance"
        currentScreen = GameScreen.PLAYING
        camera.world = world
        camera.snapToPlayer(player)

        // Auto-save fresh game locally
        saveCurrentProgress()
    }

    fun continueSavedGame(): Boolean {
        if (!saveManager.hasSavedGame) return false

        val state = saveManager.loadGameState()
        world = WorldMap()
        player = Player(state.posX, state.posY)
        player.level = state.level
        player.xp = state.xp
        player.xpToNextLevel = state.xpToNextLevel
        player.bonusMaxHealth = (state.maxHealth - 100).coerceAtLeast(0)
        player.health = state.health.coerceIn(1, player.maxHealth)
        player.bonusDamage = state.bonusDamage
        player.coins = state.coins
        player.crystals = state.crystals
        player.score = state.score
        player.potions = state.potions
        player.citadelKeysCount = state.citadelKeysCount
        player.hasAncientRelic = state.hasAncientRelic
        player.hasHeartFragment1 = state.hasHeartFragment1
        player.hasHeartFragment2 = state.hasHeartFragment2
        player.hasHeartFragment3 = state.hasHeartFragment3
        player.hasMasterKey = state.hasMasterKey
        player.hasBrassCompass = state.hasBrassCompass
        player.hasMapParchment = state.hasMapParchment
        player.runicOreCount = state.runicOreCount
        player.hasGuardBadge = state.hasGuardBadge
        player.secretsFoundCount = state.secretsFoundCount
        player.solvedPuzzles.clear()
        player.solvedPuzzles.addAll(state.solvedPuzzles)
        player.openedChests.clear()
        player.openedChests.addAll(state.openedChests)
        player.defeatedBosses.clear()
        player.defeatedBosses.addAll(state.defeatedBosses)
        player.collectedSecretRelics.clear()
        player.collectedSecretRelics.addAll(state.collectedSecretRelics)
        player.acceptedSideQuestIds.clear()
        player.acceptedSideQuestIds.addAll(state.acceptedSideQuestIds)
        player.completedSideQuestIds.clear()
        player.completedSideQuestIds.addAll(state.completedSideQuestIds)

        player.equippedWeapon = state.equippedWeapon
        player.equippedArmor = state.equippedArmor
        player.equippedAccessory = state.equippedAccessory
        player.unlockedAreas.clear()
        player.unlockedAreas.addAll(state.unlockedAreas)
        player.activeQuestId = state.activeQuestId
        player.activeQuestProgress = state.activeQuestProgress
        player.completedQuestIds.clear()
        for (qStr in state.completedQuestIds) {
            qStr.toIntOrNull()?.let { player.completedQuestIds.add(it) }
        }
        player.questBossDefeated = state.questBossDefeated
        player.questSlimesDefeated = state.questSlimesDefeated
        player.questCrystalsCollected = state.questCrystalsCollected
        player.questChestsOpened = state.questChestsOpened
        player.setCheckpoint(state.posX, state.posY)

        questManager.restoreState(
            completedQuestIds = player.completedQuestIds.toList(),
            activeId = player.activeQuestId,
            progress = player.activeQuestProgress
        )

        sideQuestManager.restoreState(
            accepted = player.acceptedSideQuestIds,
            completed = player.completedSideQuestIds,
            progressMap = state.sideQuestProgressMap
        )

        // Restore puzzle and chest state in world
        for (gate in world.lockedGates) {
            if (player.solvedPuzzles.contains(gate.id)) {
                gate.open()
            }
        }
        for (torch in world.runicTorches) {
            if (player.solvedPuzzles.contains(torch.id)) {
                torch.isLit = true
            }
        }
        for (wall in world.crumbleWalls) {
            if (player.solvedPuzzles.contains(wall.id)) {
                wall.isBroken = true
                world.setTile(wall.row, wall.col, com.example.game.world.TileType.AIR)
            }
        }

        enemies.clear()
        for (e in world.initialEnemies) {
            if (!player.defeatedBosses.contains(e.id)) {
                enemies.add(
                    Enemy(
                        id = e.id,
                        type = e.type,
                        startX = e.startX,
                        startY = e.startY,
                        patrolMinX = e.patrolMinX,
                        patrolMaxX = e.patrolMaxX
                    )
                )
            }
        }

        collectibles.clear()
        for (c in world.initialCollectibles) {
            val collectible = Collectible(
                id = c.id,
                pos = Vector2D(c.pos.x, c.pos.y),
                type = c.type,
                value = c.value,
                extraData = c.extraData
            )
            if (player.openedChests.contains(c.id)) {
                collectible.isOpened = true
                collectible.isCollected = true
            }
            if (player.collectedSecretRelics.contains(c.id)) {
                collectible.isCollected = true
            }
            collectibles.add(collectible)
        }

        npcs.clear()
        for (npc in world.initialNPCs) {
            npcs.add(
                NPC(
                    id = npc.id,
                    type = npc.type,
                    pos = Vector2D(npc.pos.x, npc.pos.y)
                )
            )
        }

        particles.clear()
        gameTime = state.gameTime
        lastActiveCheckpointName = state.checkpointName
        currentAreaName = getAreaNameForCol((state.posX / world.tileSize).toInt())
        areaBannerTimer = 3.0f
        currentScreen = GameScreen.PLAYING
        camera.world = world
        camera.snapToPlayer(player)
        return true
    }

    fun saveCurrentProgress() {
        val sideQuestProgressMap = mutableMapOf<String, Int>()
        for (sq in sideQuestManager.sideQuests) {
            sideQuestProgressMap[sq.id] = sq.currentProgress
        }

        val saveState = PlayerSaveState(
            hasSavedGame = true,
            posX = player.respawnPos.x,
            posY = player.respawnPos.y,
            checkpointName = lastActiveCheckpointName,
            level = player.level,
            xp = player.xp,
            xpToNextLevel = player.xpToNextLevel,
            health = player.health,
            maxHealth = player.maxHealth,
            bonusDamage = player.bonusDamage,
            coins = player.coins,
            crystals = player.crystals,
            score = player.score,
            potions = player.potions,
            citadelKeysCount = player.citadelKeysCount,
            hasCitadelKey = player.hasCitadelKey,
            hasAncientRelic = player.hasAncientRelic,
            hasHeartFragment1 = player.hasHeartFragment1,
            hasHeartFragment2 = player.hasHeartFragment2,
            hasHeartFragment3 = player.hasHeartFragment3,
            hasMasterKey = player.hasMasterKey,
            hasBrassCompass = player.hasBrassCompass,
            hasMapParchment = player.hasMapParchment,
            runicOreCount = player.runicOreCount,
            hasGuardBadge = player.hasGuardBadge,
            secretsFoundCount = player.secretsFoundCount,
            solvedPuzzles = player.solvedPuzzles.toSet(),
            openedChests = player.openedChests.toSet(),
            defeatedBosses = player.defeatedBosses.toSet(),
            collectedSecretRelics = player.collectedSecretRelics.toSet(),
            acceptedSideQuestIds = player.acceptedSideQuestIds.toSet(),
            completedSideQuestIds = player.completedSideQuestIds.toSet(),
            sideQuestProgressMap = sideQuestProgressMap,
            equippedWeapon = player.equippedWeapon,
            equippedArmor = player.equippedArmor,
            equippedAccessory = player.equippedAccessory,
            unlockedAreas = player.unlockedAreas.toSet(),
            activeQuestId = questManager.currentQuest?.id ?: 1,
            activeQuestProgress = questManager.currentQuest?.currentProgress ?: 0,
            completedQuestIds = player.completedQuestIds.map { it.toString() }.toSet(),
            questBossDefeated = player.questBossDefeated,
            questSlimesDefeated = player.questSlimesDefeated,
            questCrystalsCollected = player.questCrystalsCollected,
            questChestsOpened = player.questChestsOpened,
            gameTime = gameTime
        )
        saveManager.saveGameState(saveState)
    }

    private fun getAreaNameForCol(col: Int): String {
        return when {
            col < 40 -> "The Whispering Woods"
            col < 80 -> "Oakhaven Hamlet"
            col < 120 -> "Deeproot Caverns"
            col < 160 -> "Ancient Forgotten Ruins"
            col < 200 -> "Frostpeak Summit"
            col < 240 -> "The Hidden Lunar Shrine"
            col < 280 -> "Stormgate Castle Keep"
            else -> "Throne of the Ruin Colossus"
        }
    }

    fun usePotion(): Boolean {
        if (currentScreen == GameScreen.PLAYING) {
            val used = player.usePotion()
            if (used) {
                soundEngine.playCrystal()
                spawnPickupSparkles(player.pos, Color(0xFFFF3366))
                particles.add(
                    Particle(
                        pos = Vector2D(player.pos.x + player.width * 0.5f - 10f, player.pos.y - 15f),
                        velocity = Vector2D(0f, -60f),
                        life = 0.8f,
                        maxLife = 0.8f,
                        color = Color(0xFF69F0AE),
                        size = 14f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "+45 HP"
                    )
                )
                return true
            }
        }
        return false
    }

    fun useSwiftnessPotion(): Boolean {
        if (currentScreen == GameScreen.PLAYING) {
            val used = player.useSwiftnessPotion()
            if (used) {
                soundManager.onSpeedBuffApplied()
                for (i in 0..8) {
                    particles.add(
                        Particle(
                            pos = Vector2D(player.pos.x + player.width * 0.5f, player.pos.y + player.height * 0.5f),
                            velocity = Vector2D(Random.nextFloat() * 160f - 80f, Random.nextFloat() * -100f - 20f),
                            life = 0.5f,
                            maxLife = 0.5f,
                            color = Color(0xFF00E5FF),
                            size = 5f,
                            type = ParticleType.SPEED_STREAK
                        )
                    )
                }
                particles.add(
                    Particle(
                        pos = Vector2D(player.pos.x + player.width * 0.5f - 24f, player.pos.y - 18f),
                        velocity = Vector2D(0f, -60f),
                        life = 0.85f,
                        maxLife = 0.85f,
                        color = Color(0xFF00E5FF),
                        size = 14f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "HASTE +45%!"
                    )
                )
                return true
            }
        }
        return false
    }

    fun applyStatusEffect(effect: StatusEffect) {
        player.applyStatusEffect(effect)
        if (effect.type == StatusEffectType.SPEED_BUFF) {
            soundManager.onSpeedBuffApplied()
        }
    }

    fun toggleTestStatusEffect(type: StatusEffectType) {
        if (player.hasStatusEffect(type)) {
            player.removeStatusEffect(type)
        } else {
            when (type) {
                StatusEffectType.POISON -> applyStatusEffect(StatusEffect.poison())
                StatusEffectType.BURNING -> applyStatusEffect(StatusEffect.burning())
                StatusEffectType.SPEED_BUFF -> applyStatusEffect(StatusEffect.speedBuff())
                StatusEffectType.REGENERATION -> applyStatusEffect(StatusEffect.regeneration())
                StatusEffectType.FROST_CHILL -> applyStatusEffect(StatusEffect.frostChill())
                StatusEffectType.STRENGTH_BUFF -> applyStatusEffect(StatusEffect.strengthBuff())
            }
        }
    }

    fun testApplyStatusEffect(type: StatusEffectType, duration: Float = 6f) {
        val effect = when (type) {
            StatusEffectType.POISON -> StatusEffect.poison(duration = duration)
            StatusEffectType.BURNING -> StatusEffect.burning(duration = duration)
            StatusEffectType.SPEED_BUFF -> StatusEffect.speedBuff(duration = duration)
            StatusEffectType.REGENERATION -> StatusEffect.regeneration(duration = duration)
            StatusEffectType.FROST_CHILL -> StatusEffect.frostChill(duration = duration)
            StatusEffectType.STRENGTH_BUFF -> StatusEffect.strengthBuff(duration = duration)
        }
        applyStatusEffect(effect)
    }

    fun testClearAllStatusEffects() {
        player.statusEffects.clear()
    }

    fun interactWithNearby(): Boolean {
        // 1. Advance active dialogue if open
        val dialog = activeDialogue
        if (dialog != null) {
            if (activeDialogueIndex < dialog.size - 1) {
                activeDialogueIndex++
                soundEngine.playMenuClick()
            } else {
                // Closed dialogue
                activeDialogue = null
                activeDialogueIndex = 0
                soundEngine.playMenuClick()
            }
            return true
        }

        // 2. Dismiss active Lore Tablet
        if (activeLoreText != null) {
            activeLoreText = null
            soundEngine.playMenuClick()
            return true
        }

        // 3. Interact with nearby Lever
        val lever = nearbyLever
        if (lever != null) {
            val toggled = lever.toggle()
            soundEngine.playChestOpen()
            triggerScreenShake(6f)

            // Open or toggle target gate
            val gate = world.lockedGates.find { it.id == lever.targetGateId }
            if (gate != null) {
                if (lever.isOn) {
                    gate.open()
                    player.solvedPuzzles.add(gate.id)
                } else {
                    gate.close()
                }
            }

            particles.add(
                Particle(
                    pos = Vector2D(lever.pos.x - 10f, lever.pos.y - 15f),
                    velocity = Vector2D(0f, -40f),
                    life = 1.2f,
                    maxLife = 1.2f,
                    color = if (lever.isOn) Color(0xFF00E676) else Color(0xFFFF5252),
                    size = 13f,
                    type = ParticleType.DAMAGE_TEXT,
                    text = if (lever.isOn) "GATE UNLOCKED!" else "GATE CLOSED"
                )
            )
            saveCurrentProgress()
            return true
        }

        // 4. Interact with nearby NPC (Main quests & Side quests)
        val npc = nearbyNPC
        if (npc != null) {
            handleNPCInteraction(npc)
            return true
        }

        // 5. Interact with nearby Lore Tablet
        val lore = nearbyLore
        if (lore != null) {
            activeLoreText = lore.extraData.ifEmpty { "An ancient tablet inscribed with forgotten royal runes." }
            soundEngine.playCrystal()
            spawnPickupSparkles(lore.pos, Color(0xFF00E5FF))
            player.readInscriptions.add(lore.id)
            return true
        }

        // 6. Open nearby closed chest
        val chest = nearbyChest
        if (chest != null && !chest.isOpened) {
            handleItemCollection(chest)
            return true
        }

        return false
    }

    private fun handleNPCInteraction(npc: NPC) {
        // Check if this NPC has a side quest to turn in
        val sideQuest = sideQuestManager.getSideQuestForNPC(npc.type)
        if (sideQuest != null && sideQuest.isAccepted && !sideQuest.isCompleted) {
            // Check if objective is fulfilled
            val isReady = when (sideQuest.id) {
                "sq_lost_traveler" -> player.hasBrassCompass
                "sq_village_supplies" -> player.crystals >= 3
                "sq_hidden_treasure" -> player.hasMapParchment
                "sq_forgotten_cave" -> player.solvedPuzzles.contains("lever_cavern_secret")
                "sq_old_map" -> player.hasMapParchment
                "sq_missing_guard" -> player.hasGuardBadge
                "sq_forest_beast" -> player.defeatedBosses.contains("boss_dreadfang_wolf")
                "sq_ancient_key" -> player.hasMasterKey
                "sq_blacksmith_request" -> player.runicOreCount >= 3
                "sq_secret_shrine" -> player.hasHeartFragment2
                "sq_glacial_menace" -> player.defeatedBosses.contains("boss_frost_wurm")
                "sq_phantom_king" -> player.defeatedBosses.contains("boss_shadow_lich")
                else -> false
            }

            if (isReady) {
                // Complete side quest!
                sideQuestManager.completeSideQuest(sideQuest.id, player)
                player.completedSideQuestIds.add(sideQuest.id)
                grantSideQuestRewards(sideQuest)
                activeDialogue = listOf(
                    DialogueLine(npc.displayName, "[${npc.title}] Incredible! You have completed my request: ${sideQuest.title}."),
                    DialogueLine(npc.displayName, "[${npc.title}] Take this reward with my utmost gratitude, brave adventurer!")
                )
                activeDialogueIndex = 0
                soundEngine.playVictory()
                questCompletionBanner = "SIDE QUEST COMPLETE: ${sideQuest.title.uppercase()}"
                questCompletionTimer = 4.0f
                saveCurrentProgress()
                return
            }
        }

        // If side quest not accepted yet, offer it!
        if (sideQuest != null && !sideQuest.isAccepted && !sideQuest.isCompleted) {
            sideQuestManager.acceptSideQuest(sideQuest.id)
            player.acceptedSideQuestIds.add(sideQuest.id)
            activeDialogue = listOf(
                DialogueLine(npc.displayName, "[${npc.title}] Greetings traveler! I have a task for you: '${sideQuest.title}'."),
                DialogueLine(npc.displayName, sideQuest.description),
                DialogueLine(npc.displayName, "Objective: ${sideQuest.objectiveText}")
            )
            activeDialogueIndex = 0
            soundEngine.playMenuClick()
            questCompletionBanner = "NEW QUEST: ${sideQuest.title.uppercase()}"
            questCompletionTimer = 3.5f
            saveCurrentProgress()
            return
        }

        // Standard Main Story Dialogue
        val lines = npc.getDialogue(
            questId = questManager.currentQuest?.id ?: 1,
            playerHasKeys = player.citadelKeysCount,
            bossDefeated = player.questBossDefeated
        )
        activeDialogue = lines
        activeDialogueIndex = 0
        soundEngine.playMenuClick()

        // Main Quest check
        if (npc.type == NPCType.VILLAGE_ELDER && questManager.currentQuest?.id == 1) {
            onQuestObjectiveAchieved(1)
        }
        if (npc.type == NPCType.EXPLORER && questManager.currentQuest?.id == 4) {
            onQuestObjectiveAchieved(4)
        }
    }

    private fun grantSideQuestRewards(sideQuest: SideQuest) {
        val reward = sideQuest.reward
        player.coins += reward.coins
        player.score += reward.xp * 4
        player.bonusMaxHealth += reward.maxHealthBonus
        player.bonusDamage += reward.damageBonus
        player.potions += reward.potions
        player.heal(player.maxHealth)

        val leveledUp = player.addXP(reward.xp)
        if (leveledUp) {
            soundEngine.playLevelUp()
            triggerScreenShake(10f)
            particles.add(
                Particle(
                    pos = Vector2D(player.pos.x + player.width * 0.5f - 24f, player.pos.y - 35f),
                    velocity = Vector2D(0f, -60f),
                    life = 1.6f,
                    maxLife = 1.6f,
                    color = Color(0xFFFFD700),
                    size = 16f,
                    type = ParticleType.DAMAGE_TEXT,
                    text = "LEVEL UP! Lv.${player.level}"
                )
            )
        }
    }

    fun onQuestObjectiveAchieved(questId: Int, count: Int = 1) {
        val currentQ = questManager.currentQuest ?: return
        if (currentQ.id == questId) {
            val completed = questManager.updateProgress(questId, count)
            player.activeQuestId = questManager.currentQuest?.id ?: questId
            player.activeQuestProgress = questManager.currentQuest?.currentProgress ?: 0

            if (completed) {
                player.completedQuestIds.add(questId)
                grantQuestRewards(currentQ)
                soundEngine.playVictory()
                questCompletionBanner = "MISSION COMPLETED: ${currentQ.title.uppercase()}"
                questCompletionTimer = 4.0f
                saveCurrentProgress()
            }
        }
    }

    private fun grantQuestRewards(quest: Quest) {
        val reward = quest.reward
        player.coins += reward.coins
        player.score += reward.xp * 5
        player.bonusMaxHealth += reward.maxHealthBonus
        player.bonusDamage += reward.damageBonus
        player.potions += reward.potions
        player.health = player.maxHealth // Full heal on mission completion!

        val leveledUp = player.addXP(reward.xp)
        if (leveledUp) {
            soundEngine.playLevelUp()
            triggerScreenShake(10f)
            particles.add(
                Particle(
                    pos = Vector2D(player.pos.x + player.width * 0.5f - 24f, player.pos.y - 35f),
                    velocity = Vector2D(0f, -60f),
                    life = 1.6f,
                    maxLife = 1.6f,
                    color = Color(0xFFFFD700),
                    size = 16f,
                    type = ParticleType.DAMAGE_TEXT,
                    text = "LEVEL UP! Lv.${player.level}"
                )
            )
        }
    }

    fun update(dt: Float) {
        if (currentScreen != GameScreen.PLAYING) return

        val clampedDt = dt.coerceAtMost(0.05f)
        gameTime += clampedDt

        // 0. Update Sound Manager (terrain-dependent footfalls & area ambient tracks for forest / cave)
        soundManager.update(player, world, currentAreaName, clampedDt)

        // 1. Update Area Notification
        val playerCol = (player.pos.x / world.tileSize).toInt()
        val newArea = getAreaNameForCol(playerCol)
        if (newArea != currentAreaName) {
            currentAreaName = newArea
            areaBannerTimer = 3.2f
            player.unlockedAreas.add(newArea)
        }
        if (areaBannerTimer > 0f) {
            areaBannerTimer -= clampedDt
        }

        // Quest Completion Banner Timer
        if (questCompletionTimer > 0f) {
            questCompletionTimer -= clampedDt
            if (questCompletionTimer <= 0f) {
                questCompletionBanner = null
            }
        }

        // 2. Update Puzzle Mechanisms
        for (plat in world.movingPlatforms) {
            plat.update(clampedDt)
        }
        for (gate in world.lockedGates) {
            gate.update(clampedDt)
        }
        for (plate in world.pressurePlates) {
            val wasPressed = plate.isPressed
            val isNowPressed = plate.checkOverlap(player.bounds)
            if (isNowPressed && !wasPressed) {
                soundEngine.playChestOpen()
                val targetGate = world.lockedGates.find { it.id == plate.targetGateId }
                targetGate?.open()
                player.solvedPuzzles.add(plate.id)
            } else if (!isNowPressed && wasPressed) {
                val targetGate = world.lockedGates.find { it.id == plate.targetGateId }
                targetGate?.close()
            }
        }

        // 3. Proximity Check for Levers
        var nearestLever: LeverSwitch? = null
        var minLeverDist = 60f
        for (lever in world.leverSwitches) {
            val dist = hypot(player.pos.x - lever.pos.x, player.pos.y - lever.pos.y)
            if (dist < minLeverDist) {
                minLeverDist = dist
                nearestLever = lever
            }
        }
        nearbyLever = nearestLever

        // 4. Check Automatic Mission Progress based on Player Position
        checkAutomaticQuestProgress(playerCol)

        // 5. Update Player Physics & State
        physics.updatePlayer(
            player = player,
            inputX = inputX,
            inputY = inputY,
            dt = clampedDt,
            onHitHazard = {
                val tookDmg = player.takeDamage(20, if (player.facingRight) -180f else 180f)
                if (tookDmg) {
                    soundEngine.playHit()
                    triggerScreenShake(12f)
                    if (player.health <= 0) {
                        soundEngine.playGameOver()
                        currentScreen = GameScreen.GAME_OVER
                    }
                }
            },
            onJumpTriggered = {
                soundEngine.playJump()
            }
        )
        player.update(clampedDt)

        // 6b. Status Effect Processing & Environmental Interactions
        if (physics.checkWaterOverlap(player.bounds) && player.hasStatusEffect(StatusEffectType.BURNING)) {
            player.removeStatusEffect(StatusEffectType.BURNING)
            soundManager.onExtinguish()
            // Sizzling steam puff particles
            for (i in 0..8) {
                particles.add(
                    Particle(
                        pos = Vector2D(player.pos.x + player.width * 0.5f, player.pos.y + player.height * 0.5f),
                        velocity = Vector2D(Random.nextFloat() * 100f - 50f, Random.nextFloat() * -90f - 20f),
                        life = 0.6f,
                        maxLife = 0.6f,
                        color = Color(0xCCECEFF1),
                        size = 6f,
                        type = ParticleType.DEATH_POOF
                    )
                )
            }
        }

        player.statusEffects.update(
            dt = clampedDt,
            onTick = { effect, value ->
                if (player.state != PlayerState.DEAD) {
                    when (effect.type) {
                        StatusEffectType.POISON -> {
                            soundManager.onPoisonTick()
                            val died = player.takeStatusDamage(value)
                            triggerScreenShake(2.5f)
                            for (i in 0..4) {
                                particles.add(
                                    Particle(
                                        pos = Vector2D(
                                            player.pos.x + Random.nextFloat() * player.width,
                                            player.pos.y + Random.nextFloat() * player.height
                                        ),
                                        velocity = Vector2D(Random.nextFloat() * 40f - 20f, -50f - Random.nextFloat() * 30f),
                                        life = 0.55f,
                                        maxLife = 0.55f,
                                        color = Color(0xFF00E676),
                                        size = 5f,
                                        type = ParticleType.POISON_BUBBLE
                                    )
                                )
                            }
                            particles.add(
                                Particle(
                                    pos = Vector2D(player.pos.x + player.width * 0.5f - 8f, player.pos.y - 14f),
                                    velocity = Vector2D(0f, -60f),
                                    life = 0.75f,
                                    maxLife = 0.75f,
                                    color = Color(0xFF00E676),
                                    size = 14f,
                                    type = ParticleType.DAMAGE_TEXT,
                                    text = "-$value"
                                )
                            )
                            if (died) {
                                soundEngine.playGameOver()
                                currentScreen = GameScreen.GAME_OVER
                            }
                        }
                        StatusEffectType.BURNING -> {
                            soundManager.onBurnTick()
                            val died = player.takeStatusDamage(value)
                            triggerScreenShake(3.5f)
                            for (i in 0..5) {
                                particles.add(
                                    Particle(
                                        pos = Vector2D(
                                            player.pos.x + Random.nextFloat() * player.width,
                                            player.pos.y + player.height * 0.7f
                                        ),
                                        velocity = Vector2D(Random.nextFloat() * 50f - 25f, -70f - Random.nextFloat() * 40f),
                                        life = 0.5f,
                                        maxLife = 0.5f,
                                        color = if (i % 2 == 0) Color(0xFFFF3D00) else Color(0xFFFFD600),
                                        size = 4.5f,
                                        type = ParticleType.FIRE_EMBER
                                    )
                                )
                            }
                            particles.add(
                                Particle(
                                    pos = Vector2D(player.pos.x + player.width * 0.5f - 8f, player.pos.y - 14f),
                                    velocity = Vector2D(0f, -65f),
                                    life = 0.75f,
                                    maxLife = 0.75f,
                                    color = Color(0xFFFF5722),
                                    size = 14f,
                                    type = ParticleType.DAMAGE_TEXT,
                                    text = "-$value"
                                )
                            )
                            if (died) {
                                soundEngine.playGameOver()
                                currentScreen = GameScreen.GAME_OVER
                            }
                        }
                        StatusEffectType.REGENERATION -> {
                            soundManager.onHealTick()
                            player.heal(value)
                            for (i in 0..3) {
                                particles.add(
                                    Particle(
                                        pos = Vector2D(
                                            player.pos.x + Random.nextFloat() * player.width,
                                            player.pos.y + player.height * 0.8f
                                        ),
                                        velocity = Vector2D(Random.nextFloat() * 30f - 15f, -50f),
                                        life = 0.65f,
                                        maxLife = 0.65f,
                                        color = Color(0xFF69F0AE),
                                        size = 5f,
                                        type = ParticleType.HEAL_SPARKLE
                                    )
                                )
                            }
                            particles.add(
                                Particle(
                                    pos = Vector2D(player.pos.x + player.width * 0.5f - 8f, player.pos.y - 14f),
                                    velocity = Vector2D(0f, -55f),
                                    life = 0.75f,
                                    maxLife = 0.75f,
                                    color = Color(0xFF69F0AE),
                                    size = 13f,
                                    type = ParticleType.DAMAGE_TEXT,
                                    text = "+$value"
                                )
                            )
                        }
                        else -> {}
                    }
                }
            }
        )

        // Subtle ambient status effect emission particles
        if (player.hasStatusEffect(StatusEffectType.SPEED_BUFF) && player.state == PlayerState.RUN && Random.nextFloat() < 0.35f) {
            particles.add(
                Particle(
                    pos = Vector2D(
                        player.pos.x + (if (player.facingRight) 0f else player.width),
                        player.pos.y + player.height * 0.5f + Random.nextFloat() * 12f
                    ),
                    velocity = Vector2D(if (player.facingRight) -60f else 60f, -10f),
                    life = 0.28f,
                    maxLife = 0.28f,
                    color = Color(0xFF00E5FF),
                    size = 4f,
                    type = ParticleType.SPEED_STREAK
                )
            )
        }

        // Out of bounds check
        if (player.pos.y > world.height + 50f && player.state != PlayerState.DEAD) {
            player.takeDamage(999, 0f)
            soundEngine.playGameOver()
            currentScreen = GameScreen.GAME_OVER
        }

        // 7. Update NPCs & Proximity
        var nearestNpc: NPC? = null
        var minNpcDist = 80f
        for (npc in npcs) {
            npc.update(clampedDt)
            val dist = hypot(player.pos.x - npc.pos.x, player.pos.y - npc.pos.y)
            if (dist < minNpcDist) {
                minNpcDist = dist
                nearestNpc = npc
            }
        }
        nearbyNPC = nearestNpc

        // 8. Update Enemies & Target Tracking
        var closestEnemy: Enemy? = null
        var closestDist = 240f
        for (enemy in enemies) {
            if (enemy.aiState != com.example.game.entities.EnemyAIState.DEAD) {
                enemy.update(clampedDt, player)
                physics.updateEnemy(enemy, clampedDt)

                val dist = enemy.pos.distanceTo(player.pos)
                if (dist < closestDist) {
                    closestDist = dist
                    closestEnemy = enemy
                }
            }
        }
        targetedEnemy = closestEnemy

        // 9. Update Collectibles & Proximity for Lore / Chests
        var nearestLore: Collectible? = null
        var nearestChest: Collectible? = null
        for (c in collectibles) {
            if (!c.isCollected) {
                c.update(clampedDt)
                if (c.bounds.overlaps(player.bounds)) {
                    handleItemCollection(c)
                }
            }
            val dist = hypot(player.pos.x - c.pos.x, player.pos.y - c.pos.y)
            if (dist < 60f) {
                if (c.type == CollectibleType.LORE_TABLET) nearestLore = c
                if ((c.type == CollectibleType.CHEST || c.type == CollectibleType.SECRET_CHEST) && !c.isOpened) nearestChest = c
            }
        }
        nearbyLore = nearestLore
        nearbyChest = nearestChest

        // 10. Combat Updates
        combat.updateCombat(
            player = player,
            enemies = enemies,
            collectibles = collectibles,
            particles = particles,
            world = world,
            onPlayerDied = {
                currentScreen = GameScreen.GAME_OVER
            },
            onTriggerScreenShake = { intensity ->
                triggerScreenShake(intensity)
            },
            onBossDefeated = { boss ->
                when (boss.id) {
                    "boss_dreadfang_wolf" -> {
                        sideQuestManager.updateProgress("sq_forest_beast", 1)
                        questCompletionBanner = "QUEST OBJECTIVE UPDATED: Beast Defeated!"
                        questCompletionTimer = 3.5f
                    }
                    "boss_frost_wurm" -> {
                        sideQuestManager.updateProgress("sq_glacial_menace", 1)
                        questCompletionBanner = "QUEST OBJECTIVE UPDATED: Wurm Slain!"
                        questCompletionTimer = 3.5f
                    }
                    "boss_shadow_lich" -> {
                        sideQuestManager.updateProgress("sq_phantom_king", 1)
                        questCompletionBanner = "QUEST OBJECTIVE UPDATED: Lich Vanquished!"
                        questCompletionTimer = 3.5f
                    }
                }
            },
            onPuzzleTriggered = { groupId ->
                // Check if all torches in group are lit
                val groupTorches = world.runicTorches.filter { it.groupId == groupId }
                if (groupTorches.all { it.isLit }) {
                    val gate = world.lockedGates.find { it.id == "gate_$groupId" }
                    if (gate != null) {
                        gate.open()
                        soundEngine.playVictory()
                        triggerScreenShake(10f)
                        player.solvedPuzzles.add(gate.id)
                        player.solvedPuzzles.add(groupId)
                        particles.add(
                            Particle(
                                pos = Vector2D(gate.pos.x, gate.pos.y - 20f),
                                velocity = Vector2D(0f, -50f),
                                life = 2.0f,
                                maxLife = 2.0f,
                                color = Color(0xFF00E5FF),
                                size = 15f,
                                type = ParticleType.DAMAGE_TEXT,
                                text = "RUNIC SEAL UNLOCKED!"
                            )
                        )
                    }
                }
            }
        )

        // 11. Update Particles
        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            val isAlive = p.update(clampedDt)
            if (!isAlive) {
                particleIterator.remove()
            }
        }

        // 12. Update Camera
        updateCamera(clampedDt)
    }

    private fun checkAutomaticQuestProgress(playerCol: Int) {
        val currentQ = questManager.currentQuest ?: return
        when (currentQ.id) {
            1 -> {
                if (playerCol >= 42) {
                    onQuestObjectiveAchieved(1)
                }
            }
            2 -> {
                if (player.enemiesDefeated >= 4) {
                    onQuestObjectiveAchieved(2, 4)
                }
            }
            3 -> {
                if (playerCol >= 90) {
                    onQuestObjectiveAchieved(3)
                }
            }
            4 -> {
                if (playerCol >= 122) {
                    onQuestObjectiveAchieved(4)
                }
            }
            5 -> {
                val watcher = enemies.find { it.id == "ruin_watcher_stone_giant" }
                if (watcher != null && watcher.aiState == com.example.game.entities.EnemyAIState.DEAD) {
                    onQuestObjectiveAchieved(5)
                }
            }
            6 -> {
                if (playerCol >= 186) {
                    onQuestObjectiveAchieved(6)
                }
            }
            7 -> {
                if (player.hasHeartFragment1 || playerCol >= 226) {
                    onQuestObjectiveAchieved(7)
                }
            }
            8 -> {
                if (playerCol >= 244) {
                    onQuestObjectiveAchieved(8)
                }
            }
            9 -> {
                if (player.citadelKeysCount >= 3) {
                    onQuestObjectiveAchieved(9, 3)
                }
            }
            10 -> {
                if (player.questBossDefeated) {
                    onQuestObjectiveAchieved(10)
                }
            }
        }
    }

    private fun handleItemCollection(c: Collectible) {
        when (c.type) {
            CollectibleType.COIN -> {
                c.isCollected = true
                player.coins += c.value
                player.score += c.value * 10
                soundEngine.playCoin()
                spawnPickupSparkles(c.pos, Color(0xFFFFD700))
            }
            CollectibleType.MANA_CRYSTAL -> {
                c.isCollected = true
                player.crystals++
                player.questCrystalsCollected++
                player.score += c.value
                player.heal(20)
                soundEngine.playCrystal()
                spawnPickupSparkles(c.pos, Color(0xFF00E5FF))
            }
            CollectibleType.HEALTH_POTION -> {
                c.isCollected = true
                if (player.health < player.maxHealth) {
                    player.heal(c.value)
                } else {
                    player.potions++
                }
                soundEngine.playCrystal()
                spawnPickupSparkles(c.pos, Color(0xFFFF3366))
            }
            CollectibleType.ANCIENT_KEY -> {
                c.isCollected = true
                player.citadelKeysCount++
                soundEngine.playChestOpen()
                triggerScreenShake(8f)
                spawnChestBurst(c.pos)
                particles.add(
                    Particle(
                        pos = Vector2D(c.pos.x - 20f, c.pos.y - 20f),
                        velocity = Vector2D(0f, -50f),
                        life = 2.0f,
                        maxLife = 2.0f,
                        color = Color(0xFFFFD700),
                        size = 14f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "ANCIENT KEY (${player.citadelKeysCount}/3)!"
                    )
                )
                if (questManager.currentQuest?.id == 9) {
                    questManager.updateProgress(9, 1)
                    if (player.citadelKeysCount >= 3) {
                        onQuestObjectiveAchieved(9, 3)
                    }
                }
                saveCurrentProgress()
            }
            CollectibleType.HEART_FRAGMENT -> {
                c.isCollected = true
                player.hasHeartFragment1 = true
                player.bonusMaxHealth += 30
                player.health = player.maxHealth
                soundEngine.playVictory()
                triggerScreenShake(14f)
                spawnChestBurst(c.pos)
                particles.add(
                    Particle(
                        pos = Vector2D(c.pos.x - 30f, c.pos.y - 30f),
                        velocity = Vector2D(0f, -40f),
                        life = 2.8f,
                        maxLife = 2.8f,
                        color = Color(0xFF00E5FF),
                        size = 16f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "HEART OF AETHER FRAGMENT CLAIMED!"
                    )
                )
                if (questManager.currentQuest?.id == 7) {
                    onQuestObjectiveAchieved(7)
                }
                saveCurrentProgress()
            }
            CollectibleType.LORE_TABLET -> {
                // Read via interact button
            }
            CollectibleType.BRASS_COMPASS -> {
                c.isCollected = true
                player.hasBrassCompass = true
                player.collectedSecretRelics.add(c.id)
                player.secretsFoundCount++
                soundEngine.playChestOpen()
                triggerScreenShake(8f)
                spawnPickupSparkles(c.pos, Color(0xFFFFD700))
                sideQuestManager.updateProgress("sq_lost_traveler", 1)
                particles.add(
                    Particle(
                        pos = Vector2D(c.pos.x - 20f, c.pos.y - 20f),
                        velocity = Vector2D(0f, -40f),
                        life = 2.0f,
                        maxLife = 2.0f,
                        color = Color(0xFFFFD700),
                        size = 14f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "BRASS COMPASS FOUND!"
                    )
                )
                saveCurrentProgress()
            }
            CollectibleType.MAP_PARCHMENT -> {
                c.isCollected = true
                player.hasMapParchment = true
                player.collectedSecretRelics.add(c.id)
                player.secretsFoundCount++
                soundEngine.playChestOpen()
                triggerScreenShake(8f)
                spawnPickupSparkles(c.pos, Color(0xFFFFE082))
                sideQuestManager.updateProgress("sq_old_map", 1)
                particles.add(
                    Particle(
                        pos = Vector2D(c.pos.x - 20f, c.pos.y - 20f),
                        velocity = Vector2D(0f, -40f),
                        life = 2.0f,
                        maxLife = 2.0f,
                        color = Color(0xFFFFD700),
                        size = 14f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "ANCIENT MAP PARCHMENT!"
                    )
                )
                saveCurrentProgress()
            }
            CollectibleType.GUARD_BADGE -> {
                c.isCollected = true
                player.hasGuardBadge = true
                player.collectedSecretRelics.add(c.id)
                player.secretsFoundCount++
                soundEngine.playChestOpen()
                triggerScreenShake(8f)
                spawnPickupSparkles(c.pos, Color(0xFF81D4FA))
                sideQuestManager.updateProgress("sq_missing_guard", 1)
                particles.add(
                    Particle(
                        pos = Vector2D(c.pos.x - 20f, c.pos.y - 20f),
                        velocity = Vector2D(0f, -40f),
                        life = 2.0f,
                        maxLife = 2.0f,
                        color = Color(0xFF00E5FF),
                        size = 14f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "SENTRY GUARD CREST RECOVERED!"
                    )
                )
                saveCurrentProgress()
            }
            CollectibleType.RUNIC_ORE -> {
                c.isCollected = true
                player.runicOreCount++
                player.collectedSecretRelics.add(c.id)
                player.secretsFoundCount++
                soundEngine.playCrystal()
                spawnPickupSparkles(c.pos, Color(0xFF00E5FF))
                sideQuestManager.updateProgress("sq_blacksmith_request", 1)
                particles.add(
                    Particle(
                        pos = Vector2D(c.pos.x - 20f, c.pos.y - 20f),
                        velocity = Vector2D(0f, -40f),
                        life = 2.0f,
                        maxLife = 2.0f,
                        color = Color(0xFF00E5FF),
                        size = 14f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "RUNIC ORE (${player.runicOreCount}/3)!"
                    )
                )
                saveCurrentProgress()
            }
            CollectibleType.RELIC_CHALICE -> {
                c.isCollected = true
                player.collectedSecretRelics.add(c.id)
                player.hasHeartFragment2 = true
                player.bonusMaxHealth += 25
                player.secretsFoundCount++
                soundEngine.playVictory()
                triggerScreenShake(12f)
                spawnChestBurst(c.pos)
                sideQuestManager.updateProgress("sq_secret_shrine", 1)
                particles.add(
                    Particle(
                        pos = Vector2D(c.pos.x - 30f, c.pos.y - 30f),
                        velocity = Vector2D(0f, -40f),
                        life = 2.5f,
                        maxLife = 2.5f,
                        color = Color(0xFFFFD700),
                        size = 15f,
                        type = ParticleType.DAMAGE_TEXT,
                        text = "HOLY CHALICE OF AETHER CLAIMED!"
                    )
                )
                saveCurrentProgress()
            }
            CollectibleType.CHEST, CollectibleType.SECRET_CHEST -> {
                if (!c.isOpened) {
                    c.isOpened = true
                    c.isCollected = true
                    player.openedChests.add(c.id)
                    val isSecret = c.type == CollectibleType.SECRET_CHEST
                    player.coins += if (isSecret) 120 else 50
                    player.crystals += if (isSecret) 6 else 3
                    player.score += if (isSecret) 600 else 300
                    player.questChestsOpened++
                    if (isSecret) player.secretsFoundCount++
                    player.hasAncientRelic = true
                    player.potions += if (isSecret) 2 else 1

                    if (c.extraData == "MASTER_KEY") {
                        player.hasMasterKey = true
                        sideQuestManager.updateProgress("sq_ancient_key", 1)
                    }

                    soundEngine.playChestOpen()
                    triggerScreenShake(if (isSecret) 12f else 8f)
                    spawnChestBurst(c.pos)

                    particles.add(
                        Particle(
                            pos = Vector2D(c.pos.x - 25f, c.pos.y - 25f),
                            velocity = Vector2D(0f, -45f),
                            life = 2.0f,
                            maxLife = 2.0f,
                            color = if (isSecret) Color(0xFF00E5FF) else Color(0xFFFFD700),
                            size = 14f,
                            type = ParticleType.DAMAGE_TEXT,
                            text = if (isSecret) "SECRET CHEST OPENED! +120 COINS" else "TREASURE CHEST OPENED!"
                        )
                    )
                    saveCurrentProgress()
                }
            }
            CollectibleType.CHECKPOINT -> {
                if (!c.isActivated) {
                    c.isActivated = true
                    player.setCheckpoint(c.pos.x, c.pos.y - 10f)
                    player.heal(player.maxHealth)
                    lastActiveCheckpointName = when {
                        c.pos.x < 1200f -> "Forest Glade"
                        c.pos.x < 2500f -> "Village Hearth"
                        c.pos.x < 3800f -> "Deep Cavern Crystal"
                        c.pos.x < 5000f -> "Ruins Sanctum"
                        c.pos.x < 6200f -> "Frostpeak Beacon"
                        c.pos.x < 7500f -> "Lunar Shrine Altar"
                        c.pos.x < 8800f -> "Castle Gatehouse"
                        else -> "Throne Antechamber"
                    }
                    player.unlockedAreas.add(currentAreaName)
                    soundEngine.playCheckpoint()
                    spawnPickupSparkles(c.pos, Color(0xFF00E676))
                    saveCurrentProgress()

                    particles.add(
                        Particle(
                            pos = Vector2D(c.pos.x - 20f, c.pos.y - 20f),
                            velocity = Vector2D(0f, -60f),
                            life = 1.4f,
                            maxLife = 1.4f,
                            color = Color(0xFF00E676),
                            size = 14f,
                            type = ParticleType.DAMAGE_TEXT,
                            text = "CHECKPOINT: $lastActiveCheckpointName"
                        )
                    )

                    if (questManager.currentQuest?.id == 3 && c.id == "cp_cave") {
                        onQuestObjectiveAchieved(3)
                    }
                    if (questManager.currentQuest?.id == 6 && c.id == "cp_mountain") {
                        onQuestObjectiveAchieved(6)
                    }
                    if (questManager.currentQuest?.id == 8 && c.id == "cp_castle") {
                        onQuestObjectiveAchieved(8)
                    }
                }
            }
            CollectibleType.VICTORY_PORTAL -> {
                soundEngine.playVictory()
                player.questBossDefeated = true
                player.unlockedAreas.add("The Restored Kingdom")
                saveCurrentProgress()
                saveManager.recordGameFinished(player.score, player.coins, gameTime)
                currentScreen = GameScreen.VICTORY
            }
        }
    }

    private fun spawnPickupSparkles(pos: Vector2D, color: Color) {
        for (i in 0..6) {
            particles.add(
                Particle(
                    pos = Vector2D(pos.x + 10f, pos.y + 10f),
                    velocity = Vector2D(Random.nextFloat() * 120f - 60f, Random.nextFloat() * 120f - 60f),
                    life = 0.4f,
                    maxLife = 0.4f,
                    color = color,
                    size = 3.5f,
                    type = ParticleType.COIN_SPARKLE
                )
            )
        }
    }

    private fun spawnChestBurst(pos: Vector2D) {
        for (i in 0..16) {
            particles.add(
                Particle(
                    pos = Vector2D(pos.x + 18f, pos.y + 15f),
                    velocity = Vector2D(Random.nextFloat() * 200f - 100f, Random.nextFloat() * -200f - 50f),
                    life = 0.6f,
                    maxLife = 0.6f,
                    color = if (i % 2 == 0) Color(0xFFFFD700) else Color(0xFF00E5FF),
                    size = 4.5f,
                    type = ParticleType.COIN_SPARKLE
                )
            )
        }
    }

    fun triggerScreenShake(intensity: Float) {
        camera.triggerShake(intensity)
    }

    private fun updateCamera(dt: Float) {
        camera.update(player, dt)
    }

    fun onLeftPressed() { inputX = -1f }
    fun onRightPressed() { inputX = 1f }
    fun onMoveReleased() { inputX = 0f }
    fun onUpPressed() { inputY = -1f }
    fun onDownPressed() { inputY = 1f }
    fun onClimbReleased() { inputY = 0f }

    fun onJumpPressed() {
        if (currentScreen == GameScreen.PLAYING) {
            val didJump = player.jump(inputY)
            if (didJump) soundEngine.playJump()
        }
    }

    fun onJumpReleased() {
        if (currentScreen == GameScreen.PLAYING) {
            player.onJumpCut()
        }
    }

    fun onAttackPressed() {
        if (currentScreen == GameScreen.PLAYING) {
            val didAttack = player.attack()
            if (didAttack) soundManager.onPlayerAttack(player.comboStep)
        }
    }

    fun toggleSound() {
        val newState = !saveManager.isSoundEnabled
        saveManager.isSoundEnabled = newState
        soundManager.isMuted = !newState
        if (soundManager.isMuted) {
            soundManager.stopAmbience()
        } else {
            soundManager.setAreaAmbience(currentAreaName)
        }
    }

    fun respawnPlayer() {
        player.respawn()
        camera.snapToPlayer(player)
        currentScreen = GameScreen.PLAYING
        currentAreaName = getAreaNameForCol((player.pos.x / world.tileSize).toInt())
        areaBannerTimer = 3f

        for (enemy in enemies) {
            if (enemy.pos.distanceTo(player.pos) < 300f && enemy.aiState == com.example.game.entities.EnemyAIState.CHASE) {
                enemy.aiState = com.example.game.entities.EnemyAIState.PATROL
                enemy.vel.set(0f, 0f)
            }
        }
    }
}
