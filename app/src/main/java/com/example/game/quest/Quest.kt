package com.example.game.quest

data class QuestReward(
    val xp: Int = 0,
    val coins: Int = 0,
    val maxHealthBonus: Int = 0,
    val damageBonus: Int = 0,
    val potions: Int = 0,
    val itemDescription: String = ""
) {
    val rewardItemName: String get() = itemDescription
}

data class Quest(
    val id: Int,
    val title: String,
    val description: String,
    val objective: String,
    val targetArea: String,
    val targetWorldX: Float,
    val requiredProgress: Int = 1,
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false,
    val reward: QuestReward
) {
    val targetCount: Int get() = requiredProgress
    val progressText: String
        get() = if (requiredProgress > 1) "($currentProgress / $requiredProgress)" else if (isCompleted) "Completed" else "In Progress"
}

class QuestManager {
    val quests = mutableListOf<Quest>()
    var activeQuestIndex = 0

    init {
        initializeQuests()
    }

    private fun initializeQuests() {
        quests.clear()
        quests.add(
            Quest(
                id = 1,
                title = "The First Warning",
                description = "Dark energy has shattered the Heart of Aether. Journey through the Whispering Woods to reach Oakhaven Village and speak with Elder Bran.",
                objective = "Travel through the forest and reach the village.",
                targetArea = "Oakhaven Hamlet",
                targetWorldX = 1400f,
                requiredProgress = 1,
                reward = QuestReward(xp = 60, coins = 40, itemDescription = "Elder's Blessing")
            )
        )
        quests.add(
            Quest(
                id = 2,
                title = "Village in Danger",
                description = "Aggressive forest crawlers are attacking the outskirts of Oakhaven. Drive back the invasion to protect the villagers.",
                objective = "Defend the village by slaying 4 invading beasts.",
                targetArea = "Oakhaven Hamlet",
                targetWorldX = 1800f,
                requiredProgress = 4,
                reward = QuestReward(xp = 100, coins = 60, damageBonus = 3, itemDescription = "Honed Steel Blade")
            )
        )
        quests.add(
            Quest(
                id = 3,
                title = "Into the Cave",
                description = "Deep underground vibrations indicate dark energy swelling inside Deeproot Caverns. Delve into the subterranean depths.",
                objective = "Investigate the ancient cave and find the cavern checkpoint.",
                targetArea = "Deeproot Caverns",
                targetWorldX = 3200f,
                requiredProgress = 1,
                reward = QuestReward(xp = 120, coins = 80, potions = 1, itemDescription = "Cavern Crystal Shard")
            )
        )
        quests.add(
            Quest(
                id = 4,
                title = "The Broken Seal",
                description = "The ancient seal guarding the forgotten kingdom ruins has collapsed. Scout the mossy ruins and meet Scout Kaelen.",
                objective = "Explore the ancient ruins beyond the underground chasm.",
                targetArea = "Ancient Forgotten Ruins",
                targetWorldX = 4600f,
                requiredProgress = 1,
                reward = QuestReward(xp = 150, coins = 100, maxHealthBonus = 20, itemDescription = "Runic Vitality Charm")
            )
        )
        quests.add(
            Quest(
                id = 5,
                title = "The Watcher",
                description = "An ancient awakened Stone Giant guards the mountain pass. Slay the guardian to clear the ascent.",
                objective = "Defeat the ancient stone guardian in the ruins.",
                targetArea = "Ancient Forgotten Ruins",
                targetWorldX = 5200f,
                requiredProgress = 1,
                reward = QuestReward(xp = 220, coins = 140, damageBonus = 5, itemDescription = "Titan's Ring (+5 ATK)")
            )
        )
        quests.add(
            Quest(
                id = 6,
                title = "Mountain Path",
                description = "Ascend the treacherous snowy cliffs of Frostpeak Summit, navigating glacial ice and perilous mountain ridges.",
                objective = "Cross the dangerous mountain to the peak beacon.",
                targetArea = "Frostpeak Summit",
                targetWorldX = 6200f,
                requiredProgress = 1,
                reward = QuestReward(xp = 260, coins = 150, potions = 2, itemDescription = "Elixir of Frost Resistance")
            )
        )
        quests.add(
            Quest(
                id = 7,
                title = "The Hidden Shrine",
                description = "Locate the sacred Lunar Shrine hidden among the peaks and claim the radiant Heart of Aether Fragment.",
                objective = "Find another Heart fragment at the sacred shrine altar.",
                targetArea = "The Hidden Lunar Shrine",
                targetWorldX = 7400f,
                requiredProgress = 1,
                reward = QuestReward(xp = 350, coins = 200, maxHealthBonus = 25, itemDescription = "Aether Heart Fragment I")
            )
        )
        quests.add(
            Quest(
                id = 8,
                title = "The Castle Gate",
                description = "Stormgate Castle is locked tight with triple enchanted ward locks. Reach the gatehouse and inspect the barrier.",
                objective = "Find the castle gatehouse and examine the locked barrier.",
                targetArea = "Stormgate Castle Keep",
                targetWorldX = 8600f,
                requiredProgress = 1,
                reward = QuestReward(xp = 300, coins = 180, itemDescription = "Castle Ward Sigil")
            )
        )
        quests.add(
            Quest(
                id = 9,
                title = "Three Keys",
                description = "Three Ancient Citadel Keys are locked inside royal treasure chests across the realm. Recover all three to dispel the ward.",
                objective = "Recover three ancient keys from chests throughout the realm.",
                targetArea = "Across the Realm",
                targetWorldX = 8600f,
                requiredProgress = 3,
                reward = QuestReward(xp = 400, coins = 250, damageBonus = 8, itemDescription = "Master Key of the Keep")
            )
        )
        quests.add(
            Quest(
                id = 10,
                title = "The Heart of Aether",
                description = "The final corrupted guardian, Gorgaroth the Ruin Colossus, holds the central core. Slay the colossus to restore the kingdom!",
                objective = "Enter the castle interior and confront the corrupted antagonist.",
                targetArea = "Throne of the Ruin Colossus",
                targetWorldX = 9900f,
                requiredProgress = 1,
                reward = QuestReward(xp = 1000, coins = 1000, maxHealthBonus = 50, itemDescription = "The Restored Heart of Aether")
            )
        )
    }

