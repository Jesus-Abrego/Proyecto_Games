package com.proyecto.proyect_games.snake

import android.graphics.Color // Usaremos android.graphics.Color ahora

data class State(
    val targetLetterPosition: Pair<Int, Int>,
    val targetLetter: Char,
    val targetLetterColor: Int, // Cambiado a Int para android.graphics.Color
    val distractorLetterPosition: Pair<Int, Int>,
    val distractorLetter: Char,
    val distractorLetterColor: Int, // Cambiado a Int
    val snake: List<Pair<Int, Int>>,
    val score: Int
)