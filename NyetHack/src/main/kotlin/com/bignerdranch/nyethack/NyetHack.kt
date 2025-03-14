package com.bignerdranch.nyethack

import kotlin.system.exitProcess

lateinit var player: Player

fun main() {
    narrate("Welcome to NyetHack!")
    val playerName = promptHeroName()
    player = Player(playerName)
    changeNarratorMood()
    narrate("""
        |You can already hear a faint whisper of the future. The sound of the
        |people calling out your name, ${player.name}, the, ${player.title}.
    """.trimMargin())
    Game.displayUserCommands()
    Game.play()
}

private fun promptHeroName(): String {
    narrate("""
        |You enter town as the fall leaves twirl and fall in the breeze. 
        |You smile as you think about the the townsfolk praising your name.
        |Which happens to be?:
    """.trimMargin()) { message ->
        // Prints the message in yellow
        "\u001b[33;1m$message\u001b[0m"
    }
    val input = readLine()
    require(input != null && input.isNotEmpty()) {
        "The hero must have a name."
    }

    return input
}

object Game {
    private val worldMap = listOf(
        listOf(TownSquare(), Tavern(), Room("Back Room")),
        listOf(MonsterRoom("A Long Corridor"), Room("A Generic Room")),
        listOf(MonsterRoom("The Dungeon"))
    )
    private var currentRoom: Room = worldMap[0][0]
    private var currentPosition = Coordinate(0, 0)

    init {
        narrate("Welcome, adventurer")
        val mortality = if (player.isImmortal) "an immortal" else "a mortal"
        narrate("${player.name}, $mortality, has ${player.healthPoints} health points.")
    }

    // A function that displays the possible commands a player has available to them.
    fun displayUserCommands() {
        narrate("""
            |The available commands to use in NyetHack are:
            |move (cardinal direction) - i.e. move north, move south, move east, move west. This will move your player in the direction specified.
            |fight - If a monster is currently in the room, this command will engage combat. Combat is currently automatic and will end when the monster or player is defeated.
            |take loot - If their is a chest in the room, the player will open it and retrieve the contents.
            |sell loot - If you are located in a room with a sellers box, you will automatically sell everything sellable in your inventory.
        """.trimMargin())
    }

    // A function that gives details about the rooms that can be entered.
    fun getRoomDetails(currentRoom: Room) {
        TODO()
        val currentRoom = currentRoom
        val northernRoom: Room
        val easternRoom: Room
        val southernRoom: Room
        val weasternRoom: Room
        // Get a list of the rooms attached to player's current room
        // Display details about each room that player can enter
    }

    fun play() {
        while (true) {
            narrate("""
                |${player.name} currently stands in ${currentRoom.description()}.
            """.trimMargin())
            currentRoom.enterRoom()

            print(">Enter your command: ")
            GameInput(readLine()).processCommand()
        }
    }

    fun move(direction: Direction) {
        val newPosition = currentPosition move direction
        val newRoom = worldMap[newPosition].orEmptyRoom()

        narrate("The hero moves ${direction.name}")
        currentPosition = newPosition
        currentRoom = newRoom
    }

    fun fight() {
        val monsterRoom = currentRoom as? MonsterRoom
        val currentMonster = monsterRoom?.monster
        if (currentMonster == null) {
            narrate("There's nothing to fight here.")
            return
        }

        var combatRound = 0
        val previousNarrationModifier = narrationModifier
        narrationModifier = { it.addEnthusiasm(enthusiasmLevel = combatRound) }

        while (player.healthPoints > 0 && currentMonster.healthPoints > 0) {
            combatRound++
            player.attack(currentMonster)
            if (currentMonster.healthPoints > 0) {
                currentMonster.attack(player)
            }
            Thread.sleep(1000)
        }
        narrationModifier = previousNarrationModifier

        if (player.healthPoints <= 0) {
            narrate("You have been defeated! Thanks for playing.")
            exitProcess(0)
        } else {
            narrate("${currentMonster.name} has been defeated")
            monsterRoom.monster = null
        }
    }

    fun takeLoot() {
        val loot = currentRoom.lootBox.takeLoot()
        if (loot == null) {
            narrate("${player.name} approaches the loot box, but it is empty.")
        } else {
            narrate("${player.name} now has a ${loot.name}")
            player.inventory += loot
        }
    }

    fun sellLoot() {
        when (val currentRoom = currentRoom) {
            is TownSquare -> {
                player.inventory.forEach { item ->
                    if (item is Sellable) {
                        val sellPrice = currentRoom.sellLoot(item)
                        narrate("Sold ${item.name} for $sellPrice gold.")
                        player.gold += sellPrice
                    } else {
                        narrate("Your ${item.name} can't be sold.")
                    }
                }
                player.inventory.removeAll { it is Sellable }
            }
            else -> narrate("You cannot sell anything here.")
        }
    }

    private class GameInput(arg: String?) {
        private val input = arg ?: ""
        val command = input.split(" ")[0]
        val argument = input.split(" ").getOrElse(1) { "" }

        fun processCommand() = when (command.lowercase()) {
            "fight" -> fight()
            "controls" -> displayUserCommands()
            "move" -> {
                val direction = Direction.values().firstOrNull { it.name.equals(argument, ignoreCase = true) }
                if (direction != null) {
                    move(direction)
                } else {
                    narrate("I don't know what direction that is.")
                }
            }
            "take" -> {
                if (argument.equals("loot", ignoreCase = true)) {
                    takeLoot()
                } else {
                    narrate("I don't know what you're trying to take.")
                }
            }
            "sell" -> {
                if (argument.equals("loot", ignoreCase = true)) {
                    sellLoot()
                } else {
                    narrate("I don't know what you're trying to sell.")
                }
            }
            else -> narrate("I'm not sure what you're trying to do.")
        }
    }
}
