package com.dutisoft.places

import AppDatabase
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dutisoft.places.databinding.FragmentMapBinding
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dutisoft.places.BuildConfig


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: AppDatabase
    private lateinit var mapView: MapView

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

        // 🔥 Inicializamos el MapView con el token
        val mapInitOptions = MapInitOptions(
            requireContext(),
            resourceOptions = ResourceOptions.Builder()
                .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
                .build()
        )

        mapView = MapView(requireContext(), mapInitOptions)

        // 🔥 Agregamos el MapView al contenedor
        binding.mapContainer.addView(mapView)

        // 🔥 Cargamos el estilo del mapa
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

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

                Log.d("MapFragment", "Total lugares: ${places.size}")

                if (places.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay lugares registrados", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_place_list, null)

                val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewPlaces)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = PlaceAdapter(places)

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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        _binding = null
    }
}
