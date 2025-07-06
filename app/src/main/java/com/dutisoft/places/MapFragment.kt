package com.dutisoft.places

import Category
import Place
import User
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dutisoft.places.databinding.FragmentMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = DatabaseProvider.getDatabase(requireContext())
                val categoryDao = db.categoryDao()

                // Limpia tablas
                db.clearAllTables()

                // Inserta usuario
                val user1 = User(username = "miguel", profilePhotoUri = "")
                db.userDao().insertUser(user1)

                // Inserta categorías
                val natureId = categoryDao.insertCategory(Category(name = "Naturaleza")).toInt()
                val cityId = categoryDao.insertCategory(Category(name = "Ciudad")).toInt()
                val cultureId = categoryDao.insertCategory(Category(name = "Cultura")).toInt()

                val categoryIds = listOf(natureId, cityId, cultureId)

                // Inserta lugares
                repeat(10) { index ->
                    val place = Place(
                        ownerUsername = "miguel",
                        name = "Lugar $index",
                        description = "Descripción del lugar $index",
                        latitude = 19.43 + index * 0.001,
                        longitude = -99.13 - index * 0.001,
                        categoryId = categoryIds[index % 3],
                        photoUri = null,
                        isPrivate = (index % 2 == 0)
                    )
                    db.placeDao().insertPlace(place)
                }

                // Recorre y muestra contenido de la base de datos
                val user = db.userDao().getUser(user1.username)
                val categories = categoryDao.getAllCategories()

                if (user != null) {
                    Log.d("RoomDump", "→ ${user.username} | ${user.profilePhotoUri}")
                }

                Log.d("RoomDump", "📦 Categorías:")
                categories.forEach { category ->
                    Log.d("RoomDump", "→ ${category.id}: ${category.name}")
                }

                Log.d("RoomDump", "📦 Lugares:")
                user?.let { db.placeDao().getPlacesByUser(it.username) }?.forEach { place ->
                    Log.d(
                        "RoomDump",
                        "→ ${place.name} | ${place.description} | ${place.latitude}, ${place.longitude} | CatID: ${place.categoryId} | Privado: ${place.isPrivate}"
                    )
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("RoomInit", "Error al inicializar la base de datos: ${e.message}")
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}