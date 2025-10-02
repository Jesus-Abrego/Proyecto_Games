package com.proyecto.proyect_games.solitario.modelo

import java.util.Stack

class Mazo {
    private val cartas: Stack<Carta> = Stack()

    init {
        reiniciarMazo()
    }

    fun reiniciarMazo() {
        cartas.clear()
        for (palo in Palo.values()) {
            for (valor in Valor.values()) {
                // Se crea una carta por cada combinación de palo y valor.
                // Los enums Palo y Valor ya definen una baraja estándar de 52 cartas.
                cartas.add(Carta(palo, valor, esVisible = false, estaEnMazoPrincipal = true))
            }
        }
        barajar()
    }

    fun barajar() {
        cartas.shuffle()
    }

    fun robarCarta(): Carta? {
        return if (cartas.isNotEmpty()) {
            val carta = cartas.pop()
            carta.estaEnMazoPrincipal = false
            carta
        } else {
            null
        }
    }

    fun estaVacio(): Boolean = cartas.isEmpty()

    fun rellenarDesdeDescarte(cartasDescarte: MutableList<Carta>) {
        if (cartas.isEmpty() && cartasDescarte.isNotEmpty()) {
            cartasDescarte.forEach {
                it.esVisible = false
                it.estaEnMazoPrincipal = true
            }
            cartas.addAll(cartasDescarte.reversed())
            cartasDescarte.clear()
            // No se baraja al rellenar desde el descarte en Klondike.
        }
    }

    fun obtenerNumeroCartas(): Int = cartas.size
}
