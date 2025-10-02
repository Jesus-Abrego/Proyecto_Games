package com.proyecto.proyect_games.snake

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Necesario para el scope de Game
import com.proyecto.proyect_games.CatalogActivity // Para volver al catálogo
import com.proyecto.proyect_games.R
import kotlinx.coroutines.launch

class Snake_Activity : AppCompatActivity() {

    private lateinit var game: Game
    private lateinit var snakeBoardView: SnakeBoardView // La crearemos en el siguiente paso
    private lateinit var textViewScore: TextView
    private lateinit var buttonPauseResume: Button
    private lateinit var buttonUp: Button
    private lateinit var buttonDown: Button
    private lateinit var buttonLeft: Button
    private lateinit var buttonRight: Button
    private lateinit var buttonBackToCatalog: ImageButton
    private lateinit var boardContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake) // Infla el XML

        // Inicializar vistas
        textViewScore = findViewById(R.id.textViewScore)
        buttonPauseResume = findViewById(R.id.buttonPauseResume)
        buttonUp = findViewById(R.id.buttonUp)
        buttonDown = findViewById(R.id.buttonDown)
        buttonLeft = findViewById(R.id.buttonLeft)
        buttonRight = findViewById(R.id.buttonRight)
        buttonBackToCatalog = findViewById(R.id.buttonBackToCatalog)
        boardContainer = findViewById(R.id.frameLayoutBoardContainer)

        // Crear e inicializar SnakeBoardView
        // Esperaremos a que el FrameLayout tenga dimensiones para pasarle el tamaño al tablero
        boardContainer.post { // Se ejecuta después de que el layout haya sido medido
            val boardSizePx = boardContainer.width // Usar el ancho ya que es cuadrado
            snakeBoardView = SnakeBoardView(this, boardSizePx)
            boardContainer.addView(snakeBoardView)

            // Inicializar el juego DESPUÉS de que snakeBoardView esté lista
            // y el layout medido
            initializeGame()
        }


        setupControls()
    }

    private fun initializeGame() {
        // Inicializar el juego
        game = Game(
            context = this,
            onStateUpdate = { newState ->
                // Este callback se ejecuta en el hilo donde Game lo llama (DefaultDispatcher)
                // Asegurarse de que las actualizaciones de UI se hagan en el hilo principal
                runOnUiThread {
                    Log.d("SnakeClassicActivity", "New state received: Score ${newState.score}")
                    textViewScore.text = "Score: ${newState.score}"
                    if (::snakeBoardView.isInitialized) {
                        snakeBoardView.updateState(newState)
                    }
                    // Ahora 'game' debería estar inicializada aquí de forma segura
                    // para las llamadas subsecuentes del bucle del juego.
                    buttonPauseResume.text = if (game.isPaused()) "Resume" else "Pause"
                }
            },
            scope = lifecycleScope // El bucle del juego se ata al ciclo de vida de la Activity
        )
// Después de que 'game' esté completamente inicializada:
        // 1. Obtener y aplicar el estado inicial
        val initialState = game.getInitialState()
        runOnUiThread { // Actualizar UI con estado inicial
            textViewScore.text = "Score: ${initialState.score}"
            if (::snakeBoardView.isInitialized) {
                snakeBoardView.updateState(initialState)
            }
            buttonPauseResume.text = "Pause" // Estado inicial no pausado
        }

        // 2. Iniciar el bucle del juego
        game.startGameLoop()
    }

    private fun setupControls() {
        buttonBackToCatalog.setOnClickListener {
            val intent = Intent(this, CatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        buttonPauseResume.setOnClickListener {
            game.togglePause()
            // El texto del botón se actualiza en el callback onStateUpdate
        }

        buttonUp.setOnClickListener {
            lifecycleScope.launch { game.changeDirection(Pair(0, -1)) } // Arriba
        }
        buttonDown.setOnClickListener {
            lifecycleScope.launch { game.changeDirection(Pair(0, 1)) } // Abajo
        }
        buttonLeft.setOnClickListener {
            lifecycleScope.launch { game.changeDirection(Pair(-1, 0)) } // Izquierda
        }
        buttonRight.setOnClickListener {
            lifecycleScope.launch { game.changeDirection(Pair(1, 0)) } // Derecha
        }
    }

    override fun onPause() {
        super.onPause()
        if (::game.isInitialized && !game.isPaused()) { // Solo pausar si el juego está corriendo
            game.togglePause() // Pausar el juego si la Activity se pausa
        }
    }

    override fun onResume() {
        super.onResume()
        // No reanudamos automáticamente aquí, el usuario debe usar el botón "Resume"
        // o podríamos añadir una lógica para reanudar si no fue una pausa manual.
        // Por simplicidad, dejamos que el botón de pausa/resumen maneje esto.
        // Actualizar el texto del botón por si acaso.
        if (::game.isInitialized && ::buttonPauseResume.isInitialized) {
            buttonPauseResume.text = if (game.isPaused()) "Resume" else "Pause"
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::game.isInitialized) {
            game.cancelScope()    // Detener el bucle del juego
            game.releaseSounds()  // Liberar recursos de SoundPool
        }
    }
}
