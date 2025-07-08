package com.dutisoft.places

import AppDatabase
import Category
import Place
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dutisoft.places.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = DatabaseProvider.getDatabase(this)

        // Mostrar el diálogo al presionar el FAB
        binding.fab.setOnClickListener {
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_new_place, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.editTextText2)
        val categoryInput = dialogView.findViewById<EditText>(R.id.editTextText3)
        val descInput = dialogView.findViewById<EditText>(R.id.editTextText4)
        val saveButton = dialogView.findViewById<Button>(R.id.button2)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val categoryName = categoryInput.text.toString().trim()
            val description = descInput.text.toString().trim()

            if (name.isEmpty() || categoryName.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val categoryId = withContext(Dispatchers.IO) {
                        val dao = database.categoryDao()
                        val existingCategory = dao.getCategoryByName(categoryName)
                        if (existingCategory != null) {
                            existingCategory.id
                        } else {
                            val newCategory = Category(name = categoryName)
                            dao.insertCategory(newCategory) // Debe devolver el ID (Long)
                        }
                    }

                    val place = Place(
                        name = name,
                        description = description,
                        latitude = 0.0, // Por ahora, sin geolocalización
                        longitude = 0.0,
                        categoryId = categoryId.toInt()
                    )

                    withContext(Dispatchers.IO) {
                        database.placeDao().insertPlace(place)
                    }

                    Toast.makeText(this@MainActivity, "Lugar guardado correctamente", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e("ERROR", e.toString())
                    Toast.makeText(this@MainActivity, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()
    }





}
