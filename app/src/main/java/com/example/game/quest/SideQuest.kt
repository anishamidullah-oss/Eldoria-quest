package com.example.game.quest

import com.example.game.entities.NPCType
import com.example.game.entities.Player

data class SideQuest(
    val id: String,
    val title: String,
    val giverName: String,
    val giverType: NPCType,
    val objective: String,
    val description: String,
    val targetArea: String,
    val targetWorldX: Float,
    var currentProgress: Int = 0,
    val targetCount: Int = 1,
    val reward: QuestReward,
    var isDiscovered: Boolean = false,
    var isAccepted: Boolean = false,
    var isCompleted: Boolean = false
) {
    val npcName: String get() = giverName
    val objectiveText: String get() = objective
}

class SideQuestManager {
    val sideQuests = mutableListOf<SideQuest>()

    init {
        initializeSideQuests()
    }

    private fun initializeSideQuests() {
        sideQuests.clear()

        // 1. The Lost Traveler
        sideQuests.add(
            SideQuest(
                id = "sq_lost_traveler",
                title = "The Lost Traveler",
                giverName = "Traveler Roderick",
                giverType = NPCType.TRAVELER,
                objective = "Find the Brass Compass lost high in the Whispering Woods canopy.",
                description = "Roderick was ambushed by slimes in the woods and dropped his grandfather's heirloom Brass Compass on an upper treehouse platform.",
                targetArea = "Whispering Woods",
                targetWorldX = 16f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 120, coins = 60, itemDescription = "Explorer's Compass (+20% Map Detail)")
            )
        )

        // 2. Village Supplies
        sideQuests.add(
            SideQuest(
                id = "sq_village_supplies",
                title = "Village Supplies",
                giverName = "Merchant Elowen",
                giverType = NPCType.MERCHANT,
                objective = "Gather 6 glowing Mana Crystals to replenish the village defensive ward.",
                description = "With the monsters encroaching, Oakhaven's protective ward crystal is dimming. Deliver 6 Mana Crystals found in caverns and ruins.",
                targetArea = "Deeproot Caverns",
                targetWorldX = 90f * 32f,
                targetCount = 6,
                reward = QuestReward(xp = 150, coins = 90, itemDescription = "Merchant Pouch (+3 Max Potions)")
            )
        )

