package com.example.game.entities

import com.example.game.model.RectF2D
import com.example.game.model.Vector2D

enum class NPCType {
    VILLAGE_ELDER,
    BLACKSMITH,
    MERCHANT,
    GUARD,
    TRAVELER,
    EXPLORER,
    SCHOLAR
}

data class DialogueLine(
    val speaker: String,
    val text: String,
    val triggerQuestId: Int? = null,
    val rewardAction: String? = null
) {
    val speakerName: String get() = speaker

    constructor(speaker: String, subtitle: String, message: String) : this(
        speaker = if (subtitle.isNotBlank()) "$speaker ($subtitle)" else speaker,
        text = message
    )
}

class NPC(
    val id: String,
    val type: NPCType,
    val pos: Vector2D,
    val facingRight: Boolean = true
) {
    val width = 28f
    val height = 48f
    var animTimer = (Math.random() * 5.0).toFloat()
    val bounds = RectF2D(pos.x - 10f, pos.y - 10f, pos.x + width + 10f, pos.y + height + 10f)

    val displayName: String = when (type) {
        NPCType.VILLAGE_ELDER -> "Elder Bran"
        NPCType.BLACKSMITH -> "Master Torin"
        NPCType.MERCHANT -> "Elowen the Merchant"
        NPCType.GUARD -> "Captain Vane"
        NPCType.TRAVELER -> "Roderick"
        NPCType.EXPLORER -> "Kaelen the Scout"
        NPCType.SCHOLAR -> "Archivist Lysandra"
    }

    val name: String get() = displayName

    val title: String = when (type) {
        NPCType.VILLAGE_ELDER -> "Village Elder"
        NPCType.BLACKSMITH -> "Royal Forge Master"
        NPCType.MERCHANT -> "Wandering Trader"
        NPCType.GUARD -> "Oakhaven Sentry"
        NPCType.TRAVELER -> "Wayward Voyager"
        NPCType.EXPLORER -> "Ruins Pathfinder"
        NPCType.SCHOLAR -> "Arcane Historian"
    }

    var hasQuestAvailable = true

    fun update(dt: Float) {
        animTimer += dt
        bounds.set(pos.x - 16f, pos.y - 16f, pos.x + width + 16f, pos.y + height + 16f)
    }

    fun getDialogue(questId: Int, playerHasKeys: Int, bossDefeated: Boolean): List<DialogueLine> {
        return when (type) {
            NPCType.VILLAGE_ELDER -> {
                when {
                    questId <= 1 -> listOf(
                        DialogueLine(displayName, "Thank the heavens you arrived! The Heart of Aether has shattered into fragments."),
                        DialogueLine(displayName, "Darkness stirs in the wild. Please, speak to Captain Vane and defend our gates.")
                    )
                    questId == 2 -> listOf(
                        DialogueLine(displayName, "Monsters are breaching our forest borders! Slay the beasts threatening our village.")
                    )
                    bossDefeated -> listOf(
                        DialogueLine(displayName, "You did it! The Colossus has fallen, and the Heart of Aether glows whole once more!")
                    )
                    else -> listOf(
                        DialogueLine(displayName, "Keep your blade sharp, traveler. The kingdom's salvation rests upon your courage.")
                    )
                }
            }
            NPCType.BLACKSMITH -> {
                listOf(
                    DialogueLine(displayName, "Need steel tempered by fire? The monsters carry hardened hides now."),
                    DialogueLine(displayName, "Here, take this whetstone. May your fiery broadsword strike true and deep!"),
                    DialogueLine(displayName, "Bring me ancient metals from the ruins, and I will hone your edge even sharper.")
                )
            }
            NPCType.MERCHANT -> {
                listOf(
                    DialogueLine(displayName, "Greetings, adventurer! Potions, elixirs, and survival gear for your expedition."),
                    DialogueLine(displayName, "Take this Vitality Draught on the house. You'll need it where you're going!")
                )
            }
            NPCType.GUARD -> {
                when {
                    questId < 3 -> listOf(
                        DialogueLine(displayName, "Halt! The Deeproot Cavern passage is perilous. Clear out the beast prowlers first!")
                    )
                    else -> listOf(
                        DialogueLine(displayName, "The gate to the subterranean cavern is unlocked. Watch the ceiling for lurking bats!")
                    )
                }
            }
            NPCType.TRAVELER -> {
                listOf(
                    DialogueLine(displayName, "The cold winds here give me chills. I fled the mountains when the stone pillars began humming."),
                    DialogueLine(displayName, "Beware the crumbling platforms over the chasms. Timing your leaps is everything.")
                )
            }
            NPCType.EXPLORER -> {
                when {
                    questId < 5 -> listOf(
                        DialogueLine(displayName, "I found the ancient ruins ahead! But a colossal Stone Guardian awoke at the altar."),
                        DialogueLine(displayName, "Strike its glowing rune core when it staggers after a ground slam!")
                    )
                    else -> listOf(
                        DialogueLine(displayName, "Incredible combat! The path up Frostpeak Summit is now accessible.")
                    )
                }
            }
            NPCType.SCHOLAR -> {
                when {
                    questId < 7 -> listOf(
                        DialogueLine(displayName, "The scriptures speak of three Golden Citadel Keys scattered in chests across the realm."),
                        DialogueLine(displayName, "Collect all three keys to open the grand portcullis of Stormgate Castle!")
                    )
                    else -> listOf(
                        DialogueLine(displayName, "The Aether Fragment at the Lunar Shrine resonates with your presence. Claim its divine power!")
                    )
                }
            }
        }
    }
}
