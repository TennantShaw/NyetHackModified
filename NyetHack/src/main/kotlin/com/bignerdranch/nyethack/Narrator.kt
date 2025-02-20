package com.bignerdranch.nyethack

import kotlin.random.Random
import kotlin.random.nextInt

var narrationModifier: (String) -> String = { it }

inline fun narrate(
    message: String,
    modifier: (String) -> String = { narrationModifier(it) }
) {
    println(modifier(message))
}

fun changeNarratorMood() {
    val mood: String
    val modifier: (String) -> String

    when (Random.nextInt(1..4)) {
        1 -> {
            mood = "loud"
            modifier = { message ->
                val numExclamationPoints = 3
                message.uppercase() + "!".repeat(numExclamationPoints)
            }
        }
        2 -> {
            mood = "tired"
            modifier = { message ->
                message.lowercase().replace(" ", "... ")
            }
        }
        3 -> {
            mood = "unsure"
            modifier = { message ->
                message.replace(".", "?")
            }
        }
        else -> {
            mood = "proffessional"
            modifier = { message -> "$message."}
        }
    }

    narrationModifier = modifier
    narrate("The narrator begins to feel $mood.")
}