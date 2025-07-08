package com.dutisoft.places

import AppDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dutisoft.places.databinding.FragmentMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = DatabaseProvider.getDatabase(requireContext())

        // Configurar el click en el ícono de la toolbar
        binding.toolbar.setNavigationOnClickListener {
            fetchAllPlaces()
        }
    }

    private fun fetchAllPlaces() {
        lifecycleScope.launch {
            try {
                val places = withContext(Dispatchers.IO) {
                    database.placeDao().getAllPlaces()
                }

                // Por ahora, los mostramos en consola
                for (place in places) {
                    Log.d("MapFragment", "Lugar: ${place.name} - ${place.description}")
                }

                // Aquí podrías actualizar una lista, mostrar en el mapa, etc.

            } catch (e: Exception) {
                Log.e("MapFragment", "Error al obtener lugares: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
