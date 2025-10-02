package com.proyecto.proyect_games.ruleta

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
// import android.view.View // No se usa directamente View aquí
import android.widget.Button
import android.widget.ImageButton // Import para ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
// import androidx.core.content.ContextCompat // No se usa directamente aquí
import kotlin.random.Random
import com.proyecto.proyect_games.CatalogActivity
import com.proyecto.proyect_games.R

class Ruleta_Activity : AppCompatActivity() {

    // Vistas de UI
    private lateinit var imageViewBackground: ImageView
    private lateinit var textViewBank: TextView
    private lateinit var textViewResult: TextView
    private lateinit var linearLayoutNumberBoard: LinearLayout
    private lateinit var buttonBetRed: Button
    private lateinit var buttonBetBlack: Button
    private lateinit var buttonBetOdd: Button
    private lateinit var buttonBetEven: Button
    private lateinit var buttonBet1st12: Button
    private lateinit var buttonBet2nd12: Button
    private lateinit var buttonBet3rd12: Button
    private lateinit var buttonSpin: Button
    private lateinit var buttonBackToCatalog: ImageButton

    // Estado del juego
    private var bank: Int = 1000
    private var resultText: String = ""
    private var isSpinning: Boolean = false

    private val betsNumbers = mutableMapOf<Int, Int>()
    private var betRed: Int = 0
    private var betBlack: Int = 0
    private var bet1st12: Int = 0
    private var bet2nd12: Int = 0
    private var bet3rd12: Int = 0
    private var betOdd: Int = 0
    private var betEven: Int = 0

    private val numberColors = mutableMapOf<Int, Int>()
    private val numberViews = mutableMapOf<Int, TextView>()

    private val BET_AMOUNT = 100
    private val SPIN_DELAY_MS = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asegúrate de que tu layout se llame activity_ruleta.xml
        // Si lo llamaste diferente (ej. activity_ruleta_game), actualiza aquí
        setContentView(R.layout.activity_ruleta)


        // Inicializar vistas
        imageViewBackground = findViewById(R.id.imageViewBackground)
        textViewBank = findViewById(R.id.textViewBank)
        textViewResult = findViewById(R.id.textViewResult)
        linearLayoutNumberBoard = findViewById(R.id.linearLayoutNumberBoard)
        buttonBetRed = findViewById(R.id.buttonBetRed)
        buttonBetBlack = findViewById(R.id.buttonBetBlack)
        buttonBetOdd = findViewById(R.id.buttonBetOdd)
        buttonBetEven = findViewById(R.id.buttonBetEven)
        buttonBet1st12 = findViewById(R.id.buttonBet1st12)
        buttonBet2nd12 = findViewById(R.id.buttonBet2nd12)
        buttonBet3rd12 = findViewById(R.id.buttonBet3rd12)
        buttonSpin = findViewById(R.id.buttonSpin)
        buttonBackToCatalog = findViewById(R.id.buttonBackToCatalog)


