package com.proyecto.proyect_games.snake

import android.content.Context
import android.graphics.Color // Usaremos android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.proyecto.proyect_games.R // Asegúrate de tener tus sonidos en res/raw
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class Game(
    private val context: Context,
    private val onStateUpdate: (State) -> Unit, // Callback para actualizar la UI
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default) // Alcance para el bucle del juego
) {

    // Colores como Ints (android.graphics.Color)
    private val foodColors: List<Int> = listOf(
        Color.RED,
        Color.rgb(0, 123, 255), // Bright Blue
        Color.YELLOW,
        Color.MAGENTA,
        Color.WHITE,
        Color.rgb(255, 165, 0), // Orange
        Color.CYAN
    )
    private val random = Random.Default

    private lateinit var soundPool: SoundPool
    private var correctEatSoundId: Int = 0
    private var wrongEatSoundId: Int = 0

    private var isPaused = false
    private val mutex = Mutex() // Para la dirección del movimiento

    var currentMove = Pair(1, 0)
        private set // La Activity lo cambiará a través de un método

    private var gameState: State = createInitialGameState()

    // Handler para ejecutar el bucle en el hilo principal si es necesario para la UI
    // O podemos usar el callback directamente desde la corutina si la UI lo maneja bien.
    // Por ahora, el callback se llamará desde la corutina del juego.

    init {
        loadSounds()
        // NO llamar a onStateUpdate aquí
    }
    // Nuevo método para obtener el estado inicial
    fun getInitialState(): State {
        return gameState
    }

    fun startGameLoop() {
        if (scope.isActive) { // Evitar múltiples bucles si ya está corriendo
            Log.d("GameClassic", "Game loop already active or scope is not new.")
            // Puedes decidir si reiniciar o simplemente no hacer nada.
            // Por simplicidad, si ya está activo, no hacemos nada extra aquí.
        }

        scope.launch {
            var snakeLength = INITIAL_SNAKE_LENGTH
            var currentScore = 0

            while (isActive) { // El bucle se ejecutará mientras la corutina esté activa
                if (isPaused) {
                    delay(100L)
                    continue
                }

                val foodEaten = snakeLength - INITIAL_SNAKE_LENGTH
                val currentDelay = BASE_DELAY_MS - (foodEaten * DELAY_DECREASE_PER_FOOD_MS)
                val actualDelay = currentDelay.coerceAtLeast(MIN_DELAY_MS)

                delay(actualDelay)

                val newHeadPosition: Pair<Int, Int>
                // Acceso seguro a currentMove
                mutex.withLock {
                    newHeadPosition = gameState.snake.first().let { poz ->
                        Pair(
                            (poz.first + currentMove.first + BOARD_SIZE) % BOARD_SIZE,
                            (poz.second + currentMove.second + BOARD_SIZE) % BOARD_SIZE
                        )
                    }
                }

                var nextTargetLetter = gameState.targetLetter
                var nextTargetLetterColor = gameState.targetLetterColor
                var nextTargetLetterPosition = gameState.targetLetterPosition

                var nextDistractorLetter = gameState.distractorLetter
                var nextDistractorLetterColor = gameState.distractorLetterColor
                var nextDistractorLetterPosition = gameState.distractorLetterPosition
                var newScore = currentScore

                val potentialNextSnake = listOf(newHeadPosition) + gameState.snake.take(snakeLength - 1)

                if (newHeadPosition == gameState.targetLetterPosition) {
                    snakeLength++
                    newScore += 5
                    playSound(correctEatSoundId)
                    val grownSnake = listOf(newHeadPosition) + gameState.snake.take(snakeLength - 1)
                    nextTargetLetter = if (gameState.targetLetter == 'z') 'a' else gameState.targetLetter + 1
                    nextTargetLetterColor = foodColors.random(random)
                    nextTargetLetterPosition = generateRandomSafePosition(grownSnake)
                    nextDistractorLetter = generateRandomLetter(exclude = nextTargetLetter)
                    nextDistractorLetterColor = foodColors.random(random)
                    nextDistractorLetterPosition = generateRandomSafePosition(grownSnake, nextTargetLetterPosition)
                } else if (newHeadPosition == gameState.distractorLetterPosition) {
                    newScore -= 5
                    playSound(wrongEatSoundId)
                    nextDistractorLetter = generateRandomLetter(exclude = gameState.targetLetter)
                    nextDistractorLetterColor = foodColors.random(random)
                    nextDistractorLetterPosition = generateRandomSafePosition(potentialNextSnake, gameState.targetLetterPosition)
                } else if (gameState.snake.contains(newHeadPosition)) { // Auto-colisión
                    snakeLength = INITIAL_SNAKE_LENGTH
                    newScore = 0
                    val resetSnake = listOf(Pair(7,7)) // Posición inicial simple para reseteo
                    nextTargetLetter = 'a'
                    nextTargetLetterColor = foodColors.random(random)
                    nextTargetLetterPosition = generateRandomSafePosition(resetSnake)
                    nextDistractorLetter = generateRandomLetter(exclude = nextTargetLetter)
                    nextDistractorLetterColor = foodColors.random(random)
                    nextDistractorLetterPosition = generateRandomSafePosition(resetSnake, nextTargetLetterPosition)
                }

                currentScore = newScore
                gameState = gameState.copy(
                    targetLetterPosition = nextTargetLetterPosition,
                    targetLetter = nextTargetLetter,
                    targetLetterColor = nextTargetLetterColor,
                    distractorLetterPosition = nextDistractorLetterPosition,
                    distractorLetter = nextDistractorLetter,
                    distractorLetterColor = nextDistractorLetterColor,
                    snake = listOf(newHeadPosition) + gameState.snake.take(snakeLength - 1),
                    score = newScore
                )
                // Llamar al callback para actualizar la UI
                onStateUpdate(gameState)
            }
        }
    }

    suspend fun changeDirection(newMove: Pair<Int, Int>) {
        mutex.withLock {
            // Evitar que la serpiente se mueva inmediatamente en la dirección opuesta
            if (currentMove.first + newMove.first != 0 || currentMove.second + newMove.second != 0) {
                currentMove = newMove
            }
        }
    }

    fun togglePause() {
        isPaused = !isPaused
        // Actualizar UI para mostrar estado de pausa/resumen si es necesario (el callback onStateUpdate podría hacerlo)
        onStateUpdate(gameState.copy(score = gameState.score)) // Re-emitir estado para actualizar botón de pausa
    }

    fun isPaused(): Boolean = isPaused

    private fun generateRandomLetter(exclude: Char? = null): Char {
        var letter: Char
        do {
            letter = ('a'..'z').random(random)
        } while (letter == exclude)
        return letter
    }

    private fun generateRandomSafePosition(
        currentSnake: List<Pair<Int, Int>>,
        vararg occupiedSpots: Pair<Int, Int>
    ): Pair<Int, Int> {
        var position: Pair<Int, Int>
        val allOccupied = currentSnake.toSet() + occupiedSpots.filterNotNull().toSet()
        do {
            position = Pair(random.nextInt(BOARD_SIZE), random.nextInt(BOARD_SIZE))
        } while (allOccupied.contains(position))
        return position
    }

    private fun createInitialGameState(): State {
        val initialSnakeBody = listOf(Pair(7, 7))
        val initialTargetLetter = 'a'
        val initialTargetLetterColor = foodColors.random(random)
        val initialTargetLetterPosition = generateRandomSafePosition(initialSnakeBody)
        val initialDistractorLetter = generateRandomLetter(exclude = initialTargetLetter)
        val initialDistractorLetterColor = foodColors.random(random)
        val initialDistractorLetterPosition =
            generateRandomSafePosition(initialSnakeBody, initialTargetLetterPosition)
        return State(
            targetLetterPosition = initialTargetLetterPosition,
            targetLetter = initialTargetLetter,
            targetLetterColor = initialTargetLetterColor,
            distractorLetterPosition = initialDistractorLetterPosition,
            distractorLetter = initialDistractorLetter,
            distractorLetterColor = initialDistractorLetterColor,
            snake = initialSnakeBody,
            score = 0
        )
    }

    private fun loadSounds() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            correctEatSoundId = soundPool.load(context, R.raw.correct_eat, 1)
            wrongEatSoundId = soundPool.load(context, R.raw.wrong_eat, 1)
        } catch (e: Exception) {
            Log.e("GameSounds", "Error loading sounds", e)
        }
    }

    private fun playSound(soundId: Int) {
        if (soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    fun releaseSounds() {
        soundPool.release()
    }

    fun cancelScope() {
        scope.cancel() // Cancelar la corutina cuando la Activity se destruye
    }


    companion object {
        const val BOARD_SIZE = 32 // Mantener el mismo tamaño del tablero
        const val INITIAL_SNAKE_LENGTH = 4
        const val BASE_DELAY_MS = 200L
        const val MIN_DELAY_MS = 50L
        const val DELAY_DECREASE_PER_FOOD_MS = 5L
    }
}
