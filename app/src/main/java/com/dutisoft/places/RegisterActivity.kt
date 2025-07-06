package com.dutisoft.places

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var avatarView: AvatarAnimationSurfaceView
    private lateinit var arrowLeft: ImageView
    private lateinit var arrowRight: ImageView
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button

    private val avatarResIds = listOf(
        R.drawable.avatar0,
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar6,
        R.drawable.avatar7
    )

    private var currentIndex = 0

    private fun loadCurrentAvatar() {
        val bitmap = BitmapFactory.decodeResource(resources, avatarResIds[currentIndex])
        avatarView.setFrames(listOf(bitmap))
    }

    private fun isValidInput(input: String): Boolean {
        return input.isNotBlank() && !input.contains(" ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        avatarView = findViewById(R.id.surfaceView)
        arrowLeft = findViewById(R.id.arrowLeft)
        arrowRight = findViewById(R.id.arrowRight)
        usernameInput = findViewById(R.id.editTextText)
        passwordInput = findViewById(R.id.editTextTextPassword2)
        registerButton = findViewById(R.id.button)

        loadCurrentAvatar()

        arrowLeft.setOnClickListener {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else avatarResIds.size - 1
            loadCurrentAvatar()
        }

        arrowRight.setOnClickListener {
            currentIndex = (currentIndex + 1) % avatarResIds.size
            loadCurrentAvatar()
        }

        registerButton.setOnClickListener {
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
                    val selectedAvatarRes = avatarResIds[currentIndex]

                    // Aquí puedes guardar los datos (ej. en base de datos, preferences, etc.)
                    Toast.makeText(
                        this,
                        "Usuario: $username\nPassword: $password\nAvatar: avatar$currentIndex",
                        Toast.LENGTH_LONG
                    ).show()

                    // TODO: Guardar en persistencia o enviar al servidor si es necesario
                }
            }
        }
    }
}