    val currentQuest: Quest?
        get() = if (activeQuestIndex in quests.indices) quests[activeQuestIndex] else null

    fun updateProgress(questId: Int, progressDelta: Int = 1): Boolean {
        val quest = quests.find { it.id == questId } ?: return false
        if (quest.isCompleted) return false

        quest.currentProgress = (quest.currentProgress + progressDelta).coerceAtMost(quest.requiredProgress)
        if (quest.currentProgress >= quest.requiredProgress) {
            quest.isCompleted = true
            if (activeQuestIndex < quests.size - 1 && quest.id == quests[activeQuestIndex].id) {
                activeQuestIndex++
            }
            return true // Just completed!
        }
        return false
    }

    fun completeActiveQuest(): Quest? {
        val quest = currentQuest ?: return null
        quest.currentProgress = quest.requiredProgress
        quest.isCompleted = true
        if (activeQuestIndex < quests.size - 1) {
            activeQuestIndex++
        }
        return quest
    }

    fun restoreState(completedQuestIds: List<Int>, activeId: Int, progress: Int) {
        for (q in quests) {
            if (completedQuestIds.contains(q.id)) {
                q.isCompleted = true
                q.currentProgress = q.requiredProgress
            }
        }
        val targetIndex = quests.indexOfFirst { it.id == activeId }
        if (targetIndex != -1) {
            activeQuestIndex = targetIndex
            quests[targetIndex].currentProgress = progress
        }
    }
}
