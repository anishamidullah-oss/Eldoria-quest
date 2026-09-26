package com.example.game.world

import com.example.game.entities.Collectible
import com.example.game.entities.CollectibleType
import com.example.game.entities.Enemy
import com.example.game.entities.EnemyType
import com.example.game.entities.NPC
import com.example.game.entities.NPCType
import com.example.game.model.Vector2D
import com.example.game.puzzles.LeverSwitch
import com.example.game.puzzles.LockedGate
import com.example.game.puzzles.MovingPlatform
import com.example.game.puzzles.PressurePlate
import com.example.game.puzzles.RunicTorch
import com.example.game.puzzles.SecretCrumbleWall

class WorldMap {
    val tileSize = 32f
    val cols = 320
    val rows = 24

    val width: Float get() = cols * tileSize
    val height: Float get() = rows * tileSize

    val tiles = Array(rows) { Array(cols) { TileType.AIR } }

    val initialEnemies = mutableListOf<Enemy>()
    val initialCollectibles = mutableListOf<Collectible>()
    val initialNPCs = mutableListOf<NPC>()

    // Puzzle & Interactive Mechanisms
    val pressurePlates = mutableListOf<PressurePlate>()
    val leverSwitches = mutableListOf<LeverSwitch>()
    val lockedGates = mutableListOf<LockedGate>()
    val movingPlatforms = mutableListOf<MovingPlatform>()
    val runicTorches = mutableListOf<RunicTorch>()
    val crumbleWalls = mutableListOf<SecretCrumbleWall>()

    init {
        buildLevel()
    }

    fun setTile(r: Int, c: Int, type: TileType) {
        if (r in 0 until rows && c in 0 until cols) {
            tiles[r][c] = type
        }
    }

    fun getTile(r: Int, c: Int): TileType {
        if (r !in 0 until rows || c !in 0 until cols) return TileType.AIR
        return tiles[r][c]
    }

    fun getTileAtWorldPos(worldX: Float, worldY: Float): TileType {
        val c = (worldX / tileSize).toInt()
        val r = (worldY / tileSize).toInt()
        return getTile(r, c)
    }

