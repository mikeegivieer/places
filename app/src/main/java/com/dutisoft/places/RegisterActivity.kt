package com.dutisoft.places

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var avatarView: AvatarAnimationSurfaceView
    private lateinit var arrowLeft: ImageView
    private lateinit var arrowRight: ImageView

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
        avatarView.setFrames(listOf(bitmap)) // solo una imagen por ahora
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        avatarView = findViewById(R.id.surfaceView)
        arrowLeft = findViewById(R.id.arrowLeft)
        arrowRight = findViewById(R.id.arrowRight)

        loadCurrentAvatar()

        arrowLeft.setOnClickListener {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else avatarResIds.size - 1
            loadCurrentAvatar()
        }

        arrowRight.setOnClickListener {
            currentIndex = (currentIndex + 1) % avatarResIds.size
            loadCurrentAvatar()
        }
    }
}
