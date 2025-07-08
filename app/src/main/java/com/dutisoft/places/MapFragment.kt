package com.dutisoft.places

import AppDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
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

                if (places.isEmpty()) {
                    Log.d("MapFragment", "No hay lugares registrados")
                    return@launch
                }

                // Inflar layout del diálogo
                val dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_place_list, null)

                val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewPlaces)
                recyclerView.adapter = PlaceAdapter(places)

                // Mostrar el diálogo
                val dialog = android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Lugares registrados")
                    .setView(dialogView)
                    .setPositiveButton("Cerrar", null)
                    .create()

                dialog.show()

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
