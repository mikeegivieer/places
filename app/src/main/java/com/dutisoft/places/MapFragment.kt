package com.dutisoft.places

import Category
import Place
import User
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dutisoft.places.databinding.FragmentMapBinding
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



        val db = context?.let { DatabaseProvider.getDatabase(it) }

        lifecycleScope.launch {
            val db = context?.let { DatabaseProvider.getDatabase(it) }
            val categoryDao = db?.categoryDao()

            // Limpia tablas (opcional para reinicio de datos)
            db?.clearAllTables()

            // Inserta usuario
            val user = User(username = "miguel", profilePhotoUri = null)
            db?.userDao()?.insertUser(user)

            // Inserta categorías de ejemplo
            val natureId = categoryDao?.insertCategory(Category(name = "Naturaleza"))?.toInt()
            val cityId = categoryDao?.insertCategory(Category(name = "Ciudad"))?.toInt()
            val cultureId = categoryDao?.insertCategory(Category(name = "Cultura"))?.toInt()

            val categoryIds = listOf(natureId, cityId, cultureId)

            // Inserta 10 lugares con categorías aleatorias
            repeat(10) { index ->
                val place = Place(
                    ownerUsername = "miguel",
                    name = "Lugar $index",
                    description = "Descripción del lugar $index",
                    latitude = 19.43 + index * 0.001,
                    longitude = -99.13 - index * 0.001,
                    categoryId = categoryIds[index % 3],
                    photoUri = null
                )
                db?.placeDao()?.insertPlace(place)
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}