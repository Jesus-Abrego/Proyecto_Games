package com.proyecto.proyect_games

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.ActivityNotFoundException
import android.widget.TextView

import com.proyecto.proyect_games.blackjack.Blackjack_Activity
import com.proyecto.proyect_games.brickbreaker.BrickBreacker_Activity
import com.proyecto.proyect_games.snake.Snake_Activity
import com.proyecto.proyect_games.clicks123.Click123_Activity
import com.proyecto.proyect_games.pacman.Pacman_Activity
import com.proyecto.proyect_games.ruleta.Ruleta_Activity
import com.proyecto.proyect_games.solitario.Solitario_Activity
import com.proyecto.proyect_games.sudoku.Sudoku_Activity

class CatalogActivity : AppCompatActivity() {

    private lateinit var layoutBlackjackGame: LinearLayout
    private lateinit var layoutBrickBreakerGame: LinearLayout
    private lateinit var layoutSnakeGame: LinearLayout
    private lateinit var layoutClick123Game: LinearLayout
    private lateinit var layoutPacmanGame: LinearLayout
    private lateinit var layoutRuletaGame: LinearLayout
    private lateinit var layoutSolitarioGame: LinearLayout
    private lateinit var layoutSudokuGame: LinearLayout
    private lateinit var btnSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Inicializar vistas
        layoutBlackjackGame = findViewById(R.id.layoutBlackjackGame)
        layoutBrickBreakerGame = findViewById(R.id.layoutBrickBreakerGame)
        layoutSnakeGame = findViewById(R.id.layoutSnakeGame)
        layoutClick123Game = findViewById(R.id.layoutClick123Game)
        layoutPacmanGame = findViewById(R.id.layoutPacmanGame)
        layoutRuletaGame = findViewById(R.id.layoutRuletaGame)
        layoutSolitarioGame = findViewById(R.id.layoutSolitarioGame)
        layoutSudokuGame = findViewById(R.id.layoutSudokuGame)
        btnSignOut = findViewById(R.id.btnSignOut)

        // --- Listeners para los Juegos ---

        // Listener para lanzar el juego de Blackjack
        layoutBlackjackGame.setOnClickListener {
            try {
                val intent = Intent(this, Blackjack_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Blackjack no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Blackjack.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para lanzar el juego de Brick Breaker
        layoutBrickBreakerGame.setOnClickListener {
            try {
                val intent = Intent(this, BrickBreacker_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Brick Breaker no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Brick Breaker.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para lanzar el juego de Snake
        layoutSnakeGame.setOnClickListener {
            try {
                val intent = Intent(this, Snake_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Snake no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Snake.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para lanzar el juego de Click123
        layoutClick123Game.setOnClickListener {
            try {
                val intent = Intent(this, Click123_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Snake no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Click123.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para lanzar el juego de Pacman
        layoutPacmanGame.setOnClickListener {
            try {
                val intent = Intent(this, Pacman_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Pacman no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Pacman.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para lanzar el juego de Ruleta
        layoutRuletaGame.setOnClickListener {
            try {
                val intent = Intent(this, Ruleta_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Ruleta no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Ruleta.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para lanzar el juego de Solitario
        layoutSolitarioGame.setOnClickListener {
            try {
                val intent = Intent(this, Solitario_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Solitario no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Solitario.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para lanzar el juego de Sudoku
        layoutSudokuGame.setOnClickListener {
            try {
                val intent = Intent(this, Sudoku_Activity::class.java)
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Juego de Sudoku no encontrado. Verifica el Manifest.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al intentar iniciar Sudoku.", Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }

        // Listener para el botón de cerrar sesión
        btnSignOut.setOnClickListener {
            val prefs = getSharedPreferences("auth_demo", Context.MODE_PRIVATE)
            prefs.edit().remove("session_username").apply()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }

        // Opcional: Recuperar y mostrar nombre de usuario
        val username = intent.getStringExtra("USERNAME_KEY")
        val displayName = intent.getStringExtra("DISPLAY_NAME_KEY")
        if (displayName != null) {
            val tvCatalogTitle: TextView = findViewById(R.id.tvCatalogTitle)
            tvCatalogTitle.text = "Juegos para $displayName"
            // Toast.makeText(this, "Bienvenido de nuevo, $displayName", Toast.LENGTH_SHORT).show() // Ya mostrado en Login
        } else if (username != null) {
            val tvCatalogTitle: TextView = findViewById(R.id.tvCatalogTitle)
            tvCatalogTitle.text = "Juegos para $username"
            // Toast.makeText(this, "Bienvenido de nuevo, $username", Toast.LENGTH_SHORT).show() // Ya mostrado en Login
        }
    }
}