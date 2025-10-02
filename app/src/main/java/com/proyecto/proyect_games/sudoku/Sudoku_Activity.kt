package com.proyecto.proyect_games.sudoku

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.proyect_games.CatalogActivity
import com.proyecto.proyect_games.R

class Sudoku_Activity : AppCompatActivity() {

    private lateinit var sudokuGrid: GridLayout
    private lateinit var numberButtons: List<Button>
    private lateinit var btnReset: Button
    private lateinit var btnDelete: Button

    private lateinit var buttonBackToCatalog: ImageButton

    private var selectedCell: TextView? = null
    private val cells = Array(9) { Array<TextView?>(9) { null } }

    // 🎨 Colores personalizados
    private val colorBlock1 = Color.parseColor("#3A3A50") // gris oscuro
    private val colorBlock2 = Color.parseColor("#2C2C3E") // gris más oscuro
    private val colorSelected = Color.parseColor("#74B9FF") // azul para celda seleccionada
    private val colorText = Color.WHITE  // texto siempre blanco
    private val colorConflict = Color.parseColor("#FF4C60") // rojo para conflictos
    private val colorHighlight = Color.parseColor("#FFD32A") // amarillo para celdas con mismo número

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        sudokuGrid = findViewById(R.id.sudokuGrid)
        btnReset = findViewById(R.id.btnReset)
        buttonBackToCatalog = findViewById(R.id.buttonBackToCatalog)

        // Inicializar los botones numéricos
        numberButtons = listOf(
            findViewById(R.id.btn1),
            findViewById(R.id.btn2),
            findViewById(R.id.btn3),
            findViewById(R.id.btn4),
            findViewById(R.id.btn5),
            findViewById(R.id.btn6),
            findViewById(R.id.btn7),
            findViewById(R.id.btn8),
            findViewById(R.id.btn9)
        )

        backToCatalog()
        setupGrid()
        setupNumberButtons()
        setupResetButton()
        setupDeleteButton()
    }

    private fun backToCatalog() {
        buttonBackToCatalog.setOnClickListener {
            val intent = Intent(this, CatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupGrid() {
        sudokuGrid.removeAllViews()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = TextView(this)
                val params = GridLayout.LayoutParams()
                params.rowSpec = GridLayout.spec(row, 1f)
                params.columnSpec = GridLayout.spec(col, 1f)
                params.width = 0
                params.height = 0
                params.setMargins(1, 1, 1, 1)

                cell.layoutParams = params
                cell.setTextColor(colorText)
                cell.textSize = 18f
                cell.gravity = Gravity.CENTER
                cell.setBackgroundColor(getBlockColor(row, col))

                // Evento al tocar una celda
                cell.setOnClickListener {
                    restoreCellColors()
                    selectedCell = cell
                    selectedCell?.setBackgroundColor(colorSelected)
                    highlightSameNumbers()
                }

                sudokuGrid.addView(cell)
                cells[row][col] = cell
            }
        }
    }

    // 🎨 Alterna colores según bloque 3x3
    private fun getBlockColor(row: Int, col: Int): Int {
        return if ((row / 3 + col / 3) % 2 == 0) colorBlock1 else colorBlock2
    }

    private fun setupNumberButtons() {
        for (button in numberButtons) {
            button.setOnClickListener {
                selectedCell?.let { cell ->
                    cell.text = button.text
                    cell.background = makeCircle(Color.parseColor("#4ECCA3")) // círculo verde
                    cell.setTextColor(Color.BLACK) // números visibles
                    validateConflicts()
                    highlightSameNumbers()
                }
            }
        }
    }

    private fun setupDeleteButton() {
        // Crear botón dinámico
        btnDelete = Button(this).apply {
            text = "Borrar"
            setBackgroundColor(Color.parseColor("#FFA502")) // naranja
            setTextColor(Color.WHITE)
        }

        // Insertar en el numberPad
        val numberPad = findViewById<LinearLayout>(R.id.numberPad)
        numberPad.addView(btnDelete)

        // Acción borrar
        btnDelete.setOnClickListener {
            selectedCell?.let { cell ->
                cell.text = ""
                val row = sudokuGrid.indexOfChild(cell) / 9
                val col = sudokuGrid.indexOfChild(cell) % 9
                cell.setBackgroundColor(getBlockColor(row, col))
                cell.setTextColor(colorText)
                validateConflicts()
                highlightSameNumbers()
            }
        }
    }

    private fun setupResetButton() {
        btnReset.setOnClickListener {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    val cell = cells[row][col]
                    cell?.text = ""
                    cell?.setBackgroundColor(getBlockColor(row, col))
                    cell?.setTextColor(colorText)
                }
            }
            selectedCell = null
        }
    }

    // 🔍 Verifica conflictos
    private fun validateConflicts() {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = cells[row][col]
                val value = cell?.text?.toString()
                if (value.isNullOrEmpty()) {
                    cell?.setBackgroundColor(getBlockColor(row, col))
                    cell?.setTextColor(colorText)
                } else {
                    if (hasConflict(row, col, value)) {
                        cell?.setBackgroundColor(colorConflict)
                    } else {
                        // si ya era círculo verde, lo mantenemos
                        if (cell.background is GradientDrawable) {
                            cell.background = makeCircle(Color.parseColor("#4ECCA3"))
                            cell.setTextColor(Color.BLACK)
                        } else {
                            cell.setBackgroundColor(getBlockColor(row, col))
                            cell.setTextColor(colorText)
                        }
                    }
                }
            }
        }
    }

    // Comprueba fila, columna y bloque 3x3
    private fun hasConflict(row: Int, col: Int, value: String): Boolean {
        for (c in 0 until 9) {
            if (c != col && cells[row][c]?.text?.toString() == value) return true
        }
        for (r in 0 until 9) {
            if (r != row && cells[r][col]?.text?.toString() == value) return true
        }
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if ((r != row || c != col) && cells[r][c]?.text?.toString() == value) return true
            }
        }
        return false
    }

    // 🔦 Resalta celdas con el mismo número que la seleccionada
    private fun highlightSameNumbers() {
        val selectedValue = selectedCell?.text?.toString()
        if (selectedValue.isNullOrEmpty()) return

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = cells[row][col]
                if (cell?.text?.toString() == selectedValue && cell != selectedCell) {
                    cell.setBackgroundColor(colorHighlight)
                    cell.setTextColor(colorText)
                }
            }
        }
        selectedCell?.setBackgroundColor(colorSelected)
    }

    // 🔄 Restaura colores antes de aplicar validaciones
    private fun restoreCellColors() {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = cells[row][col]
                val value = cell?.text?.toString()
                if (value.isNullOrEmpty()) {
                    cell?.setBackgroundColor(getBlockColor(row, col))
                } else {
                    if (hasConflict(row, col, value)) {
                        cell?.setBackgroundColor(colorConflict)
                    } else {
                        if (cell.background is GradientDrawable) {
                            cell.background = makeCircle(Color.parseColor("#4ECCA3"))
                            cell.setTextColor(Color.BLACK)
                        } else {
                            cell.setBackgroundColor(getBlockColor(row, col))
                        }
                    }
                }
                cell?.setTextColor(colorText)
            }
        }
    }

    // 🔵 Función para crear un círculo dinámicamente
    private fun makeCircle(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }
}
