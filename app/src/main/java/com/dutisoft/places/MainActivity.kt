package com.dutisoft.places

import AppDatabase
import Category
import Place
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.dutisoft.places.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineStart
import java.io.ByteArrayOutputStream
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var photoUri: Uri
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private var currentDialogView: View? = null
    private var currentEncodedPhoto: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = DatabaseProvider.getDatabase(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        takePhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                    val imageView = currentDialogView?.findViewById<ImageView>(R.id.imageView2)

                    imageBitmap?.let { bitmap ->
                        imageView?.setImageBitmap(bitmap)
                        currentEncodedPhoto = bitmapToBase64(bitmap)
                    }
                }
            }

        // Mostrar el diálogo al presionar el FAB
        binding.fab.setOnClickListener {
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_new_place, null)
        currentDialogView = dialogView
        val LOCATION_PERMISSION_REQUEST_CODE = 100
        val nameInput = dialogView.findViewById<EditText>(R.id.editTextText2)
        val takePhoto = dialogView.findViewById<ImageView>(R.id.imageView2)

        val categoryInput = dialogView.findViewById<EditText>(R.id.editTextText3)
        val descInput = dialogView.findViewById<EditText>(R.id.editTextText4)
        val saveButton = dialogView.findViewById<Button>(R.id.button2)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        takePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePhotoLauncher.launch(intent)
        }


        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val categoryName = categoryInput.text.toString().trim()
            val description = descInput.text.toString().trim()

            if (name.isEmpty() || categoryName.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar permisos de ubicación
            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                Toast.makeText(this, "Permiso de ubicación requerido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT)
                        .show()
                    return@addOnSuccessListener
                }

                val latitude = location.latitude
                val longitude = location.longitude

                lifecycleScope.launch {
                    try {
                        val categoryId = withContext(Dispatchers.IO) {
                            val dao = database.categoryDao()
                            val existingCategory = dao.getCategoryByName(categoryName)
                            if (existingCategory != null) {
                                existingCategory.id
                            } else {
                                val newCategory = Category(name = categoryName)
                                dao.insertCategory(newCategory)
                            }
                        }

                        val encodedPhoto = currentEncodedPhoto


                        val place = Place(
                            name = name,
                            description = description,
                            latitude = latitude,
                            longitude = longitude,
                            categoryId = categoryId.toInt(),
                            photoUri = encodedPhoto ?: ""
                        )

                        withContext(Dispatchers.IO) {
                            database.placeDao().insertPlace(place)
                        }

                        Toast.makeText(
                            this@MainActivity,
                            "Lugar guardado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    } catch (e: Exception) {
                        Log.e("ERROR", e.toString())
                        Toast.makeText(
                            this@MainActivity,
                            "Error al guardar: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


        dialog.show()
    }



    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }



}
