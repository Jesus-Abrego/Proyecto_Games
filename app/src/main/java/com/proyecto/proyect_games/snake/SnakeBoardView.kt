package com.proyecto.proyect_games.snake

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.proyecto.proyect_games.snake.Game // Asegúrate de importar tu clase Game
import com.proyecto.proyect_games.snake.State // Y tu clase State

class SnakeBoardView @JvmOverloads constructor(
    context: Context,
    private val boardPixelSize: Int, // Tamaño total del tablero en píxeles
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentGameState: State? = null
    private var cellSize: Float = 0f

    private val snakePaint = Paint().apply {
        color = Color.GREEN // Color por defecto para la serpiente
        style = Paint.Style.FILL
    }
    private val headPaint = Paint().apply { // Diferenciar la cabeza
        color = Color.rgb(0, 150, 0) // Verde más oscuro para la cabeza
        style = Paint.Style.FILL
    }
    private val foodPaint = Paint().apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 30f // Ajustaremos esto dinámicamente
        isAntiAlias = true
    }
    private val borderPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 4f // Grosor del borde
    }
    private val gridPaint = Paint().apply { // Para dibujar la cuadrícula (opcional)
        color = Color.argb(50, 128, 128, 128) // Gris semitransparente
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val textBoundsPaint = Paint() // Para centrar el texto correctamente

    init {
        // Calcular el tamaño de cada celda basado en el tamaño del tablero en píxeles
        // y el tamaño lógico del tablero (Game.BOARD_SIZE)
        if (Game.BOARD_SIZE > 0) {
            cellSize = boardPixelSize.toFloat() / Game.BOARD_SIZE
            foodPaint.textSize = cellSize * 0.7f // Ajustar tamaño de texto a la celda
        }
    }

    /**
     * Este método será llamado desde la Activity para actualizar el estado del juego
     * y solicitar un redibujado.
     */
    fun updateState(newState: State) {
        this.currentGameState = newState
        invalidate() // Muy importante: solicita que la vista se redibuje
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Hacemos que la vista sea cuadrada usando el boardPixelSize que nos pasaron
        val finalSize = MeasureSpec.makeMeasureSpec(boardPixelSize, MeasureSpec.EXACTLY)
        setMeasuredDimension(finalSize, finalSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Si no hay canvas o estado, no dibujar nada
        if (currentGameState == null) {
            return
        }

        // 1. Dibujar el borde del tablero
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)

        // 2. Dibujar la cuadrícula (opcional, pero útil para depurar)
        // for (i in 0 until Game.BOARD_SIZE) {
        //     canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), gridPaint)
        //     canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, gridPaint)
        // }

        // 3. Dibujar la serpiente
        currentGameState!!.snake.forEachIndexed { index, part ->
            val left = part.first * cellSize
            val top = part.second * cellSize
            val right = left + cellSize
            val bottom = top + cellSize
            // Usar un color diferente para la cabeza
            val paintToUse = if (index == 0) headPaint else snakePaint
            canvas.drawRect(left, top, right, bottom, paintToUse)
        }

        // 4. Dibujar la letra objetivo (comida)
        drawLetter(canvas, currentGameState!!.targetLetter, currentGameState!!.targetLetterPosition, currentGameState!!.targetLetterColor)

        // 5. Dibujar la letra distractora
        drawLetter(canvas, currentGameState!!.distractorLetter, currentGameState!!.distractorLetterPosition, currentGameState!!.distractorLetterColor)
    }

    private fun drawLetter(canvas: Canvas, letter: Char, position: Pair<Int, Int>, color: Int) {
        foodPaint.color = color
        val x = (position.first * cellSize) + (cellSize / 2)
        val y = (position.second * cellSize) + (cellSize / 2) - ((foodPaint.descent() + foodPaint.ascent()) / 2) // Centrar verticalmente

        canvas.drawText(letter.toString(), x, y, foodPaint)
    }
}