        initializeNumberColors()
        createNumberBoard()
        setupListeners()
        updateUI()
        // Solo llama a backToCatalog si buttonBackToCatalog está presente y fue inicializado
        if (::buttonBackToCatalog.isInitialized) {
            backToCatalog()
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

    private fun initializeNumberColors() {
        (0..36).forEach { number ->
            numberColors[number] = when {
                number == 0 -> Color.GREEN
                isRed(number) -> Color.RED
                else -> Color.BLACK
            }
        }
    }

    private fun isRed(number: Int): Boolean {
        if (number == 0) return false
        val redNumbers = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
        return redNumbers.contains(number)
    }

    private fun createNumberBoard() {
        linearLayoutNumberBoard.removeAllViews()

        val textView0 = createNumberTextView(0)
        val params0 = LinearLayout.LayoutParams(
            dpToPx(50),
            dpToPx(150 + 8) // (50 * 3 filas) + (4dp de margen * 2 espacios entre 3 filas)
        ).apply {
            marginEnd = dpToPx(4)
        }
        textView0.layoutParams = params0
        linearLayoutNumberBoard.addView(textView0)
        numberViews[0] = textView0

        val numbersLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            // Los LayoutParams para numbersLayout (cómo se posiciona dentro de linearLayoutNumberBoard)
            // se pueden definir aquí o dejar que el padre los asigne por defecto si es WRAP_CONTENT.
            // Si linearLayoutNumberBoard es horizontal, este numbersLayout tomará el espacio restante.
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val rows = listOf(
            (1..36).filter { it % 3 == 0 }.reversed(),
            (1..36).filter { it % 3 == 2 }.reversed(),
            (1..36).filter { it % 3 == 1 }.reversed()
        )

        rows.forEachIndexed { rowIndex, rowNumbers -> // Usar forEachIndexed para obtener el índice
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            // Definir los LayoutParams para rowLayout (cómo se posiciona DENTRO de numbersLayout)
            val paramsForRow = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Aplicar bottomMargin a todas las filas excepto la última
            if (rowIndex < rows.size - 1) {
                paramsForRow.bottomMargin = dpToPx(4)
            }
            rowLayout.layoutParams = paramsForRow // Asignar los params a rowLayout

            rowNumbers.forEachIndexed { numberIndex, number -> // Usar forEachIndexed
                val textView = createNumberTextView(number)
                val paramsForTextView = LinearLayout.LayoutParams(dpToPx(50), dpToPx(50))
                // Aplicar marginEnd a todos los números en la fila excepto el último
                if (numberIndex < rowNumbers.size - 1) {
                    paramsForTextView.marginEnd = dpToPx(4)
                }
                textView.layoutParams = paramsForTextView
                rowLayout.addView(textView)
                numberViews[number] = textView
            }
            numbersLayout.addView(rowLayout) // Añadir la fila completa a numbersLayout
        }
        linearLayoutNumberBoard.addView(numbersLayout) // Añadir el contenedor de filas 1-36
    }


    private fun createNumberTextView(number: Int): TextView {
        return TextView(this).apply {
            text = "$number\n($${betsNumbers[number] ?: 0})" // Mostrar apuesta actual
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(numberColors[number] ?: Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            isClickable = true
            isFocusable = true

            setOnClickListener {
                if (!isSpinning && bank >= BET_AMOUNT) {
                    val currentBet = betsNumbers.getOrElse(number) { 0 }
                    betsNumbers[number] = currentBet + BET_AMOUNT
                    bank -= BET_AMOUNT
                    updateUI()
                }
            }
        }
    }


    private fun setupListeners() {
        buttonBetRed.setOnClickListener { placeSpecialBet { betRed += BET_AMOUNT } }
        buttonBetBlack.setOnClickListener { placeSpecialBet { betBlack += BET_AMOUNT } }
        buttonBetOdd.setOnClickListener { placeSpecialBet { betOdd += BET_AMOUNT } }
        buttonBetEven.setOnClickListener { placeSpecialBet { betEven += BET_AMOUNT } }
        buttonBet1st12.setOnClickListener { placeSpecialBet { bet1st12 += BET_AMOUNT } }
        buttonBet2nd12.setOnClickListener { placeSpecialBet { bet2nd12 += BET_AMOUNT } }
        buttonBet3rd12.setOnClickListener { placeSpecialBet { bet3rd12 += BET_AMOUNT } }

        buttonSpin.setOnClickListener {
            if (!isSpinning && hasBets()) {
                spinRoulette()
            }
        }
    }

    private fun placeSpecialBet(updateBetAction: () -> Unit) {
        if (!isSpinning && bank >= BET_AMOUNT) {
            updateBetAction()
            bank -= BET_AMOUNT
            updateUI()
        }
    }

    private fun hasBets(): Boolean {
        return betsNumbers.any { it.value > 0 } || betRed > 0 || betBlack > 0 ||
                bet1st12 > 0 || bet2nd12 > 0 || bet3rd12 > 0 ||
                betOdd > 0 || betEven > 0
    }

    private fun spinRoulette() {
        isSpinning = true
        resultText = "Girando..."
        updateUI()

        Handler(Looper.getMainLooper()).postDelayed({
            val winningNumber = Random.nextInt(0, 37)
            val amountWon = calculateWinnings(
                winningNumber, betsNumbers,
                betRed, betBlack,
                bet1st12, bet2nd12, bet3rd12,
                betOdd, betEven
            )
            // El 'amountWon' de calculateWinnings es el retorno total (apuesta + ganancia)
            // Como las apuestas ya se restaron, el cambio neto en el banco es amountWon - totalApostadoEstaRonda.
            // O, más simple: El banco se incrementa por la cantidad total devuelta.
            bank += amountWon

            val winningColorName = when {
                winningNumber == 0 -> "Verde"
                isRed(winningNumber) -> "Rojo"
                else -> "Negro"
            }
            // Para mostrar la ganancia neta en el mensaje:
            // Primero calculamos el total apostado en esta ronda
            var totalBetThisRound = betsNumbers.values.sum() + betRed + betBlack + bet1st12 + bet2nd12 + bet3rd12 + betOdd + betEven
            val netGainOrLoss = amountWon - totalBetThisRound // Puede ser negativo si se pierde más de lo que se devuelve

            resultText = "Cayó en $winningNumber ($winningColorName).\n" +
                    if (netGainOrLoss > 0) "¡Ganaste $$netGainOrLoss!"
                    else if (netGainOrLoss < 0) "Perdiste $${-netGainOrLoss}."
                    else "¡Recuperaste tu apuesta!"


            clearBets()
            isSpinning = false
            updateUI()
        }, SPIN_DELAY_MS)
    }

    // Devuelve el MONTO TOTAL a RECIBIR por las apuestas ganadoras (incluye la apuesta original)
    private fun calculateWinnings(
        winningNumber: Int, currentBetsNumbers: Map<Int, Int>,
        currentBetRed: Int, currentBetBlack: Int,
        currentBet1st12: Int, currentBet2nd12: Int, currentBet3rd12: Int,
        currentBetOdd: Int, currentBetEven: Int
    ): Int {
        var amountToReturn = 0

        currentBetsNumbers[winningNumber]?.let { amountBetOnNumber ->
            amountToReturn += amountBetOnNumber * 36
        }

        if (winningNumber != 0) {
            if (isRed(winningNumber) && currentBetRed > 0) {
                amountToReturn += currentBetRed * 2
            }
            if (!isRed(winningNumber) && currentBetBlack > 0) { // Es Negro (y no 0)
                amountToReturn += currentBetBlack * 2
            }
            if (winningNumber % 2 != 0 && currentBetOdd > 0) { // Impar
                amountToReturn += currentBetOdd * 2
            }
            if (winningNumber % 2 == 0 && currentBetEven > 0) { // Par (y no 0)
                amountToReturn += currentBetEven * 2
            }
        }

        when (winningNumber) {
            in 1..12 -> if (currentBet1st12 > 0) amountToReturn += currentBet1st12 * 3
            in 13..24 -> if (currentBet2nd12 > 0) amountToReturn += currentBet2nd12 * 3
            in 25..36 -> if (currentBet3rd12 > 0) amountToReturn += currentBet3rd12 * 3
        }
        return amountToReturn
    }

    private fun clearBets() {
        betsNumbers.clear()
        betRed = 0
        betBlack = 0
        bet1st12 = 0
        bet2nd12 = 0
        bet3rd12 = 0
        betOdd = 0
        betEven = 0
    }

    private fun updateUI() {
        textViewBank.text = "Banco: $$bank"
        textViewResult.text = resultText

        numberViews.forEach { (number, textView) ->
            val betOnNumber = betsNumbers[number] ?: 0
            textView.text = "$number\n($$betOnNumber)"
            textView.isEnabled = !isSpinning && bank >= BET_AMOUNT
            // textView.isClickable = !isSpinning // isEnabled también maneja la clickeabilidad
        }

        buttonBetRed.text = "Rojo ($$betRed)"
        buttonBetBlack.text = "Negro ($$betBlack)"
        buttonBetOdd.text = "Impar ($$betOdd)"
        buttonBetEven.text = "Par ($$betEven)"
        buttonBet1st12.text = "1ra Docena ($$bet1st12)"
        buttonBet2nd12.text = "2da Docena ($$bet2nd12)"
        buttonBet3rd12.text = "3ra Docena ($$bet3rd12)"

        val canPlaceSpecialBet = !isSpinning && bank >= BET_AMOUNT
        buttonBetRed.isEnabled = canPlaceSpecialBet
        buttonBetBlack.isEnabled = canPlaceSpecialBet
        buttonBetOdd.isEnabled = canPlaceSpecialBet
        buttonBetEven.isEnabled = canPlaceSpecialBet
        buttonBet1st12.isEnabled = canPlaceSpecialBet
        buttonBet2nd12.isEnabled = canPlaceSpecialBet
        buttonBet3rd12.isEnabled = canPlaceSpecialBet

        buttonSpin.isEnabled = !isSpinning && hasBets()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
