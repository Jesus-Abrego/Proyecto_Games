package com.proyecto.proyect_games

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject

class Login : AppCompatActivity() {

    // Login views (usa los ids de tu XML actual)
    private lateinit var etUser: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: TextView
    private lateinit var btnCreate: TextView
    private lateinit var btnForgot: TextView

    // Storage
    private val prefs by lazy { getSharedPreferences("auth_demo", Context.MODE_PRIVATE) }
    private val USERS = "users_json"
    private val SESSION = "session_username" // Clave para guardar el username de la sesión

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- VERIFICACIÓN DE SESIÓN EXISTENTE ---
        val sessionUsername = prefs.getString(SESSION, null)
        if (sessionUsername != null) {
            // Ya hay una sesión activa, ir directamente al catálogo
            Toast.makeText(this, "Bienvenido de nuevo, $sessionUsername", Toast.LENGTH_SHORT).show()
            navigateToCatalog(sessionUsername) // Llama a una función separada para la navegación
            return // Importante: evita que se ejecute el resto de onCreate si ya hay sesión
        }
        // --- FIN DE VERIFICACIÓN DE SESIÓN ---

        // No hay sesión, configurar la pantalla de login
        setContentView(R.layout.login) // tu layout de login

        etUser = findViewById(R.id.etUser)
        etPass = findViewById(R.id.etPass)
        btnLogin = findViewById(R.id.btnLogin)
        btnCreate = findViewById(R.id.btnCreate)
        btnForgot = findViewById(R.id.btnForgot)

        btnLogin.setOnClickListener { doLogin() }
        btnCreate.setOnClickListener { showRegisterSheet() }
        btnForgot.setOnClickListener {
            Toast.makeText(this, "Feature demo: recuperación no implementada", Toast.LENGTH_SHORT).show()
        }
    }

    // ====== LOGIN ======
    private fun doLogin() {
        val id = etUser.text.toString().trim()
        val pass = etPass.text.toString()

        if (id.isEmpty() || pass.isEmpty()) {
            toast("Completa usuario/correo y contraseña")
            return
        }

        val users = readUsers()
        val user = users.firstOrNull {
            it.username.equals(id, true) || it.email.equals(id, true)
        }

        if (user == null) {
            toast("Usuario/correo no encontrado")
            return
        }

        if (user.password != pass) { // (DEMO) en producción: usa hash seguro
            toast("Contraseña incorrecta")
            return
        }

        // Guardar la sesión
        prefs.edit().putString(SESSION, user.username).apply()
        toast("¡Bienvenido, ${user.displayName}!")

        navigateToCatalog(user.username, user.displayName) // Llama a la función de navegación
    }

    /**
     * Navega a CatalogActivity, opcionalmente pasando información del usuario.
     */
    private fun navigateToCatalog(username: String?, displayName: String? = null) {
        val intent = Intent(this, CatalogActivity::class.java)
        // Puedes pasar datos si CatalogActivity los necesita
        if (username != null) {
            intent.putExtra("USERNAME_KEY", username)
        }
        if (displayName != null) {
            intent.putExtra("DISPLAY_NAME_KEY", displayName)
        }
        startActivity(intent)
        finish() // Cierra LoginActivity para que no quede en la pila
    }

    // ====== REGISTER SHEET ======
    private fun showRegisterSheet() {
        val dialog = BottomSheetDialog(this, com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog)
        val view = LayoutInflater.from(this).inflate(R.layout.register_sheet, null, false)
        dialog.setContentView(view)

        val etUsername = view.findViewById<EditText>(R.id.etRegUsername)
        val etDisplay = view.findViewById<EditText>(R.id.etRegDisplayName)
        val etEmail = view.findViewById<EditText>(R.id.etRegEmail)
        val etPhone = view.findViewById<EditText>(R.id.etRegPhone)
        val rbMale = view.findViewById<RadioButton>(R.id.rbMale)
        val rbFemale = view.findViewById<RadioButton>(R.id.rbFemale)
        val rbOther = view.findViewById<RadioButton>(R.id.rbOther)
        val etPass1 = view.findViewById<EditText>(R.id.etRegPass)
        val etPass2 = view.findViewById<EditText>(R.id.etRegPass2)
        val btnDoRegister = view.findViewById<TextView>(R.id.btnDoRegister)

        btnDoRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val display = etDisplay.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass1 = etPass1.text.toString()
            val pass2 = etPass2.text.toString()
            val gender = when {
                rbMale.isChecked -> "Hombre"
                rbFemale.isChecked -> "Mujer"
                else -> "No especifico"
            }

            // Validaciones
            if (username.length < 3) { toast("El usuario debe tener al menos 3 caracteres"); return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Correo inválido"); return@setOnClickListener }
            if (phone.isNotEmpty() && phone.length < 8) { toast("Teléfono inválido"); return@setOnClickListener }
            if (pass1.length < 6) { toast("La contraseña debe tener al menos 6 caracteres"); return@setOnClickListener }
            if (pass1 != pass2) { toast("Las contraseñas no coinciden"); return@setOnClickListener }

            val users = readUsers()
            if (users.any { it.username.equals(username, true) }) {
                toast("Ese nombre de usuario ya está en uso")
                return@setOnClickListener
            }

            // Guardar
            val newUser = User(
                username = username,
                displayName = if (display.isNotBlank()) display else username,
                email = email,
                phone = phone,
                gender = gender,
                password = pass1 // (DEMO) guarda hash en producción
            )
            saveUser(newUser)
            dialog.dismiss()
            toast("Cuenta creada. ¡Ahora inicia sesión!")
            // auto-rellenar login
            etUser.setText(username)
            etPass.setText("") // Limpiar campo de contraseña
            etUser.requestFocus() // Poner foco en el usuario
        }
        dialog.show()
    }

    // ====== STORAGE (SharedPreferences + JSON) ======
    data class User(
        val username: String,
        val displayName: String,
        val email: String,
        val phone: String,
        val gender: String,
        val password: String
    )

    private fun readUsers(): MutableList<User> {
        val list = mutableListOf<User>()
        val json = prefs.getString(USERS, "[]") ?: "[]"
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    User(
                        username = o.getString("username"),
                        displayName = o.getString("displayName"),
                        email = o.getString("email"),
                        phone = o.optString("phone"),
                        gender = o.optString("gender", "No especifico"),
                        password = o.getString("password")
                    )
                )
            }
        } catch (e: Exception) {
            // En caso de JSON malformado, devuelve lista vacía y quizás loguea el error
            // o borra las SharedPreferences corruptas.
            prefs.edit().remove(USERS).apply() // Opcional: limpiar si está corrupto
            e.printStackTrace()
        }
        return list
    }

    private fun saveUser(u: User) {
        val list = readUsers()
        list.add(u)
        val arr = JSONArray()
        list.forEach {
            val o = JSONObject()
            o.put("username", it.username)
            o.put("displayName", it.displayName)
            o.put("email", it.email)
            o.put("phone", it.phone)
            o.put("gender", it.gender)
            o.put("password", it.password)
            arr.put(o)
        }
        prefs.edit().putString(USERS, arr.toString()).apply()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}