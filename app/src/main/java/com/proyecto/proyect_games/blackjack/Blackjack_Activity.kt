package com.proyecto.proyect_games.blackjack

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import kotlin.random.Random
import com.proyecto.proyect_games.R
import com.proyecto.proyect_games.CatalogActivity
import android.widget.ImageView


class Blackjack_Activity : AppCompatActivity() {

    // Money / bet UI
    private lateinit var tvBalance: TextView
    private lateinit var tvBet: TextView
    private lateinit var tvInPlay: TextView
    private lateinit var playerInfoBar: LinearLayout   // contenedor de apuesta
    private lateinit var btnBetPlus: TextView
    private lateinit var btnBetMinus: TextView

    // Buttons
    private lateinit var btnDeal: TextView
    private lateinit var btnHit: TextView
    private lateinit var btnStand: TextView
    private lateinit var btnDouble: TextView
    private lateinit var btnSplit: TextView
    private lateinit var btnInsurance: TextView
    private lateinit var btnClear: TextView

    // Hands
    private lateinit var dealerHandView: LinearLayout
    private lateinit var playerHandView: LinearLayout
    private lateinit var tvDealerCount: TextView
    private lateinit var tvPlayerCount: TextView

    // Game state
    private var balance = 20_000
    private var betStep = 1_000
    private var baseBet = 1_000
    private var inPlay = 0

    private val deck = mutableListOf<String>()
    private val player = mutableListOf<String>()
    private val dealer = mutableListOf<String>()
    private var roundActive = false
    private var dealerHoleCard: String? = null
    private var holeCardView: TextView? = null
    private var doubledThisRound = false

