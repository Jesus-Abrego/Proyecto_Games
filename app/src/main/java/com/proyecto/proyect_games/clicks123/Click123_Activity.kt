package com.proyecto.proyect_games.clicks123

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.proyect_games.CatalogActivity
import com.proyecto.proyect_games.R

class Click123_Activity : AppCompatActivity() {

    private lateinit var textViewTime: TextView
    private lateinit var textViewScore: TextView
    private lateinit var buttonClick: Button
    private lateinit var buttonStart: Button
    private lateinit var buttonBackToCatalog: ImageButton

    private var tiempoRestanteEnSegundos: Int = 10
    private var puntuacion: Int = 0
    private var jugando: Boolean = false

    private var countDownTimer: CountDownTimer? = null
    private val tiempoTotalJuegoMs: Long = 10000 // 10 segundos en milisegundos
    private val intervaloTimerMs: Long = 1000 // 1 segundo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_click123)

        textViewTime = findViewById(R.id.textViewTime)
        textViewScore = findViewById(R.id.textViewScore)
        buttonClick = findViewById(R.id.buttonClick)
        buttonStart = findViewById(R.id.buttonStart)
        buttonBackToCatalog = findViewById(R.id.buttonBackToCatalog)

        backToCatalog()
        actualizarUI() // Configurar estado inicial de la UI

        buttonStart.setOnClickListener {
            if (!jugando) {
                iniciarJuego()
            }
        }

        buttonClick.setOnClickListener {
            if (jugando) {
                puntuacion++
                actualizarUI()
            }
        }
    }

    private fun backToCatalog() {
        buttonBackToCatalog.setOnClickListener {
            val intent = Intent(this, CatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun iniciarJuego() {
        puntuacion = 0
        tiempoRestanteEnSegundos = (tiempoTotalJuegoMs / 1000).toInt()
        jugando = true
        actualizarUI() // Muestra botón "¡Toca!", oculta "Iniciar"

        countDownTimer?.cancel() // Cancelar timer anterior si existe
        countDownTimer = object : CountDownTimer(tiempoTotalJuegoMs, intervaloTimerMs) {
            override fun onTick(millisUntilFinished: Long) {
                tiempoRestanteEnSegundos = (millisUntilFinished / 1000).toInt()
                actualizarUI()
            }

            override fun onFinish() {
                tiempoRestanteEnSegundos = 0
                jugando = false
                actualizarUI() // Muestra "Jugar de nuevo", oculta "¡Toca!"
            }
        }.start()
    }

    private fun actualizarUI() {
        textViewTime.text = "Tiempo: $tiempoRestanteEnSegundos"
        textViewScore.text = "Puntuación: $puntuacion"

        if (jugando) {
            buttonClick.visibility = View.VISIBLE
            buttonStart.visibility = View.GONE
        } else {
            buttonClick.visibility = View.GONE
            buttonStart.visibility = View.VISIBLE
            buttonStart.text = if (tiempoRestanteEnSegundos == 0 && puntuacion > 0) "Jugar de nuevo" else "Iniciar"
            if (tiempoRestanteEnSegundos == 0 && puntuacion == 0 && !buttonStart.text.equals("Iniciar")){
                }
            if (buttonStart.text.toString().isNotEmpty() && tiempoRestanteEnSegundos == 0 && puntuacion == 0) {
               }
            // Lógica más simple para el texto del botón de inicio/reinicio:
            if (tiempoRestanteEnSegundos == 0 && (puntuacion > 0 || countDownTimer != null) ) {
                // Si el tiempo es 0 Y (hubo puntuación O el timer ya ha corrido alguna vez)
                buttonStart.text = "Jugar de nuevo"
            } else {
                buttonStart.text = "Iniciar"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // Importante para evitar memory leaks
    }
}