    private fun buildLevel() {
        // Left boundary wall
        for (r in 0 until rows) {
            setTile(r, 0, TileType.STONE_BLOCK)
        }

        // ==========================================
        // 1. REGION 1: THE WHISPERING WOODS (c: 0 to 39)
        // ==========================================
        for (c in 0..39) {
            if (c !in 18..20 && c !in 32..34) {
                setTile(18, c, TileType.GRASS_TOP)
                for (r in 19 until rows) setTile(r, c, TileType.DIRT)
            } else {
                setTile(22, c, TileType.SPIKE_HAZARD)
                for (r in 23 until rows) setTile(r, c, TileType.DIRT)
            }
        }

        // Forest elevated treehouse canopy & secret platforms
        for (c in 6..11) setTile(14, c, TileType.WOOD_PLATFORM)
        for (r in 15..17) setTile(r, 11, TileType.LADDER)

        for (c in 14..19) setTile(11, c, TileType.WOOD_PLATFORM)
        for (r in 12..17) setTile(r, 14, TileType.LADDER)

        // Secret High Treehouse Canopy (Contains Roderick's Brass Compass)
        for (c in 15..18) setTile(7, c, TileType.WOOD_PLATFORM)
        for (r in 8..10) setTile(r, 18, TileType.LADDER)

        for (c in 22..26) setTile(15, c, TileType.WOOD_PLATFORM)

        // Secret Forest Grotto / Wolf Den (c: 24..30)
        setTile(12, 27, TileType.GRASS_LEFT)
        for (c in 28..30) setTile(12, c, TileType.GRASS_TOP)
        setTile(12, 31, TileType.GRASS_RIGHT)
        for (c in 27..31) setTile(13, c, TileType.DIRT)

        // Checkpoint 1: Forest Glade (c: 38, r: 16)
        initialCollectibles.add(
            Collectible(
                id = "cp_forest",
                pos = Vector2D(38f * tileSize, 16f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // Lore Tablet 1: Forest Border
        initialCollectibles.add(
            Collectible(
                id = "lore_forest",
                pos = Vector2D(12f * tileSize, 17f * tileSize),
                type = CollectibleType.LORE_TABLET,
                extraData = "Ancient Lore: When the Heart of Aether was shattered by dark corruption, its radiant fragments scattered to the far corners of the realm."
            )
        )

        // Side Quest Collectible: Roderick's Brass Compass
        initialCollectibles.add(
            Collectible(
                id = "quest_brass_compass",
                pos = Vector2D(16.5f * tileSize, 6f * tileSize),
                type = CollectibleType.BRASS_COMPASS,
                value = 50,
                extraData = "BRASS_COMPASS"
            )
        )

        // Optional Boss: Dreadfang Shadow Wolf in Forest Grotto
        initialEnemies.add(
            Enemy(
                id = "boss_dreadfang_wolf",
                type = EnemyType.DREADFANG_WOLF,
                startX = 28f * tileSize,
                startY = 10.5f * tileSize,
                patrolMinX = 26f * tileSize,
                patrolMaxX = 31f * tileSize
            )
        )

        // ==========================================
        // 2. REGION 2: OAKHAVEN HAMLET (c: 40 to 79)
        // ==========================================
        for (c in 40..79) {
            setTile(18, c, TileType.COBBLESTONE)
            for (r in 19 until rows) setTile(r, c, TileType.STONE_BRICK)
        }

        // Village Buildings & Roofs
        // Elder's Lodge (c: 42 to 48)
        for (c in 42..48) setTile(13, c, TileType.ROOF_TILE)
        for (r in 14..17) {
            setTile(r, 42, TileType.WOOD_PLANK)
            setTile(r, 48, TileType.WOOD_PLANK)
        }

        // Blacksmith Forge (c: 52 to 58)
        for (c in 52..58) setTile(13, c, TileType.ROOF_TILE)
        for (r in 14..17) {
            setTile(r, 52, TileType.STONE_BLOCK)
            setTile(r, 58, TileType.STONE_BLOCK)
        }
        for (c in 53..57) setTile(15, c, TileType.WOOD_PLATFORM)

        // Checkpoint 2: Village Hearth (c: 58, r: 16)
        initialCollectibles.add(
            Collectible(
                id = "cp_village",
                pos = Vector2D(58f * tileSize, 16f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // Village Gate Tower & Ladder (c: 74 to 78)
        for (c in 74..78) setTile(12, c, TileType.STONE_BLOCK)
        for (r in 13..17) setTile(r, 78, TileType.LADDER)

        // NPCs IN OAKHAVEN
        initialNPCs.add(NPC("npc_elder", NPCType.VILLAGE_ELDER, Vector2D(44f * tileSize, 16.5f * tileSize)))
        initialNPCs.add(NPC("npc_blacksmith", NPCType.BLACKSMITH, Vector2D(54f * tileSize, 16.5f * tileSize)))
        initialNPCs.add(NPC("npc_merchant", NPCType.MERCHANT, Vector2D(64f * tileSize, 16.5f * tileSize)))
        initialNPCs.add(NPC("npc_guard", NPCType.GUARD, Vector2D(76f * tileSize, 16.5f * tileSize)))

        // ==========================================
        // 3. REGION 3: DEEPROOT CAVERNS (c: 80 to 119)
        // ==========================================
        // Cave ceiling
        for (c in 80..119) {
            for (r in 0..6) setTile(r, c, TileType.CAVE_ROCK)
        }

        // Cave ground terrain
        for (c in 80..119) {
            if (c !in 96..99 && c !in 112..114) {
                setTile(20, c, TileType.CAVE_ROCK)
                for (r in 21 until rows) setTile(r, c, TileType.CAVE_ROCK)
            } else {
                setTile(22, c, TileType.SPIKE_HAZARD)
                for (r in 23 until rows) setTile(r, c, TileType.CAVE_ROCK)
            }
        }

        // Cavern upper ledges
        for (c in 86..94) setTile(15, c, TileType.CAVE_ROCK)
        for (r in 16..19) setTile(r, 88, TileType.LADDER)

        // Hidden Cavern Vault (c: 106 to 112 at r: 12)
        for (c in 106..112) setTile(12, c, TileType.WOOD_PLATFORM)
        for (r in 13..19) setTile(r, 110, TileType.LADDER)

        // Sunken Cave Hollow (Beneath Cavern at c: 98..105, r: 21..23)
        for (c in 98..105) {
            setTile(21, c, TileType.AIR)
            setTile(22, c, TileType.CAVE_ROCK)
        }

        // Puzzle 1: Pressure Plate & Lower Gate to Sunken Hollow
        pressurePlates.add(
            PressurePlate(
                id = "plate_cave_sunken",
                pos = Vector2D(86f * tileSize, 14.7f * tileSize),
                targetMechanismId = "gate_cave_sunken"
            )
        )
        lockedGates.add(
            LockedGate(
                id = "gate_cave_sunken",
                pos = Vector2D(97f * tileSize, 20f * tileSize),
                height = 64f,
                triggerMechanismId = "plate_cave_sunken"
            )
        )

        // NPC Traveler
        initialNPCs.add(NPC("npc_traveler", NPCType.TRAVELER, Vector2D(84f * tileSize, 18.5f * tileSize)))

        // Checkpoint 3: Deep Cavern Crystal (c: 92, r: 18)
        initialCollectibles.add(
            Collectible(
                id = "cp_cave",
                pos = Vector2D(92f * tileSize, 18f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // Chest with CITADEL KEY 1 in Hidden Cavern Vault
        initialCollectibles.add(
            Collectible(
                id = "chest_key_1",
                pos = Vector2D(108f * tileSize, 11f * tileSize),
                type = CollectibleType.CHEST,
                value = 100,
                extraData = "ANCIENT_KEY_1"
            )
        )

        // Ancient Runic Ore Vein 1 & 2
        initialCollectibles.add(
            Collectible(
                id = "ore_vein_1",
                pos = Vector2D(91f * tileSize, 14f * tileSize),
                type = CollectibleType.RUNIC_ORE,
                extraData = "RUNIC_ORE_1"
            )
        )
        initialCollectibles.add(
            Collectible(
                id = "ore_vein_2",
                pos = Vector2D(102f * tileSize, 21.5f * tileSize),
                type = CollectibleType.RUNIC_ORE,
                extraData = "RUNIC_ORE_2"
            )
        )

        // Secret Chest in Sunken Cave (Contains Heart Shard II)
        initialCollectibles.add(
            Collectible(
                id = "secret_chest_cave",
                pos = Vector2D(104f * tileSize, 21.2f * tileSize),
                type = CollectibleType.SECRET_CHEST,
                value = 250,
                extraData = "HEART_FRAGMENT_2"
            )
        )

        // Lore Tablet 2: Subterranean Inscription
        initialCollectibles.add(
            Collectible(
                id = "lore_cave",
                pos = Vector2D(102f * tileSize, 19f * tileSize),
                type = CollectibleType.LORE_TABLET,
                extraData = "Runic Carving: Three Golden Citadel Keys were sealed away by ancient kings to secure the inner sanctum against shadowy usurpation."
            )
        )

        // ==========================================
        // 4. REGION 4: ANCIENT FORGOTTEN RUINS (c: 120 to 159)
        // ==========================================
        for (c in 120..159) {
            if (c !in 134..137 && c !in 150..152) {
                setTile(18, c, TileType.STONE_BLOCK)
                for (r in 19 until rows) setTile(r, c, TileType.STONE_BRICK)
            } else {
                setTile(22, c, TileType.SPIKE_HAZARD)
                for (r in 23 until rows) setTile(r, c, TileType.STONE_BRICK)
            }
        }

        // Broken Ruin Pillars & Overhanging Bridges
        for (c in 126..132) setTile(14, c, TileType.STONE_BLOCK)
        for (r in 15..17) setTile(r, 128, TileType.LADDER)

        for (c in 138..146) setTile(13, c, TileType.WOOD_PLATFORM)
        for (c in 152..158) setTile(14, c, TileType.STONE_BLOCK)

        // Waterfall covering Secret Ruins Treasury
        for (r in 8..13) {
            setTile(r, 142, TileType.WATERFALL)
            setTile(r, 143, TileType.WATERFALL)
        }

        // Secret Crumble Wall behind Waterfall (c: 144, r: 12)
        setTile(12, 144, TileType.RUIN_CRUMBLE_BLOCK)
        crumbleWalls.add(
            SecretCrumbleWall(
                id = "crumble_ruins_treasury",
                col = 144,
                row = 12,
                tileSize = tileSize
            )
        )

        // Puzzle 2: Lever Switch in Ruins Crypt unlocking Treasury Chest
        leverSwitches.add(
            LeverSwitch(
                id = "lever_ruins_crypt",
                pos = Vector2D(127f * tileSize, 13f * tileSize),
                targetMechanismId = "gate_ruins_treasury"
            )
        )
        lockedGates.add(
            LockedGate(
                id = "gate_ruins_treasury",
                pos = Vector2D(145f * tileSize, 11f * tileSize),
                height = 64f,
                triggerMechanismId = "lever_ruins_crypt"
            )
        )

        // NPC Explorer Kaelen
        initialNPCs.add(NPC("npc_explorer", NPCType.EXPLORER, Vector2D(124f * tileSize, 16.5f * tileSize)))

        // Checkpoint 4: Ruins Sanctum (c: 132, r: 16)
        initialCollectibles.add(
            Collectible(
                id = "cp_ruins",
                pos = Vector2D(132f * tileSize, 16f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // Chest with CITADEL KEY 2 in Ruins High Arch
        initialCollectibles.add(
            Collectible(
                id = "chest_key_2",
                pos = Vector2D(154f * tileSize, 13f * tileSize),
                type = CollectibleType.CHEST,
                value = 120,
                extraData = "ANCIENT_KEY_2"
            )
        )

        // Secret Relic Chalice in Treasury behind waterfall
        initialCollectibles.add(
            Collectible(
                id = "secret_relic_chalice",
                pos = Vector2D(147f * tileSize, 11.5f * tileSize),
                type = CollectibleType.RELIC_CHALICE,
                value = 500,
                extraData = "RELIC_CHALICE"
            )
        )

        // Side Quest item: Bronze Dungeon Key in Crypt
        initialCollectibles.add(
            Collectible(
                id = "quest_bronze_key",
                pos = Vector2D(136f * tileSize, 16.5f * tileSize),
                type = CollectibleType.ANCIENT_KEY,
                extraData = "BRONZE_DUNGEON_KEY"
            )
        )

        // ==========================================
        // 5. REGION 5: FROSTPEAK SUMMIT (c: 160 to 199)
        // ==========================================
        for (c in 160..199) {
            if (c !in 176..179 && c !in 192..194) {
                setTile(18, c, TileType.SNOW_TOP)
                for (r in 19 until rows) setTile(r, c, TileType.DIRT)
            } else {
                setTile(22, c, TileType.SPIKE_HAZARD)
                for (r in 23 until rows) setTile(r, c, TileType.DIRT)
            }
        }

        // Glacial Ice Platforms & Mountain Steps
        for (c in 166..174) setTile(14, c, TileType.ICE_BLOCK)
        for (r in 15..17) setTile(r, 168, TileType.LADDER)

        for (c in 178..186) setTile(11, c, TileType.SNOW_TOP)
        for (r in 12..17) setTile(r, 180, TileType.LADDER)

        // Moving Platforms over Frozen Mountain Chasms
        movingPlatforms.add(
            MovingPlatform(
                id = "moving_plat_frost_1",
                startPos = Vector2D(174f * tileSize, 14f * tileSize),
                endPos = Vector2D(180f * tileSize, 14f * tileSize),
                width = 56f,
                speed = 50f
            )
        )
        movingPlatforms.add(
            MovingPlatform(
                id = "moving_plat_frost_2",
                startPos = Vector2D(188f * tileSize, 9f * tileSize),
                endPos = Vector2D(196f * tileSize, 9f * tileSize),
                width = 56f,
                speed = 55f
            )
        )

        // High Glacial Ridge (Holds Cartographer's Parchment)
        for (c in 194..198) setTile(8, c, TileType.ICE_BLOCK)

        // Checkpoint 5: Frostpeak Beacon (c: 188, r: 16)
        initialCollectibles.add(
            Collectible(
                id = "cp_mountain",
                pos = Vector2D(188f * tileSize, 16f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // Quest Collectibles: Sentry Badge & Cartographer's Parchment
        initialCollectibles.add(
            Collectible(
                id = "quest_guard_badge",
                pos = Vector2D(171f * tileSize, 13f * tileSize),
                type = CollectibleType.GUARD_BADGE,
                extraData = "GUARD_BADGE"
            )
        )
        initialCollectibles.add(
            Collectible(
                id = "quest_map_parchment",
                pos = Vector2D(196f * tileSize, 7f * tileSize),
                type = CollectibleType.MAP_PARCHMENT,
                extraData = "MAP_PARCHMENT"
            )
        )

        // Optional Boss: Glacial Frost Wurm
        initialEnemies.add(
            Enemy(
                id = "boss_frost_wurm",
                type = EnemyType.FROST_WURM,
                startX = 191f * tileSize,
                startY = 16f * tileSize,
                patrolMinX = 186f * tileSize,
                patrolMaxX = 196f * tileSize
            )
        )

        // ==========================================
        // 6. REGION 6: THE HIDDEN LUNAR SHRINE (c: 200 to 239)
        // ==========================================
        for (c in 200..239) {
            setTile(18, c, TileType.STONE_BLOCK)
            for (r in 19 until rows) setTile(r, c, TileType.STONE_BRICK)
        }

        // Shrine Fluted Marble Altars & Colonades
        for (c in 208..216) setTile(14, c, TileType.STONE_BLOCK)
        for (c in 222..232) setTile(13, c, TileType.STONE_BLOCK)
        for (r in 14..17) setTile(r, 224, TileType.LADDER)

        // Puzzle 3: 3 Runic Torches at the Sacred Sanctuary
        runicTorches.add(
            RunicTorch(
                id = "torch_shrine_1",
                pos = Vector2D(222f * tileSize, 11.5f * tileSize),
                groupId = "group_shrine",
                targetMechanismId = "gate_shrine_vault"
            )
        )
        runicTorches.add(
            RunicTorch(
                id = "torch_shrine_2",
                pos = Vector2D(227f * tileSize, 11.5f * tileSize),
                groupId = "group_shrine",
                targetMechanismId = "gate_shrine_vault"
            )
        )
        runicTorches.add(
            RunicTorch(
                id = "torch_shrine_3",
                pos = Vector2D(232f * tileSize, 11.5f * tileSize),
                groupId = "group_shrine",
                targetMechanismId = "gate_shrine_vault"
            )
        )

        lockedGates.add(
            LockedGate(
                id = "gate_shrine_vault",
                pos = Vector2D(235f * tileSize, 16f * tileSize),
                height = 64f,
                triggerMechanismId = "group_shrine"
            )
        )

        // NPC Scholar Lysandra
        initialNPCs.add(NPC("npc_scholar", NPCType.SCHOLAR, Vector2D(206f * tileSize, 16.5f * tileSize)))

        // Checkpoint 6: Lunar Shrine Altar (c: 218, r: 16)
        initialCollectibles.add(
            Collectible(
                id = "cp_shrine",
                pos = Vector2D(218f * tileSize, 16f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // HEART OF AETHER FRAGMENT I AT SHRINE ALTAR (c: 226)
        initialCollectibles.add(
            Collectible(
                id = "heart_fragment_1",
                pos = Vector2D(226f * tileSize, 11.5f * tileSize),
                type = CollectibleType.HEART_FRAGMENT,
                value = 500,
                extraData = "HEART_FRAGMENT_1"
            )
        )

        // Chest with CITADEL KEY 3 at Shrine Crypt
        initialCollectibles.add(
            Collectible(
                id = "chest_key_3",
                pos = Vector2D(234f * tileSize, 17f * tileSize),
                type = CollectibleType.CHEST,
                value = 150,
                extraData = "ANCIENT_KEY_3"
            )
        )

        // Ancient Runic Ore Vein 3
        initialCollectibles.add(
            Collectible(
                id = "ore_vein_3",
                pos = Vector2D(210f * tileSize, 13f * tileSize),
                type = CollectibleType.RUNIC_ORE,
                extraData = "RUNIC_ORE_3"
            )
        )

        // Secret Chest inside Shrine Vault (Heart Shard III & Hermes Boots)
        initialCollectibles.add(
            Collectible(
                id = "secret_chest_shrine",
                pos = Vector2D(237f * tileSize, 17f * tileSize),
                type = CollectibleType.SECRET_CHEST,
                value = 400,
                extraData = "HEART_FRAGMENT_3"
            )
        )

        // Lore Tablet 3: Lunar Scripture
        initialCollectibles.add(
            Collectible(
                id = "lore_shrine",
                pos = Vector2D(212f * tileSize, 17f * tileSize),
                type = CollectibleType.LORE_TABLET,
                extraData = "Shrine Tablet: As the Heart of Aether shattered, its primary heartshard sought sanctuary beneath the sacred moonlight."
            )
        )

        // ==========================================
        // 7. REGION 7: STORMGATE CASTLE KEEP (c: 240 to 279)
        // ==========================================
        for (c in 240..279) {
            if (c !in 254..256 && c !in 268..270) {
                setTile(18, c, TileType.STONE_BLOCK)
                for (r in 19 until rows) setTile(r, c, TileType.STONE_BRICK)
            } else {
                setTile(22, c, TileType.SPIKE_HAZARD)
                for (r in 23 until rows) setTile(r, c, TileType.STONE_BRICK)
            }
        }

        // Castle Gatehouse Battlements & Portcullis
        for (c in 246..252) setTile(13, c, TileType.STONE_BLOCK)
        for (r in 14..17) setTile(r, 247, TileType.LADDER)

        for (c in 260..266) setTile(14, c, TileType.WOOD_PLATFORM)

        // Castle Crypt Undercroft (Optional Boss: Shadow Lich)
        for (c in 266..276) {
            setTile(21, c, TileType.AIR)
            setTile(22, c, TileType.STONE_BLOCK)
        }

        // Checkpoint 7: Castle Gatehouse (c: 244, r: 16)
        initialCollectibles.add(
            Collectible(
                id = "cp_castle",
                pos = Vector2D(244f * tileSize, 16f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // Optional Boss: Malakor the Shadow Lich in Undercroft
        initialEnemies.add(
            Enemy(
                id = "boss_shadow_lich",
                type = EnemyType.SHADOW_LICH,
                startX = 270f * tileSize,
                startY = 20f * tileSize,
                patrolMinX = 266f * tileSize,
                patrolMaxX = 276f * tileSize
            )
        )

        // Secret Chest in Castle Crypt (Contains Aegis Dragon Armor)
        initialCollectibles.add(
            Collectible(
                id = "secret_chest_castle",
                pos = Vector2D(275f * tileSize, 21f * tileSize),
                type = CollectibleType.SECRET_CHEST,
                value = 500,
                extraData = "AEGIS_ARMOR"
            )
        )

        // ==========================================
        // 8. REGION 8: THRONE OF THE RUIN COLOSSUS (c: 280 to 319)
        // ==========================================
        for (c in 280 until cols) {
            setTile(18, c, TileType.STONE_BLOCK)
            for (r in 19 until rows) setTile(r, c, TileType.STONE_BRICK)
        }

        // High arena battlements
        for (c in 288..310) setTile(12, c, TileType.WOOD_PLATFORM)
        for (r in 13..17) setTile(r, 290, TileType.LADDER)

        // Right boundary wall
        for (r in 0 until rows) {
            setTile(r, cols - 1, TileType.STONE_BLOCK)
        }

        // Checkpoint 8: Throne Antechamber (c: 282, r: 16)
        initialCollectibles.add(
            Collectible(
                id = "cp_throne",
                pos = Vector2D(282f * tileSize, 16f * tileSize),
                type = CollectibleType.CHECKPOINT
            )
        )

        // Victory Portal at the end of the realm (c: 314, r: 15)
        initialCollectibles.add(
            Collectible(
                id = "victory_portal",
                pos = Vector2D(314f * tileSize, 15.5f * tileSize),
                type = CollectibleType.VICTORY_PORTAL
            )
        )

        // ==========================================
        // POPULATE COINS, CRYSTALS & POTIONS ACROSS WORLD
        // ==========================================
        val coinCols = listOf(
            5f, 9f, 15f, 23f, 29f, 36f, 48f, 55f, 63f, 72f, 82f, 89f, 95f, 104f, 111f,
            122f, 128f, 136f, 144f, 153f, 164f, 172f, 180f, 186f, 194f, 204f, 214f, 224f,
            232f, 242f, 250f, 258f, 266f, 274f, 284f, 292f, 302f, 308f
        )
        coinCols.forEachIndexed { i, colX ->
            initialCollectibles.add(
                Collectible(
                    id = "coin_$i",
                    pos = Vector2D(colX * tileSize, 16.5f * tileSize),
                    type = CollectibleType.COIN,
                    value = 15
                )
            )
        }

        // Mana Crystals
        val crystalCols = listOf(17f, 30f, 67f, 90f, 107f, 140f, 170f, 184f, 210f, 230f, 262f, 305f)
        crystalCols.forEachIndexed { i, colX ->
            initialCollectibles.add(
                Collectible(
                    id = "crystal_$i",
                    pos = Vector2D(colX * tileSize, 13f * tileSize),
                    type = CollectibleType.MANA_CRYSTAL,
                    value = 50
                )
            )
        }

        // Health Potions
        val potionCols = listOf(28f, 65f, 105f, 145f, 182f, 222f, 264f)
        potionCols.forEachIndexed { i, colX ->
            initialCollectibles.add(
                Collectible(
                    id = "potion_$i",
                    pos = Vector2D(colX * tileSize, 16.5f * tileSize),
                    type = CollectibleType.HEALTH_POTION,
                    value = 45
                )
            )
        }

        // ==========================================
        // POPULATE ENEMIES ACROSS ALL 8 REGIONS
        // ==========================================
        // 1. Forest
        initialEnemies.add(Enemy("moss_slime_1", EnemyType.MOSS_SLIME, 8f * tileSize, 17f * tileSize, 4f * tileSize, 12f * tileSize))
        initialEnemies.add(Enemy("grasswalker_1", EnemyType.GRASSWALKER, 16f * tileSize, 17f * tileSize, 13f * tileSize, 21f * tileSize))
        initialEnemies.add(Enemy("bush_beetle_1", EnemyType.BUSH_BEETLE, 25f * tileSize, 17f * tileSize, 22f * tileSize, 30f * tileSize))

        // 2. Village Outskirts Invaders
        initialEnemies.add(Enemy("invader_slime_1", EnemyType.MOSS_SLIME, 49f * tileSize, 17f * tileSize, 47f * tileSize, 53f * tileSize))
        initialEnemies.add(Enemy("invader_beetle_1", EnemyType.BUSH_BEETLE, 61f * tileSize, 17f * tileSize, 59f * tileSize, 67f * tileSize))
        initialEnemies.add(Enemy("invader_crawler_1", EnemyType.CAVE_CRAWLER, 70f * tileSize, 17f * tileSize, 66f * tileSize, 75f * tileSize))

        // 3. Deeproot Caverns
        initialEnemies.add(Enemy("cave_crawler_1", EnemyType.CAVE_CRAWLER, 88f * tileSize, 19f * tileSize, 85f * tileSize, 95f * tileSize))
        initialEnemies.add(Enemy("cave_bat_1", EnemyType.CAVE_BAT, 94f * tileSize, 13f * tileSize, 88f * tileSize, 102f * tileSize))
        initialEnemies.add(Enemy("goblin_1", EnemyType.SHADOW_GOBLIN, 104f * tileSize, 19f * tileSize, 100f * tileSize, 110f * tileSize))

        // 4. Ancient Forgotten Ruins Guardian
        initialEnemies.add(Enemy("ruin_beetle_1", EnemyType.BUSH_BEETLE, 128f * tileSize, 17f * tileSize, 125f * tileSize, 133f * tileSize))
        initialEnemies.add(Enemy("ruin_watcher_stone_giant", EnemyType.STONE_GIANT, 146f * tileSize, 17f * tileSize, 140f * tileSize, 150f * tileSize))
        initialEnemies.add(Enemy("ruin_bat_1", EnemyType.CAVE_BAT, 142f * tileSize, 12f * tileSize, 136f * tileSize, 148f * tileSize))

        // 5. Frostpeak Summit
        initialEnemies.add(Enemy("frost_beetle_1", EnemyType.BUSH_BEETLE, 168f * tileSize, 17f * tileSize, 162f * tileSize, 174f * tileSize))
        initialEnemies.add(Enemy("frost_bat_1", EnemyType.CAVE_BAT, 174f * tileSize, 11f * tileSize, 168f * tileSize, 184f * tileSize))
        initialEnemies.add(Enemy("frost_giant_1", EnemyType.STONE_GIANT, 184f * tileSize, 17f * tileSize, 178f * tileSize, 190f * tileSize))

        // 6. The Hidden Lunar Shrine
        initialEnemies.add(Enemy("shrine_goblin_1", EnemyType.SHADOW_GOBLIN, 210f * tileSize, 17f * tileSize, 204f * tileSize, 216f * tileSize))
        initialEnemies.add(Enemy("shrine_bat_1", EnemyType.CAVE_BAT, 220f * tileSize, 12f * tileSize, 214f * tileSize, 228f * tileSize))

        // 7. Stormgate Castle Keep
        initialEnemies.add(Enemy("castle_knight_1", EnemyType.SKELETON_KNIGHT, 252f * tileSize, 17f * tileSize, 246f * tileSize, 258f * tileSize))
        initialEnemies.add(Enemy("castle_giant_1", EnemyType.STONE_GIANT, 264f * tileSize, 17f * tileSize, 258f * tileSize, 272f * tileSize))
        initialEnemies.add(Enemy("castle_knight_2", EnemyType.SKELETON_KNIGHT, 274f * tileSize, 17f * tileSize, 268f * tileSize, 278f * tileSize))

        // 8. Throne Arena Main Boss
        initialEnemies.add(
            Enemy(
                id = "boss_ruin_colossus",
                type = EnemyType.RUIN_COLOSSUS,
                startX = 298f * tileSize,
                startY = 16f * tileSize,
                patrolMinX = 286f * tileSize,
                patrolMaxX = 310f * tileSize
            )
        )
    }
}
