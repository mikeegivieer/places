package com.dutisoft.places

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dutisoft.places.databinding.ActivityMainBinding
import com.dutisoft.places.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var avatarView: AvatarAnimationSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        avatarView = findViewById(R.id.surfaceView)

        val frames = listOf(
            BitmapFactory.decodeResource(resources, R.drawable.avatar),
          //  BitmapFactory.decodeResource(resources, R.drawable.avatar2),
           // BitmapFactory.decodeResource(resources, R.drawable.avatar3),
            // Agrega los que tengas
        )

        avatarView.setFrames(frames)
    }
}