    // Boton de regreso
    private lateinit var ivBackToCatalog: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blackjack)

        // find views
        tvBalance = findViewById(R.id.tvBalance)
        tvBet = findViewById(R.id.tvBet)
        tvInPlay = findViewById(R.id.tvInPlay)
        playerInfoBar = findViewById(R.id.playerInfoBar)

        btnBetPlus = findViewById(R.id.btnBetPlus)
        btnBetMinus = findViewById(R.id.btnBetMinus)

        btnDeal = findViewById(R.id.btnDeal)
        btnHit = findViewById(R.id.btnHit)
        btnStand = findViewById(R.id.btnStand)
        btnDouble = findViewById(R.id.btnDouble)
        btnSplit = findViewById(R.id.btnSplit)
        btnInsurance = findViewById(R.id.btnInsurance)
        btnClear = findViewById(R.id.btnClear)

        dealerHandView = findViewById(R.id.dealerHand)
        playerHandView = findViewById(R.id.playerHand)
        tvDealerCount = findViewById(R.id.tvDealerCount)
        tvPlayerCount = findViewById(R.id.tvPlayerCount)

        // Inicializar el botón de regreso
        ivBackToCatalog = findViewById(R.id.ivBackToCatalog)

        // Configurar el listener para volver al catálogo
        ivBackToCatalog.setOnClickListener {
            navigateToCatalog()
        }

        updateMoneyUI()
        setListeners()
        setActionButtonsEnabled(false)

        // Al abrir el juego: mostrar controles para elegir apuesta (ronda aún no activa)
        setBetControlsVisible(true)
        updateCounts()
    }

    private fun setListeners() {
        btnBetPlus.setOnClickListener {
            if (roundActive) return@setOnClickListener
            if (balance - baseBet >= betStep) {
                baseBet += betStep
                updateBetUI()
            } else toast("No alcanza para subir apuesta")
        }
        btnBetMinus.setOnClickListener {
            if (roundActive) return@setOnClickListener
            if (baseBet > betStep) {
                baseBet -= betStep
                updateBetUI()
            } else toast("Apuesta mínima: $betStep")
        }
        btnClear.setOnClickListener {
            if (roundActive) { toast("Termina la ronda primero"); return@setOnClickListener }
            baseBet = betStep
            updateBetUI()
        }

        btnDeal.setOnClickListener { if (!roundActive) startRound() }
        btnHit.setOnClickListener { if (roundActive) playerHit() }
        btnStand.setOnClickListener { if (roundActive) finishRound(stand = true) }
        btnDouble.setOnClickListener { if (roundActive) doubleDown() }

        btnSplit.setOnClickListener { toast("Split no disponible en este prototipo") }
        btnInsurance.setOnClickListener { toast("Insurance no disponible en este prototipo") }
    }

    private fun startRound() {
        if (baseBet > balance) { toast("Saldo insuficiente"); return }
        roundActive = true
        doubledThisRound = false

        // Oculta controles de apuesta durante la ronda
        setBetControlsVisible(false)

        inPlay = baseBet
        balance -= baseBet
        updateMoneyUI()

        buildDeck()
        player.clear(); dealer.clear()
        dealerHandView.removeAllViews(); playerHandView.removeAllViews()
        holeCardView = null; dealerHoleCard = null

        dealToPlayer(); updateCounts()
        dealDealerUp();  updateCounts()
        dealToPlayer();  updateCounts()
        dealDealerHole(); updateCounts()

        if (isBlackjack(player)) {
            revealDealerHole(); updateCounts()
            if (isBlackjack(dealer)) push() else {
                val win = (inPlay * 3) / 2
                balance += inPlay + win
                inPlay = 0
                endRoundUI()
                toast("Blackjack! Pagado 3:2 (+$win)")
            }
        } else {
            setActionButtonsEnabled(true)
        }
    }

    private fun playerHit() {
        dealToPlayer()
        updateCounts()
        val value = handValue(player)
        if (value > 21) {
            revealDealerHole(); updateCounts()
            inPlay = 0
            endRoundUI()
            toast("Te pasaste ($value). Pierdes.")
        }
    }

    private fun finishRound(stand: Boolean) {
        revealDealerHole(); updateCounts()
        while (handValue(dealer) < 17) {
            dealToDealer(); updateCounts()
        }
        val p = handValue(player)
        val d = handValue(dealer)
        val result = when {
            d > 21 || p > d -> "win"
            p == d -> "push"
            else -> "lose"
        }
        when (result) {
            "win" -> { balance += inPlay * 2; toast("Ganaste (+$inPlay)") }
            "push" -> { balance += inPlay; toast("Empate (push)") }
            "lose" -> { toast("Perdiste (-$inPlay)") }
        }
        inPlay = 0
        endRoundUI()
    }

    private fun doubleDown() {
        if (player.size != 2) { toast("Solo puedes doblar con 2 cartas"); return }
        if (balance < inPlay) { toast("No alcanza para doblar"); return }
        balance -= inPlay
        inPlay *= 2
        updateMoneyUI()
        doubledThisRound = true
        dealToPlayer(); updateCounts()
        finishRound(stand = true)
    }

    private fun push() {
        balance += inPlay
        inPlay = 0
        endRoundUI()
        toast("Push (empate)")
    }

    /** Mostrar/ocultar la barra para elegir apuesta */
    private fun setBetControlsVisible(visible: Boolean) {
        playerInfoBar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /** Finaliza ronda: desactiva acciones, muestra controles de apuesta y actualiza dinero */
    private fun endRoundUI() {
        updateMoneyUI()
        setActionButtonsEnabled(false)
        roundActive = false
        setBetControlsVisible(true)   // <- vuelven a aparecer para elegir la siguiente apuesta
    }

    // ---------- Deck / dealing ----------
    private fun buildDeck() {
        deck.clear()
        val ranks = listOf("A","2","3","4","5","6","7","8","9","10","J","Q","K")
        val suits = listOf("♠","♥","♦","♣")
        for (s in suits) for (r in ranks) deck.add("$r$s")
        deck.shuffle(Random(System.currentTimeMillis()))
    }

    private fun drawCard(): String = deck.removeAt(0)

    private fun dealToPlayer() {
        val c = drawCard()
        player.add(c)
        playerHandView.addView(cardView(c))
        setActionButtonsEnabled(true)
    }

    private fun dealDealerUp() {
        val c = drawCard()
        dealer.add(c)
        dealerHandView.addView(cardView(c))
    }

    private fun dealDealerHole() {
        val c = drawCard()
        dealer.add(c)
        dealerHoleCard = c
        val v = cardBackView()
        holeCardView = v
        dealerHandView.addView(v)
    }

    private fun dealToDealer() {
        val c = drawCard()
        dealer.add(c)
        dealerHandView.addView(cardView(c))
    }

    private fun revealDealerHole() {
        dealerHoleCard?.let { hole ->
            val index = dealerHandView.indexOfChild(holeCardView)
            if (index >= 0) {
                dealerHandView.removeViewAt(index)
                dealerHandView.addView(cardView(hole), index)
            }
            dealerHoleCard = null
        }
    }

    // ---------- Valores / contadores ----------
    private fun cardValue(card: String): Int {
        val r = card.dropLast(1)
        return when (r) {
            "A" -> 11
            "K","Q","J","10" -> 10
            else -> r.toInt()
        }
    }

    private fun handValue(hand: List<String>): Int {
        var total = hand.sumOf { cardValue(it) }
        var aces = hand.count { it.startsWith("A") }
        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }
        return total
    }

    private fun isBlackjack(hand: List<String>) = hand.size == 2 && handValue(hand) == 21

    private fun updateCounts() {
        tvPlayerCount.text = handValue(player).toString()
        // si la hole card sigue oculta, mostramos el valor de la visible (sin contar la oculta)
        val dealerShown = if (dealerHoleCard != null && dealer.size >= 2) {
            handValue(listOf(dealer.first()))
        } else handValue(dealer)
        tvDealerCount.text = dealerShown.toString()
    }

    // ---------- UI helpers ----------
    private fun cardView(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 18f
            typeface = Typeface.MONOSPACE
            setPadding(16)
            gravity = Gravity.CENTER
            setTextColor(0xFF000000.toInt())
            background = roundedCardBg()
            val lp = LinearLayout.LayoutParams(110, 150)
            lp.setMargins(12, 6, 12, 6)
            layoutParams = lp
        }

    private fun cardBackView(): TextView =
        TextView(this).apply {
            text = "🂠"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(16)
            background = roundedBackBg()
            val lp = LinearLayout.LayoutParams(110, 150)
            lp.setMargins(12, 6, 12, 6)
            layoutParams = lp
        }

    private fun roundedCardBg() =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(0xFFFFFFFF.toInt())
            cornerRadius = 12f
            setStroke(3, 0xFFCCCCCC.toInt())
        }

    private fun roundedBackBg() =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(0xFF1A1A1A.toInt())
            cornerRadius = 12f
            setStroke(3, 0xFF3B3B3B.toInt())
        }

    private fun updateMoneyUI() {
        tvBalance.text = formatMoney(balance)
        tvInPlay.text = formatMoney(inPlay)
        updateBetUI()
    }

    private fun updateBetUI() {
        tvBet.text = formatMoney(baseBet)
    }

    private fun setActionButtonsEnabled(enabled: Boolean) {
        btnHit.isEnabled = enabled
        btnStand.isEnabled = enabled
        btnDouble.isEnabled = enabled && player.size == 2 && balance >= inPlay
        val alpha = if (enabled) 1f else 0.45f
        listOf(btnHit, btnStand, btnDouble).forEach { it.alpha = alpha }
    }

    private fun navigateToCatalog() {
        val intent = Intent(this, CatalogActivity::class.java)
        // Limpiar la pila de actividades hasta CatalogActivity si ya está en la pila,
        // o lanzar una nueva instancia si no lo está.
        // Y asegura que si el usuario presiona "Atrás" desde el catálogo, no vuelva al juego.
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish() // Cierra la Activity del juego actual
    }

    private fun formatMoney(v: Int): String = "%,d".format(v)
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
