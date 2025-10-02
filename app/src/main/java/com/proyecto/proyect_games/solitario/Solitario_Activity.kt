package com.proyecto.proyect_games.solitario

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.proyecto.proyect_games.solitario.modelo.Carta
import com.proyecto.proyect_games.solitario.modelo.Mazo
import com.proyecto.proyect_games.solitario.modelo.Valor
import java.util.Stack
import com.proyecto.proyect_games.CatalogActivity
import com.proyecto.proyect_games.R

class Solitario_Activity : AppCompatActivity() {

    private lateinit var mazo: Mazo
    private val descarte: MutableList<Carta> = mutableListOf()
    private val pilasFundacion: Array<Stack<Carta>> = Array(4) { Stack() }
    private val pilasTableau: Array<MutableList<Carta>> = Array(7) { mutableListOf() }

    // Vistas
    private lateinit var imagenMazoRobo: ImageView
    private lateinit var imagenDescarte: ImageView
    private lateinit var fundacionViews: List<ImageView>
    private lateinit var tableauContainers: List<FrameLayout>
    private lateinit var buttonBackToCatalog: ImageButton

    // Para la lógica de selección/arrastre (Click-to-select, Click-to-drop)
    private var pilaSeleccionada: MutableList<Carta> = mutableListOf()
    private var vistaCartaInferiorPilaSeleccionada: View? = null // Vista de la carta más baja de la pila seleccionada
    private var origenPilaSeleccion: Zona? = null
    private var indiceColumnaOrigenPila: Int? = null // Para TABLEAU y FUNDACION (índice de columna/fundación)
    // private var indiceCartaInferiorEnPilaOrigen: Int? = null

    private val CARTA_DRAG_TAG = "CARTA_SOLITARIO_DRAG"

