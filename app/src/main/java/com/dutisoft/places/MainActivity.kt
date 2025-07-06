package com.dutisoft.places

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.dutisoft.places.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mostrar el diálogo al presionar el FAB
        binding.fab.setOnClickListener {
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        // Inflar el layout personalizado
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_new_place, null)

        // Crear y mostrar el diálogo
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()
    }
}
