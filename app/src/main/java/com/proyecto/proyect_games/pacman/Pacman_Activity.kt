package com.proyecto.proyect_games.pacman

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random
import com.proyecto.proyect_games.CatalogActivity
import com.proyecto.proyect_games.R

class Pacman_Activity : AppCompatActivity() {

    // HUD
    private lateinit var tvScore: TextView
    private lateinit var tvHigh: TextView
    private lateinit var tvMsg: TextView
    private lateinit var life1: TextView
    private lateinit var life2: TextView
    private lateinit var life3: TextView

    // Buttons
    private lateinit var btnStart: TextView
    private lateinit var btnReset: TextView
    private lateinit var btnUp: TextView
    private lateinit var btnDown: TextView
    private lateinit var btnLeft: TextView
    private lateinit var btnRight: TextView
    private lateinit var btnPause: TextView

    // Layers (we draw custom Views inside each)
    private lateinit var layerMaze: FrameLayout
    private lateinit var layerPellets: FrameLayout
    private lateinit var layerActors: FrameLayout
    private lateinit var layerFx: FrameLayout

    // Game + rendering
    private val handler = Handler(Looper.getMainLooper())
    private val tickMs = 16L // ~60fps
    private var paused = true
    private var running = false

    private lateinit var mazeView: MazeView
    private lateinit var pelletView: PelletsView
    private lateinit var actorsView: ActorsView

    // ======= GAME STATE =======
    private val cols = 19
    private val rows = 21

    // 0 = wall, 1 = pellet, 2 = empty, 3 = power pellet
    // Simple symmetric level (feel free to replace with your own)
    private val level: Array<IntArray> = arrayOf(
        intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,0),
        intArrayOf(0,1,0,0,0,1,0,1,0,1,0,1,0,1,0,0,0,1,0),
        intArrayOf(0,3,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,3,0),
        intArrayOf(0,1,0,1,0,1,0,0,0,1,0,0,0,1,0,1,0,1,0),
        intArrayOf(0,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,0),
        intArrayOf(0,1,0,0,0,1,0,1,0,0,0,1,0,1,0,0,0,1,0),
        intArrayOf(0,1,1,1,0,1,1,1,1,0,1,1,1,1,0,1,1,1,0),
        intArrayOf(0,1,0,1,0,0,0,0,1,0,1,0,0,0,0,1,0,1,0),
        intArrayOf(0,1,0,1,1,1,1,0,1,2,1,0,1,1,1,1,0,1,0), // 2 = empty spawn corridor
        intArrayOf(0,1,0,1,0,0,1,0,0,2,0,0,1,0,0,1,0,1,0),
        intArrayOf(0,1,1,1,1,0,1,1,1,1,1,1,1,0,1,1,1,1,0),
        intArrayOf(0,1,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,1,0),
        intArrayOf(0,1,1,0,1,1,1,1,1,0,1,1,1,1,1,0,1,1,0),
        intArrayOf(0,0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,1,0,0),
        intArrayOf(0,1,1,1,1,1,1,0,1,1,1,0,1,1,1,1,1,1,0),
        intArrayOf(0,1,0,0,0,1,1,1,1,0,1,1,1,0,0,0,0,1,0),
        intArrayOf(0,3,1,1,0,1,0,0,0,1,0,0,0,1,0,1,1,3,0),
        intArrayOf(0,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,0),
        intArrayOf(0,1,0,0,0,0,0,1,0,1,0,1,0,0,0,0,0,1,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
    )

    private val pellets = Array(rows) { IntArray(cols) }
    private var pelletCount = 0

    private data class Actor(var cx: Int, var cy: Int, var dir: Dir = Dir.NONE)
    private enum class Dir { UP, DOWN, LEFT, RIGHT, NONE }

    private var pacman = Actor(9, 13, Dir.LEFT)
    private val ghosts = arrayOf(
        Actor(9, 9, Dir.RIGHT),
        Actor(8, 9, Dir.LEFT),
        Actor(10, 9, Dir.UP)
    )

    private var score = 0
    private var high = 0
    private var lives = 3
    private var powerTimer = 0 // frames remaining in power mode

    // Cell metrics (computed by MazeView)
    private var cell = 0f
    private var leftPad = 0f
    private var topPad = 0f

    // Movement pacing
    private var stepAccumulator = 0f
    private var stepEvery = 0.12f // cells per frame (~speed)

