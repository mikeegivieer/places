package com.dutisoft.places

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button

    private fun isValidInput(input: String): Boolean {
        return input.isNotBlank() && !input.contains(" ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameInput = findViewById(R.id.editTextText)
        passwordInput = findViewById(R.id.editTextTextPassword2)
        loginButton = findViewById(R.id.button)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            when {
                !isValidInput(username) -> {
                    Toast.makeText(this, "Username inválido", Toast.LENGTH_SHORT).show()
                }

                !isValidInput(password) -> {
                    Toast.makeText(this, "Password inválido", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val db = DatabaseProvider.getDatabase(this@LoginActivity)
                            val userDao = db.userDao()
                            val user = userDao.getUser(username)

                            runOnUiThread {
                                if (user != null && user.password == password) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Inicio de sesión exitoso",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Ir a MainActivity
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Usuario o contraseña incorrectos",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Error al acceder a la base de datos: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }
}
