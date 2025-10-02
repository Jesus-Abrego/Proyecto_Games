package com.proyecto.proyect_games.solitario.modelo

enum class Palo {
    CORAZONES, DIAMANTES, PICAS, TREBOLES
}

enum class Valor(val valorNumerico: Int) {
    AS(1), DOS(2), TRES(3), CUATRO(4), CINCO(5), SEIS(6),
    SIETE(7), OCHO(8), NUEVE(9), DIEZ(10), JOTA(11), REINA(12), REY(13)
}

data class Carta(
    val palo: Palo,
    val valor: Valor,
    var esVisible: Boolean = false,
    var estaEnMazoPrincipal: Boolean = true
) {
    fun obtenerNombreImagen(): String {
        val valorBaseStr = when (valor) {
            Valor.AS -> "ace" // Los ases generalmente no empiezan con número, así que no necesitan la 'c'
            Valor.DOS -> "2"
            Valor.TRES -> "3"
            Valor.CUATRO -> "4"
            Valor.CINCO -> "5"
            Valor.SEIS -> "6"
            Valor.SIETE -> "7"
            Valor.OCHO -> "8"
            Valor.NUEVE -> "9"
            Valor.DIEZ -> "10"
            Valor.JOTA -> "jack" // J, Q, K tampoco necesitan la 'c' inicial
            Valor.REINA -> "queen"
            Valor.REY -> "king"
        }

        // Añadir la 'c' inicial SOLO si el valor base empieza con un dígito
        val valorStr = if (valorBaseStr.first().isDigit()) {
            "c$valorBaseStr"
        } else {
            valorBaseStr
        }

        val paloStr = when (palo) {
            Palo.CORAZONES -> "hearts"
            Palo.DIAMANTES -> "diamonds"
            Palo.PICAS -> "spades"
            Palo.TREBOLES -> "clubs"
        }
        // Genera nombres como "ace_of_clubs", "c2_of_diamonds", "c10_of_hearts", "jack_of_spades" etc.
        return "${valorStr}_of_${paloStr}"
    }

    fun esRoja(): Boolean = palo == Palo.CORAZONES || palo == Palo.DIAMANTES
    fun esNegra(): Boolean = palo == Palo.PICAS || palo == Palo.TREBOLES
}