    // Input buffer: desired direction (turn at next intersection)
    private var wanted: Dir = Dir.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacman)

        tvScore = findViewById(R.id.tvScore)
        tvHigh = findViewById(R.id.tvHighScore)
        tvMsg = findViewById(R.id.tvCenterMessage)
        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)

        btnStart = findViewById(R.id.btnStart)
        btnReset = findViewById(R.id.btnReset)
        btnUp = findViewById(R.id.btnUp)
        btnDown = findViewById(R.id.btnDown)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        btnPause = findViewById(R.id.btnPause)

        layerMaze = findViewById(R.id.layer_maze)
        layerPellets = findViewById(R.id.layer_pellets)
        layerActors = findViewById(R.id.layer_actors)
        layerFx = findViewById(R.id.layer_fx)

        // Views to draw
        mazeView = MazeView()
        pelletView = PelletsView()
        actorsView = ActorsView()

        layerMaze.addView(mazeView)
        layerPellets.addView(pelletView)
        layerActors.addView(actorsView)

        // Controls
        fun press(d: Dir) { wanted = d }
        btnUp.setOnClickListener { press(Dir.UP) }
        btnDown.setOnClickListener { press(Dir.DOWN) }
        btnLeft.setOnClickListener { press(Dir.LEFT) }
        btnRight.setOnClickListener { press(Dir.RIGHT) }
        btnPause.setOnClickListener { pauseResume() }
        btnReset.setOnClickListener { resetAll() }
        btnStart.setOnClickListener {
            if (!running) {
                startLevel()
            } else {
                pauseResume()
            }
        }

        resetAll()
    }

    private fun resetAll() {
        score = 0
        lives = 3
        updateLives()
        startLevel()
    }

    private fun startLevel() {
        // Copy pellets from level (1-> pellet, 3->power)
        pelletCount = 0
        for (y in 0 until rows) for (x in 0 until cols) {
            pellets[y][x] = when (level[y][x]) {
                1 -> 1.also { pelletCount++ }
                3 -> 3.also { pelletCount++ }
                else -> 0
            }
        }
        pacman = Actor(9, 13, Dir.LEFT)
        ghosts[0] = Actor(9, 9, Dir.RIGHT)
        ghosts[1] = Actor(8, 9, Dir.LEFT)
        ghosts[2] = Actor(10, 9, Dir.UP)
        wanted = Dir.LEFT
        powerTimer = 0
        paused = false
        running = true
        tvMsg.text = "READY!"
        tvMsg.visibility = View.VISIBLE
        handler.removeCallbacks(loop)
        handler.postDelayed({
            tvMsg.visibility = View.INVISIBLE
            handler.post(loop)
        }, 800)
        redrawAll()
    }

    private fun pauseResume() {
        if (!running) return
        paused = !paused
        tvMsg.text = if (paused) "PAUSED" else ""
        tvMsg.visibility = if (paused) View.VISIBLE else View.INVISIBLE
        if (!paused) handler.post(loop)
    }

    private fun updateLives() {
        life1.alpha = if (lives >= 1) 1f else 0.2f
        life2.alpha = if (lives >= 2) 1f else 0.2f
        life3.alpha = if (lives >= 3) 1f else 0.2f
    }

    private val loop = object : Runnable {
        override fun run() {
            if (!running || paused) return
            step()
            redrawAll()
            handler.postDelayed(this, tickMs)
        }
    }

    private fun step() {
        // smoother pacing: move a cell when accumulator exceeds 1.0
        stepAccumulator += stepEvery
        while (stepAccumulator >= 1f) {
            stepAccumulator -= 1f
            tickCell()
        }
    }

    private fun tickCell() {
        // turn if wanted dir is available
        if (canMove(pacman.cx, pacman.cy, wanted)) pacman.dir = wanted
        // move pacman one cell
        val next = nextCell(pacman.cx, pacman.cy, pacman.dir)
        if (isWall(next.first, next.second)) {
            // blocked; try stop
            pacman.dir = Dir.NONE
        } else {
            pacman.cx = wrapX(next.first)
            pacman.cy = next.second
        }

        // eat pellet
        val p = pellets[pacman.cy][pacman.cx]
        if (p == 1) { pellets[pacman.cy][pacman.cx] = 0; score += 10; pelletCount-- }
        if (p == 3) { pellets[pacman.cy][pacman.cx] = 0; score += 50; pelletCount--; powerTimer = 60 * 6 } // 6 sec
        tvScore.text = "%06d".format(score)

        // ghosts AI: simple random with bias to chase
        for (g in ghosts) {
            val options = mutableListOf<Dir>()
            for (d in Dir.values()) {
                if (d == Dir.NONE) continue
                val (nx, ny) = nextCell(g.cx, g.cy, d)
                if (!isWall(nx, ny)) options.add(d)
            }
            if (options.isNotEmpty()) {
                g.dir = pickGhostDir(g, options)
                val (nx, ny) = nextCell(g.cx, g.cy, g.dir)
                g.cx = wrapX(nx)
                g.cy = ny
            }
        }

        // collisions
        for (g in ghosts) {
            if (g.cx == pacman.cx && g.cy == pacman.cy) {
                if (powerTimer > 0) {
                    score += 200
                    // send ghost back to house
                    g.cx = 9; g.cy = 9; g.dir = Dir.values().random()
                } else {
                    // lose life
                    lives--
                    updateLives()
                    tvMsg.text = "OUCH!"
                    tvMsg.visibility = View.VISIBLE
                    paused = true
                    handler.postDelayed({
                        if (lives <= 0) {
                            gameOver()
                        } else {
                            pacman = Actor(9, 13, Dir.LEFT)
                            wanted = Dir.LEFT
                            tvMsg.visibility = View.INVISIBLE
                            paused = false
                            handler.post(loop)
                        }
                    }, 900)
                    break
                }
            }
        }

        if (pelletCount <= 0) {
            // next level
            score += 500
            tvScore.text = "%06d".format(score)
            tvMsg.text = "LEVEL UP!"
            tvMsg.visibility = View.VISIBLE
            paused = true
            handler.postDelayed({ startLevel() }, 1200)
        }

        if (powerTimer > 0) powerTimer--
    }

    private fun gameOver() {
        running = false
        paused = true
        tvMsg.text = "GAME OVER"
        tvMsg.visibility = View.VISIBLE
        if (score > high) {
            high = score
            tvHigh.text = "%06d".format(high)
            Toast.makeText(this, "NEW HIGH SCORE!", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== helpers =====
    private fun isWall(x: Int, y: Int): Boolean {
        val yy = y.coerceIn(0, rows - 1)
        val xx = ((x % cols) + cols) % cols
        return level[yy][xx] == 0
    }

    private fun canMove(x: Int, y: Int, dir: Dir): Boolean {
        if (dir == Dir.NONE) return false
        val (nx, ny) = nextCell(x, y, dir)
        return !isWall(nx, ny)
    }

    private fun nextCell(x: Int, y: Int, dir: Dir): Pair<Int, Int> = when (dir) {
        Dir.UP -> x to (y - 1)
        Dir.DOWN -> x to (y + 1)
        Dir.LEFT -> (x - 1) to y
        Dir.RIGHT -> (x + 1) to y
        Dir.NONE -> x to y
    }

    private fun wrapX(x: Int): Int {
        var nx = x
        if (nx < 0) nx = cols - 1
        if (nx >= cols) nx = 0
        return nx
    }

    private fun pickGhostDir(g: Actor, options: List<Dir>): Dir {
        // Slight chase bias: choose dir that reduces manhattan distance to pacman
        var best: Dir = options.random()
        var bestScore = Int.MAX_VALUE
        for (d in options) {
            val (nx, ny) = nextCell(g.cx, g.cy, d)
            val man = abs(nx - pacman.cx) + abs(ny - pacman.cy)
            if (powerTimer > 0) {
                // frightened: go away
                if (-man < bestScore) {
                    bestScore = -man
                    best = d
                }
            } else {
                if (man < bestScore) {
                    bestScore = man
                    best = d
                }
            }
        }
        // random spice
        if (Random.nextFloat() < 0.2f) return options.random()
        return best
    }

    private fun redrawAll() {
        pelletView.invalidate()
        actorsView.invalidate()
        // maze static – only size depends on view bounds
        mazeView.invalidate()
    }

    // ====== RENDERING VIEWS ======
    inner class MazeView : View(this) {
        private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#092347")
            style = Paint.Style.FILL
        }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F4B9A")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val size = min(w, h)
            cell = size / maxOf(cols, rows)
            // center the grid
            leftPad = (w - cols * cell) / 2f
            topPad = (h - rows * cell) / 2f

            // draw walls
            for (y in 0 until rows) for (x in 0 until cols) {
                if (level[y][x] == 0) {
                    val l = leftPad + x * cell
                    val t = topPad + y * cell
                    canvas.drawRoundRect(l, t, l + cell, t + cell, 8f, 8f, wallPaint)
                    canvas.drawRoundRect(l, t, l + cell, t + cell, 8f, 8f, borderPaint)
                }
            }
        }
    }

    inner class PelletsView : View(this) {
        private val pellet = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        private val power = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFE94D") }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val rSmall = cell * 0.12f
            val rBig = cell * 0.22f
            for (y in 0 until rows) for (x in 0 until cols) {
                val type = pellets[y][x]
                if (type == 0) continue
                val cx = leftPad + (x + 0.5f) * cell
                val cy = topPad + (y + 0.5f) * cell
                if (type == 1) {
                    canvas.drawCircle(cx, cy, rSmall, pellet)
                } else if (type == 3) {
                    canvas.drawCircle(cx, cy, rBig, power)
                }
            }
        }
    }

    inner class ActorsView : View(this) {
        private val pacPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFE94D") }
        private val eyeWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        private val eyeBlack = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
        private val ghostPaints = arrayOf(
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF5B6B") },
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#60C3FF") },
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF9D00") }
        )
        private val ghostFright = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2C6BFF") }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // PAC-MAN
            drawPacman(canvas)
            // GHOSTS
            ghosts.forEachIndexed { i, g ->
                drawGhost(canvas, g, if (powerTimer > 0) ghostFright else ghostPaints[i % ghostPaints.size])
            }
        }

        private fun drawPacman(c: Canvas) {
            val cx = leftPad + (pacman.cx + 0.5f) * cell
            val cy = topPad + (pacman.cy + 0.5f) * cell
            val r = cell * 0.42f
            // mouth animation
            val mouth = (System.currentTimeMillis() / 40 % 20).toInt()
            val mouthDeg = 20 + kotlin.math.abs(10 - mouth) * 3
            val start = when (pacman.dir) {
                Dir.RIGHT, Dir.NONE -> mouthDeg.toFloat()
                Dir.LEFT -> 180f + mouthDeg
                Dir.UP -> 270f + mouthDeg
                Dir.DOWN -> 90f + mouthDeg
            }
            c.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), start, 360f - mouthDeg * 2, true, pacPaint)
            // eye
            val ex = cx + when (pacman.dir) { Dir.LEFT -> -r*0.3f; Dir.RIGHT, Dir.NONE -> r*0.3f; Dir.UP, Dir.DOWN -> 0f }
            val ey = cy - r*0.3f
            c.drawCircle(ex, ey, r*0.18f, eyeWhite)
            c.drawCircle(ex, ey, r*0.08f, eyeBlack)
        }

        private fun drawGhost(c: Canvas, g: Actor, body: Paint) {
            val cx = leftPad + (g.cx + 0.5f) * cell
            val cy = topPad + (g.cy + 0.5f) * cell
            val r = cell * 0.42f
            val rect = RectF(cx - r, cy - r, cx + r, cy + r)
            // body
            val path = Path()
            path.addArc(RectF(rect.left, rect.top, rect.right, rect.top + r*2f), 180f, 180f)
            path.lineTo(rect.right, rect.bottom)
            // wavy bottom
            val bumps = 4
            val step = (rect.right - rect.left) / bumps
            for (i in bumps downTo 0) {
                val x = rect.left + i * step
                val y = rect.bottom - if (i % 2 == 0) r*0.2f else 0f
                path.lineTo(x, y)
            }
            path.close()
            c.drawPath(path, body)

            // eyes
            val ex = cx - r*0.25f
            val ey = cy - r*0.1f
            c.drawCircle(ex, ey, r*0.18f, eyeWhite)
            c.drawCircle(ex + r*0.5f, ey, r*0.18f, eyeWhite)
            val pupilDx = when (g.dir) { Dir.LEFT -> -r*0.08f; Dir.RIGHT -> r*0.08f; else -> 0f }
            val pupilDy = when (g.dir) { Dir.UP -> -r*0.08f; Dir.DOWN -> r*0.08f; else -> 0f }
            c.drawCircle(ex + pupilDx, ey + pupilDy, r*0.09f, eyeBlack)
            c.drawCircle(ex + r*0.5f + pupilDx, ey + pupilDy, r*0.09f, eyeBlack)
        }
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }
}