    enum class Zona { MAZO_ROBO, DESCARTE, TABLEAU, FUNDACION }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_solitario)

        buttonBackToCatalog = findViewById(R.id.buttonBackToCatalog)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()
        iniciarJuego()
        backToCatalog()

        val botonReiniciar: Button = findViewById(R.id.botonReiniciar)
        botonReiniciar.setOnClickListener {
            reiniciarJuegoCompleto()
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

    private fun inicializarVistas() {
        imagenMazoRobo = findViewById(R.id.imagenMazoRobo)
        imagenDescarte = findViewById(R.id.imagenDescarte)

        fundacionViews = listOf(
            findViewById(R.id.fundacion1),
            findViewById(R.id.fundacion2),
            findViewById(R.id.fundacion3),
            findViewById(R.id.fundacion4)
        )

        tableauContainers = listOf(
            findViewById(R.id.tableauColumna1),
            findViewById(R.id.tableauColumna2),
            findViewById(R.id.tableauColumna3),
            findViewById(R.id.tableauColumna4),
            findViewById(R.id.tableauColumna5),
            findViewById(R.id.tableauColumna6),
            findViewById(R.id.tableauColumna7)
        )

        imagenMazoRobo.setOnClickListener { robarDelMazo() }
        imagenDescarte.setOnClickListener { seleccionarDesdeDescarte() }

        fundacionViews.forEachIndexed { index, imageView ->
            imageView.setOnDragListener(createDropListener(Zona.FUNDACION, index))
            imageView.setOnClickListener {
                if (pilaSeleccionada.isNotEmpty()) {
                    intentarMoverPilaSeleccionadaA(Zona.FUNDACION, index)
                } else if (pilasFundacion[index].isNotEmpty()) {
                    seleccionarPila(mutableListOf(pilasFundacion[index].peek()), imageView, Zona.FUNDACION, index)
                }
            }
        }

        tableauContainers.forEachIndexed { index, frameLayout ->
            frameLayout.setOnDragListener(createDropListener(Zona.TABLEAU, index))
            frameLayout.setOnClickListener {
                if (pilaSeleccionada.isNotEmpty()) {
                    intentarMoverPilaSeleccionadaA(Zona.TABLEAU, index)
                }
            }
        }
    }

    private fun reiniciarJuegoCompleto() {
        Log.d("Solitario", "Reiniciando juego...")
        mazo.reiniciarMazo()
        Log.d("Solitario", "Mazo reiniciado, cartas: ${mazo.obtenerNumeroCartas()}")
        descarte.clear()
        pilasFundacion.forEach { it.clear() }
        pilasTableau.forEach { it.clear() }

        cancelarSeleccion()

        // Limpiar vistas explícitamente
        imagenDescarte.setImageResource(android.R.color.transparent)
        imagenDescarte.setBackgroundResource(R.drawable.placeholder_tableau) // Asumiendo que tienes este drawable

        fundacionViews.forEach {
            it.setImageResource(android.R.color.transparent)
            it.setBackgroundResource(R.drawable.placeholder_fundacion) // Asumiendo que tienes este drawable
        }

        tableauContainers.forEach { container ->
            // Eliminar todas las ImageViews de cartas, no el fondo del FrameLayout
            val vistasAEliminar = container.children.filterIsInstance<ImageView>().toList()
            vistasAEliminar.forEach {
                Log.d("Solitario", "Reiniciar: Eliminando ImageView de container ${container.id}")
                container.removeView(it)
            }
        }
        Log.d("Solitario", "Vistas de cartas eliminadas de los contenedores del tableau.")

        iniciarJuego() // Reparte y llama a actualizarTodasLasVistas()
        Log.d("Solitario", "Juego reiniciado. Tableau[0] size: ${pilasTableau[0].size}")
    }


    private fun iniciarJuego() {
        mazo = Mazo() // Mazo se baraja en su init
        descarte.clear()
        pilasFundacion.forEach { it.clear() }
        pilasTableau.forEach { it.clear() }

        Log.d("Solitario", "Iniciando reparto al tableau...")
        for (i in 0..6) {
            for (j in 0..i) {
                val carta = mazo.robarCarta()
                if (carta != null) {
                    carta.esVisible = (j == i)
                    pilasTableau[i].add(carta)
                } else {
                    Log.e("Solitario", "Mazo vacío durante el reparto inicial en columna $i, carta $j.")
                }
            }
            Log.d("Solitario", "Columna Tableau $i repartida con ${pilasTableau[i].size} cartas.")
        }
        actualizarTodasLasVistas()
        Log.d("Solitario", "actualizarTodasLasVistas() llamada después de iniciarJuego.")
    }


    private fun actualizarTodasLasVistas() {
        Log.d("Solitario", "Actualizando todas las vistas...")
        actualizarVistaMazo()
        actualizarVistaDescarte()
        actualizarVistasFundacion()
        actualizarVistasTableau()
        Log.d("Solitario", "Todas las vistas actualizadas.")
    }

    private fun actualizarVistaMazo() {
        when {
            mazo.estaVacio() && descarte.isEmpty() -> {
                imagenMazoRobo.setImageResource(android.R.color.transparent)
                imagenMazoRobo.setBackgroundResource(R.drawable.placeholder_tableau)
                imagenMazoRobo.isClickable = false
            }
            mazo.estaVacio() && descarte.isNotEmpty() -> {
                imagenMazoRobo.setImageResource(R.drawable.card_refresh)
                imagenMazoRobo.setBackgroundResource(android.R.color.transparent)
                imagenMazoRobo.isClickable = true
            }
            else -> {
                imagenMazoRobo.setImageResource(R.drawable.card_back)
                imagenMazoRobo.setBackgroundResource(android.R.color.transparent)
                imagenMazoRobo.isClickable = true
            }
        }
    }

    private fun actualizarVistaDescarte() {
        if (descarte.isNotEmpty()) {
            val cartaSuperior = descarte.last()
            cartaSuperior.esVisible = true
            val resId = resources.getIdentifier(cartaSuperior.obtenerNombreImagen(), "drawable", packageName)
            if (resId != 0) imagenDescarte.setImageResource(resId) else imagenDescarte.setImageResource(R.drawable.card_back)
            imagenDescarte.setBackgroundResource(android.R.color.transparent)
            imagenDescarte.isClickable = true
        } else {
            imagenDescarte.setImageResource(android.R.color.transparent)
            imagenDescarte.setBackgroundResource(R.drawable.placeholder_tableau)
            imagenDescarte.isClickable = false
        }
    }

    private fun actualizarVistasFundacion() {
        pilasFundacion.forEachIndexed { index, pila ->
            val imageView = fundacionViews[index]
            if (pila.isNotEmpty()) {
                val cartaSuperior = pila.peek()
                val resId = resources.getIdentifier(cartaSuperior.obtenerNombreImagen(), "drawable", packageName)
                if (resId != 0) imageView.setImageResource(resId) else imageView.setImageResource(R.drawable.placeholder_fundacion)
                imageView.setBackgroundResource(android.R.color.transparent)
            } else {
                imageView.setImageResource(android.R.color.transparent)
                imageView.setBackgroundResource(R.drawable.placeholder_fundacion)
            }
        }
    }

    private fun actualizarVistasTableau() {
        Log.d("Solitario", "Actualizando vistas del Tableau...")
        tableauContainers.forEachIndexed { colIndex, container ->
            // Eliminar ImageViews de cartas anteriores
            val vistasCartasAnteriores = container.children.filterIsInstance<ImageView>().toList()
            vistasCartasAnteriores.forEach { container.removeView(it) }
            Log.d("Solitario", "Tableau Col $colIndex: Vistas anteriores eliminadas (${vistasCartasAnteriores.size}). Cartas en modelo: ${pilasTableau[colIndex].size}")


            if (pilasTableau[colIndex].isEmpty()) {
                // El FrameLayout ya tiene su fondo de placeholder_tableau y su OnClickListener/OnDragListener.
            } else {
                pilasTableau[colIndex].forEachIndexed { cartaIndexEnPila, carta ->
                    val imageView = ImageView(this).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.card_width),
                            resources.getDimensionPixelSize(R.dimen.card_height)
                        ).apply {
                            topMargin = cartaIndexEnPila * resources.getDimensionPixelSize(R.dimen.card_overlap_tableau)
                        }
                        scaleType = ImageView.ScaleType.FIT_CENTER

                        if (carta.esVisible) {
                            val resId = resources.getIdentifier(carta.obtenerNombreImagen(), "drawable", packageName)
                            setImageResource(if (resId != 0) resId else R.drawable.card_back)
                            Log.d("Solitario", "Tableau Col $colIndex, Carta ${carta.obtenerNombreImagen()}: Visible")
                        } else {
                            setImageResource(R.drawable.card_back)
                            Log.d("Solitario", "Tableau Col $colIndex, Carta Index $cartaIndexEnPila: Oculta")
                        }

                        if (carta.esVisible) {
                            setOnClickListener {
                                if (pilaSeleccionada.isNotEmpty()) {
                                    intentarMoverPilaSeleccionadaA(Zona.TABLEAU, colIndex)
                                } else {
                                    seleccionarPila(null, this, Zona.TABLEAU, colIndex, cartaIndexEnPila, carta)
                                }
                            }
                            setOnLongClickListener {
                                seleccionarPila(null, this, Zona.TABLEAU, colIndex, cartaIndexEnPila, carta)
                                if (pilaSeleccionada.isNotEmpty()) {
                                    val dragClipData = ClipData(
                                        CARTA_DRAG_TAG,
                                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                                        ClipData.Item(CARTA_DRAG_TAG) // Podrías añadir info de la carta/pila aquí
                                    )
                                    val shadowBuilder = View.DragShadowBuilder(this) // Sombra de la carta clickeada
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        this.startDragAndDrop(dragClipData, shadowBuilder, this, 0)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        this.startDrag(dragClipData, shadowBuilder, this, 0)
                                    }
                                    // No cancelar selección aquí, se maneja en el listener de drag
                                }
                                true
                            }
                        } else if (cartaIndexEnPila == pilasTableau[colIndex].size - 1 && !carta.esVisible) { // Última y boca abajo
                            setOnClickListener {
                                voltearCartaTableau(colIndex)
                            }
                        }
                    }
                    container.addView(imageView)
                }
            }
            Log.d("Solitario", "Tableau Col $colIndex: ${container.childCount} ImageViews añadidas.")
        }
    }

    private fun robarDelMazo() {
        cancelarSeleccion()
        if (mazo.estaVacio()) {
            if (descarte.isNotEmpty()) {
                mazo.rellenarDesdeDescarte(descarte)
                actualizarVistaMazo()
                actualizarVistaDescarte()
            }
        } else {
            mazo.robarCarta()?.let { cartaRobada ->
                cartaRobada.esVisible = true
                descarte.add(cartaRobada)
                actualizarVistaMazo()
                actualizarVistaDescarte()
            }
        }
    }

    private fun seleccionarDesdeDescarte() {
        if (descarte.isNotEmpty()) {
            val carta = descarte.last()
            // Si la carta del descarte ya está seleccionada (es la única en pilaSeleccionada y origen es DESCARTE)
            if (pilaSeleccionada.size == 1 && pilaSeleccionada.first() == carta && origenPilaSeleccion == Zona.DESCARTE) {
                cancelarSeleccion()
            } else {
                seleccionarPila(mutableListOf(carta), imagenDescarte, Zona.DESCARTE)
            }
        }
    }

    private fun seleccionarPila(
        cartas: MutableList<Carta>? = null, // Para selección directa (descarte, fundación)
        viewPulsada: View,
        zona: Zona,
        indiceColumna: Int? = null, // Tableau col index o Fundacion index
        indiceCartaPulsadaEnColumna: Int? = null, // Solo para Tableau, índice de la carta clickeada
        cartaPulsadaEnTableau: Carta? = null // Solo para Tableau
    ) {
        cancelarSeleccion()

        when (zona) {
            Zona.TABLEAU -> {
                if (indiceColumna != null && indiceCartaPulsadaEnColumna != null && cartaPulsadaEnTableau != null) {
                    val pilaOriginal = pilasTableau[indiceColumna]
                    if (cartaPulsadaEnTableau.esVisible && pilaOriginal.contains(cartaPulsadaEnTableau)) {
                        val indiceInicioSubpila = pilaOriginal.indexOf(cartaPulsadaEnTableau)
                        if (indiceInicioSubpila != -1 && indiceInicioSubpila >= indiceCartaPulsadaEnColumna) { // Sanity check
                            for (i in indiceInicioSubpila until pilaOriginal.size) {
                                pilaSeleccionada.add(pilaOriginal[i])
                            }
                            if (pilaSeleccionada.isNotEmpty()) {
                                origenPilaSeleccion = Zona.TABLEAU
                                indiceColumnaOrigenPila = indiceColumna
                                // Encontrar la ImageView correspondiente a la cartaPulsadaEnTableau
                                val container = tableauContainers[indiceColumna]
                                // Este mapeo de carta a vista es simplificado.
                                // Idealmente, las ImageViews tendrían tags o una forma más directa de encontrarlas.
                                // Por ahora, usamos la vista pulsada.
                                vistaCartaInferiorPilaSeleccionada = viewPulsada
                                vistaCartaInferiorPilaSeleccionada?.alpha = 0.7f
                                Log.d("Solitario", "Pila seleccionada de Tableau ($indiceColumna): ${pilaSeleccionada.map { it.obtenerNombreImagen() }}")
                            } else {
                                Log.w("Solitario", "Selección Tableau: Pila seleccionada terminó vacía.")
                            }
                        } else {
                            Log.w("Solitario", "Selección Tableau: Carta no encontrada o índice incorrecto.")
                        }
                    } else {
                        Log.w("Solitario", "Selección Tableau: Carta no visible o no en pila.")
                    }
                }
            }
            Zona.DESCARTE, Zona.FUNDACION -> { // Para descarte y fundación, 'cartas' ya es una lista de 1 carta
                if (cartas != null && cartas.isNotEmpty()) {
                    pilaSeleccionada.addAll(cartas)
                    origenPilaSeleccion = zona
                    if (zona == Zona.FUNDACION) indiceColumnaOrigenPila = indiceColumna // Índice de la fundación
                    vistaCartaInferiorPilaSeleccionada = viewPulsada
                    vistaCartaInferiorPilaSeleccionada?.alpha = 0.7f
                    Log.d("Solitario", "Carta seleccionada de $zona: ${pilaSeleccionada.first().obtenerNombreImagen()}")
                }
            }
            else -> {}
        }
        if (pilaSeleccionada.isEmpty()){
            Log.d("Solitario", "Selección fallida o cancelada, pilaSeleccionada está vacía.")
        }
    }


    private fun cancelarSeleccion() {
        vistaCartaInferiorPilaSeleccionada?.alpha = 1.0f
        pilaSeleccionada.clear()
        vistaCartaInferiorPilaSeleccionada = null
        origenPilaSeleccion = null
        indiceColumnaOrigenPila = null
        Log.d("Solitario", "Selección cancelada.")
    }

    private fun intentarMoverPilaSeleccionadaA(zonaDestino: Zona, indiceDestino: Int) {
        if (pilaSeleccionada.isEmpty() || origenPilaSeleccion == null) {
            cancelarSeleccion()
            return
        }

        val cartaInferiorDePila = pilaSeleccionada.first()
        var movimientoExitoso = false

        when (zonaDestino) {
            Zona.TABLEAU -> {
                if (origenPilaSeleccion == Zona.TABLEAU && indiceColumnaOrigenPila == indiceDestino) {
                    // No mover a la misma pila, podría ser un click accidental
                    // Toast.makeText(this, "Mismo origen", Toast.LENGTH_SHORT).show()
                    // cancelarSeleccion() // Se cancela al final de todas formas
                    return
                }
                if (puedeMoverPilaATableau(cartaInferiorDePila, indiceDestino)) {
                    pilasTableau[indiceDestino].addAll(pilaSeleccionada)
                    movimientoExitoso = true
                }
            }
            Zona.FUNDACION -> {
                if (pilaSeleccionada.size == 1 && puedeMoverCartaAFundacion(cartaInferiorDePila, indiceDestino)) {
                    pilasFundacion[indiceDestino].push(cartaInferiorDePila)
                    movimientoExitoso = true
                    if (comprobarVictoria()) {
                        Toast.makeText(this, "¡Has Ganado!", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> { }
        }

        if (movimientoExitoso) {
            removerPilaDelOrigen()
            actualizarTodasLasVistas() // Crucial
        } else {
            Toast.makeText(this, "Movimiento inválido", Toast.LENGTH_SHORT).show()
        }
        cancelarSeleccion()
    }

    private fun puedeMoverPilaATableau(cartaInferiorPilaMovida: Carta, colDestino: Int): Boolean {
        val pilaTableauDestino = pilasTableau[colDestino]
        if (pilaTableauDestino.isEmpty()) {
            return cartaInferiorPilaMovida.valor == Valor.REY
        } else {
            val cartaSuperiorDestino = pilaTableauDestino.last()
            if (!cartaSuperiorDestino.esVisible) return false
            val coloresOpuestos = (cartaInferiorPilaMovida.esRoja() && cartaSuperiorDestino.esNegra()) ||
                    (cartaInferiorPilaMovida.esNegra() && cartaSuperiorDestino.esRoja())
            val valorCorrecto = cartaInferiorPilaMovida.valor.valorNumerico == cartaSuperiorDestino.valor.valorNumerico - 1
            return coloresOpuestos && valorCorrecto
        }
    }

    private fun puedeMoverCartaAFundacion(carta: Carta, indexFundacion: Int): Boolean {
        // pilaSeleccionada.size == 1 ya se verifica antes de llamar aquí si es de una pila
        val pilaFundacionDestino = pilasFundacion[indexFundacion]
        if (pilaFundacionDestino.isEmpty()) {
            return carta.valor == Valor.AS
        } else {
            val cartaSuperiorDestino = pilaFundacionDestino.peek()
            return carta.palo == cartaSuperiorDestino.palo && carta.valor.valorNumerico == cartaSuperiorDestino.valor.valorNumerico + 1
        }
    }

    private fun removerPilaDelOrigen() {
        if (origenPilaSeleccion == null || pilaSeleccionada.isEmpty()) return

        when (origenPilaSeleccion) {
            Zona.DESCARTE -> {
                if (descarte.isNotEmpty() && descarte.last() == pilaSeleccionada.first()) {
                    descarte.removeAt(descarte.lastIndex)
                }
            }
            Zona.TABLEAU -> {
                if (indiceColumnaOrigenPila != null) {
                    val pilaOriginal = pilasTableau[indiceColumnaOrigenPila!!]
                    val numCartasARemover = pilaSeleccionada.size
                    // Remover desde el final de la pila original
                    if (pilaOriginal.size >= numCartasARemover) {
                        for (i in 0 until numCartasARemover) {
                            if (pilaOriginal.isNotEmpty()) { // Doble check
                                pilaOriginal.removeAt(pilaOriginal.lastIndex)
                            }
                        }
                    }
                    // Voltear nueva carta superior si es necesario
                    if (pilaOriginal.isNotEmpty() && !pilaOriginal.last().esVisible) {
                        pilaOriginal.last().esVisible = true
                    }
                }
            }
            Zona.FUNDACION -> {
                if (indiceColumnaOrigenPila != null) { // Aquí indiceColumnaOrigenPila es el índice de la fundación
                    val pilaOriginal = pilasFundacion[indiceColumnaOrigenPila!!]
                    if (pilaOriginal.isNotEmpty() && pilaOriginal.peek() == pilaSeleccionada.first()) {
                        pilaOriginal.pop()
                    }
                }
            }
            else -> {}
        }
    }

    private fun voltearCartaTableau(colIndex: Int) {
        cancelarSeleccion()
        val pila = pilasTableau[colIndex]
        if (pila.isNotEmpty() && !pila.last().esVisible) {
            pila.last().esVisible = true
            actualizarVistasTableau() // Podría optimizarse
        }
    }

    private fun comprobarVictoria(): Boolean {
        return pilasFundacion.all { it.size == 13 }
    }

    private fun createDropListener(zonaDestinoDrop: Zona, indiceDestinoDrop: Int): View.OnDragListener {
        return View.OnDragListener { view, event ->
            // val draggableItemView = event.localState as? View // La vista que se está arrastrando

            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    event.clipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true &&
                            event.clipDescription.label == CARTA_DRAG_TAG && pilaSeleccionada.isNotEmpty()
                    // Si no dependemos de pilaSeleccionada, el ClipData necesitaría más info.
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    view.setBackgroundResource(R.drawable.placeholder_drop_hover)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    val pilaModeloSubyacente: List<Any> = when (zonaDestinoDrop) {
                        Zona.FUNDACION -> pilasFundacion[indiceDestinoDrop]
                        Zona.TABLEAU -> pilasTableau[indiceDestinoDrop]
                        else -> emptyList()
                    }
                    if (pilaModeloSubyacente.isNotEmpty()) {
                        view.setBackgroundResource(android.R.color.transparent) // La carta de abajo se ve
                    } else {
                        when (zonaDestinoDrop) {
                            Zona.FUNDACION -> view.setBackgroundResource(R.drawable.placeholder_fundacion)
                            Zona.TABLEAU -> view.setBackgroundResource(R.drawable.placeholder_tableau)
                            else -> {}
                        }
                    }
                    true
                }
                DragEvent.ACTION_DROP -> {
                    Log.d("Solitario", "ACTION_DROP en $zonaDestinoDrop[$indiceDestinoDrop] para pila: ${pilaSeleccionada.map{it.obtenerNombreImagen()}}")
                    if (pilaSeleccionada.isNotEmpty()) {
                        intentarMoverPilaSeleccionadaA(zonaDestinoDrop, indiceDestinoDrop)
                    } else {
                        Log.e("Solitario", "Error: Pila seleccionada es null/vacía en ACTION_DROP")
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // Restaurar fondo original y opacidad de la vista que se arrastró
                    vistaCartaInferiorPilaSeleccionada?.alpha = 1.0f

                    // Restaurar el fondo del target independientemente de si el drop fue exitoso o no
                    val pilaModeloSubyacente: List<Any> = when (zonaDestinoDrop) {
                        Zona.FUNDACION -> pilasFundacion[indiceDestinoDrop]
                        Zona.TABLEAU -> pilasTableau[indiceDestinoDrop]
                        else -> emptyList()
                    }
                    if (view is ImageView && pilaModeloSubyacente.isNotEmpty()){ // Si es ImageView y tiene carta, fondo transparente
                        view.setBackgroundResource(android.R.color.transparent)
                    } else { // Si es FrameLayout o ImageView vacía, restaurar placeholder
                        when (zonaDestinoDrop) {
                            Zona.FUNDACION -> view.setBackgroundResource(R.drawable.placeholder_fundacion)
                            Zona.TABLEAU -> view.setBackgroundResource(R.drawable.placeholder_tableau)
                            else -> {}
                        }
                    }
                    // Si el drop no fue exitoso (intentarMover no canceló), o si el drop fue fuera de un target
                    // la selección se cancela en intentarMoverPilaSeleccionadaA.
                    // Si el drag termina sin un ACTION_DROP válido (ej. soltado fuera), necesitamos cancelar.
                    if (!event.result) { // event.result es false si el drop no fue manejado (o no ocurrió en un listener que devolvió true)
                        Log.d("Solitario", "Drag ended sin drop exitoso. Cancelando selección.")
                        cancelarSeleccion()
                    }
                    // Si el drop fue exitoso, la función intentarMoverPilaSeleccionadaA ya llama a cancelarSeleccion()
                    // así que no necesitamos hacerlo explícitamente aquí.
                    true
                }
                else -> false
            }
        }
    }
}