        // 3. Hidden Treasure
        sideQuests.add(
            SideQuest(
                id = "sq_hidden_treasure",
                title = "Hidden Treasure",
                giverName = "Scout Kaelen",
                giverType = NPCType.EXPLORER,
                objective = "Uncover the secret treasure chamber hidden behind the ancient ruins waterfall.",
                description = "Legend tells of an opulent vault concealed behind crumbling masonry and roaring waters in the Forgotten Ruins.",
                targetArea = "Ancient Ruins",
                targetWorldX = 142f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 200, coins = 150, itemDescription = "Golden Relic Chalice (+500 Score)")
            )
        )

        // 4. The Forgotten Cave
        sideQuests.add(
            SideQuest(
                id = "sq_forgotten_cave",
                title = "The Forgotten Cave",
                giverName = "Elder Bran",
                giverType = NPCType.VILLAGE_ELDER,
                objective = "Delve into the Sunken Cave beneath the Caverns and defeat the Alpha Crawler.",
                description = "An ancient subterranean hollow under Deeproot Caverns has bred a ferocious Alpha Crawler that threatens our underground well.",
                targetArea = "Deeproot Caverns",
                targetWorldX = 110f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 180, coins = 100, maxHealthBonus = 25, itemDescription = "Heart Shard II (+25 Max HP)")
            )
        )

        // 5. The Old Map
        sideQuests.add(
            SideQuest(
                id = "sq_old_map",
                title = "The Old Map",
                giverName = "Scholar Lysandra",
                giverType = NPCType.SCHOLAR,
                objective = "Recover the ancient Cartographer's Parchment from the Frostpeak high glacial cliff.",
                description = "An ancient expedition lost their royal cartographic chart atop the windswept icy precipice of Frostpeak Summit.",
                targetArea = "Frostpeak Summit",
                targetWorldX = 182f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 160, coins = 100, itemDescription = "Royal Cartography (Reveals All Checkpoints)")
            )
        )

        // 6. The Missing Guard
        sideQuests.add(
            SideQuest(
                id = "sq_missing_guard",
                title = "The Missing Guard",
                giverName = "Guard Captain",
                giverType = NPCType.GUARD,
                objective = "Locate Sentry Eric's outpost on the Mountain Pass and retrieve his Badge.",
                description = "Sentry Eric was posted at the mountain choke-point before contact was lost. Search the frozen crags for his badge.",
                targetArea = "Frostpeak Summit",
                targetWorldX = 172f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 170, coins = 110, itemDescription = "Iron Vanguard Shield (+10 Armor)")
            )
        )

        // 7. The Forest Beast
        sideQuests.add(
            SideQuest(
                id = "sq_forest_beast",
                title = "The Forest Beast",
                giverName = "Blacksmith Torin",
                giverType = NPCType.BLACKSMITH,
                objective = "Track and slay the Dreadfang Shadow Wolf in the secret forest grotto.",
                description = "A savage Shadow Wolf has made the deepest forest clearing its hunting ground, terrorizing woodcutters and travelers.",
                targetArea = "Whispering Woods",
                targetWorldX = 26f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 250, coins = 160, damageBonus = 6, itemDescription = "Dreadfang Fang Blade (+6 Attack)")
            )
        )

        // 8. The Ancient Key
        sideQuests.add(
            SideQuest(
                id = "sq_ancient_key",
                title = "The Ancient Key",
                giverName = "Scout Kaelen",
                giverType = NPCType.EXPLORER,
                objective = "Find the Bronze Dungeon Key locked in the Ruins Crypt to open the lower gate.",
                description = "A heavy iron gate in the ruins requires an ancient bronze key rumoured to be sealed in a nearby puzzle room.",
                targetArea = "Ancient Ruins",
                targetWorldX = 136f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 190, coins = 120, itemDescription = "Skeleton Master Key")
            )
        )

        // 9. The Blacksmith's Request
        sideQuests.add(
            SideQuest(
                id = "sq_blacksmith_request",
                title = "The Blacksmith's Request",
                giverName = "Blacksmith Torin",
                giverType = NPCType.BLACKSMITH,
                objective = "Mine 3 chunks of Ancient Runic Ore from secret cave veins to forge the Blazing Sunblade.",
                description = "Torin requires rare Runic Ore veins buried in deep caverns to forge an ancient elemental sword capable of piercing dark armor.",
                targetArea = "Deeproot Caverns",
                targetWorldX = 100f * 32f,
                targetCount = 3,
                reward = QuestReward(xp = 300, coins = 200, damageBonus = 12, itemDescription = "Blazing Sunblade (Legendary Weapon +12 DMG)")
            )
        )

        // 10. The Secret Shrine
        sideQuests.add(
            SideQuest(
                id = "sq_secret_shrine",
                title = "The Secret Shrine",
                giverName = "Scholar Lysandra",
                giverType = NPCType.SCHOLAR,
                objective = "Solve the 3 Runic Torches puzzle in the sacred Sunken Lunar Sanctuary.",
                description = "Three ancient arcane torches in the hidden sanctuary must be ignited in unison with a melee strike to reveal the sacred blessing.",
                targetArea = "The Lunar Shrine",
                targetWorldX = 224f * 32f,
                targetCount = 3,
                reward = QuestReward(xp = 280, coins = 180, maxHealthBonus = 25, itemDescription = "Heart Shard III (+25 Max HP) & Hermes Boots")
            )
        )

        // 11. Glacial Menace (Optional Boss Quest)
        sideQuests.add(
            SideQuest(
                id = "sq_glacial_menace",
                title = "Glacial Menace",
                giverName = "Scholar Lysandra",
                giverType = NPCType.SCHOLAR,
                objective = "Defeat the Glacial Frost Wurm lurking in the high mountain crevasse.",
                description = "A massive frost serpent has emerged from the glacier, freezing the upper trails and trapping weary wanderers.",
                targetArea = "Frostpeak Summit",
                targetWorldX = 190f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 320, coins = 220, itemDescription = "Frostguard Cloak (+15 Defense)")
            )
        )

        // 12. Phantom King of the Crypt (Optional Boss Quest)
        sideQuests.add(
            SideQuest(
                id = "sq_phantom_king",
                title = "Phantom King of the Crypt",
                giverName = "Elder Bran",
                giverType = NPCType.VILLAGE_ELDER,
                objective = "Vanquish the Shadow Lich Lord sealed in the Stormgate Castle Undercroft.",
                description = "Before the Ruin Colossus conquered the throne, a wicked Shadow Lich took root in the royal crypt below the keep.",
                targetArea = "Stormgate Castle",
                targetWorldX = 268f * 32f,
                targetCount = 1,
                reward = QuestReward(xp = 350, coins = 250, maxHealthBonus = 50, itemDescription = "Aegis Dragon Armor (Legendary Armor +50 Max HP)")
            )
        )
    }

    fun getQuest(id: String): SideQuest? = sideQuests.find { it.id == id }

    fun getSideQuestForNPC(npcType: NPCType): SideQuest? {
        return sideQuests.find { it.giverType == npcType && !it.isCompleted }
    }

    fun getSideQuestForNPC(npcTypeName: String): SideQuest? {
        return sideQuests.find { (it.giverType.name.equals(npcTypeName, ignoreCase = true)) && !it.isCompleted }
    }

    fun acceptSideQuest(id: String): Boolean = acceptQuest(id)

    fun acceptQuest(id: String): Boolean {
        val quest = getQuest(id) ?: return false
        if (!quest.isAccepted && !quest.isCompleted) {
            quest.isAccepted = true
            quest.isDiscovered = true
            return true
        }
        return false
    }

    fun updateProgress(id: String, increment: Int = 1, player: Player? = null): Boolean {
        val quest = getQuest(id) ?: return false
        if (quest.isCompleted) return false

        quest.isAccepted = true
        quest.isDiscovered = true
        quest.currentProgress = (quest.currentProgress + increment).coerceAtMost(quest.targetCount)

        if (quest.currentProgress >= quest.targetCount) {
            if (player != null) {
                return completeQuest(quest, player)
            } else {
                quest.isCompleted = true
                return true
            }
        }
        return false
    }

    fun completeSideQuest(id: String, player: Player? = null): Boolean {
        val quest = getQuest(id) ?: return false
        return if (player != null) {
            completeQuest(quest, player)
        } else {
            quest.isCompleted = true
            quest.currentProgress = quest.targetCount
            true
        }
    }

    fun completeQuest(quest: SideQuest, player: Player): Boolean {
        if (quest.isCompleted) return false
        quest.isCompleted = true
        quest.currentProgress = quest.targetCount

        // Add to player set
        player.completedSideQuestIds.add(quest.id)
        player.acceptedSideQuestIds.add(quest.id)

        // Grant rewards
        player.addXp(quest.reward.xp)
        player.coins += quest.reward.coins

        when (quest.id) {
            "sq_lost_traveler" -> {
                player.equippedAccessory = "Explorer's Compass"
            }
            "sq_village_supplies" -> {
                player.potions = (player.potions + 3).coerceAtMost(10)
            }
            "sq_hidden_treasure" -> {
                player.score += 500
            }
            "sq_forgotten_cave" -> {
                player.hasHeartFragment2 = true
                player.bonusMaxHealth += 25
                player.health = player.maxHealth
            }
            "sq_old_map" -> {
                player.unlockedAreas.addAll(listOf("Whispering Woods", "Oakhaven Hamlet", "Deeproot Caverns", "Ancient Ruins", "Frostpeak Summit", "The Lunar Shrine", "Stormgate Castle", "Throne of the Ruin Colossus"))
            }
            "sq_missing_guard" -> {
                player.equippedArmor = "Iron Vanguard Armor"
            }
            "sq_forest_beast" -> {
                player.bonusDamage += 6
                player.equippedWeapon = "Dreadfang Fang Blade"
            }
            "sq_ancient_key" -> {
                player.hasMasterKey = true
            }
            "sq_blacksmith_request" -> {
                player.bonusDamage += 12
                player.equippedWeapon = "Blazing Sunblade"
            }
            "sq_secret_shrine" -> {
                player.hasHeartFragment3 = true
                player.bonusMaxHealth += 25
                player.health = player.maxHealth
                player.equippedAccessory = "Hermes Winged Boots"
                player.moveSpeed = 260f
            }
            "sq_glacial_menace" -> {
                player.equippedArmor = "Frostguard Cloak"
            }
            "sq_phantom_king" -> {
                player.equippedArmor = "Aegis Dragon Armor"
                player.bonusMaxHealth += 50
                player.health = player.maxHealth
            }
        }
        return true
    }

    fun restoreState(
        accepted: Set<String> = emptySet(),
        completed: Set<String> = emptySet(),
        progressMap: Map<String, Int> = emptyMap(),
        acceptedIds: Set<String> = accepted,
        completedIds: Set<String> = completed
    ) {
        val actualAccepted = if (acceptedIds.isNotEmpty()) acceptedIds else accepted
        val actualCompleted = if (completedIds.isNotEmpty()) completedIds else completed
        for (q in sideQuests) {
            if (actualCompleted.contains(q.id)) {
                q.isCompleted = true
                q.isAccepted = true
                q.isDiscovered = true
                q.currentProgress = q.targetCount
            } else if (actualAccepted.contains(q.id)) {
                q.isAccepted = true
                q.isDiscovered = true
                q.currentProgress = progressMap[q.id] ?: 0
            }
        }
    }

    val completedCount: Int get() = sideQuests.count { it.isCompleted }
    val totalCount: Int get() = sideQuests.size
}
