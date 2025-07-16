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
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var currentStyle: Style? = null

    private lateinit var database: AppDatabase
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient


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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 🔥 Inicializamos el MapView con el token
        val mapInitOptions = MapInitOptions(
            requireContext(),
            resourceOptions = ResourceOptions.Builder()
                .accessToken(BuildConfig.MAPBOX_TOKEN)
                .build()
        )

        mapView = MapView(requireContext(), mapInitOptions)

        // 🔥 Agregamos el MapView al contenedor
        binding.mapContainer.addView(mapView)



        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            currentStyle = style
           // getUserLocationAndShowMarker(style)
           loadPlacesAndShowMarkers(style)
        }


        binding.toolbar.setNavigationOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.toolbar_menu, popup.menu)

            // Opcional: manejar clics en cada ítem
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_my_places -> {
                        // Acción para My places
                        showSearchDialog()
                        true
                    }
                    R.id.menu_settings -> {
                        // Acción para Settings
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }

    }


    private fun loadPlacesAndShowMarkers(style: Style) {
        // Marcador de lugares
        val placeBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker)
        val scaledPlaceBitmap = Bitmap.createScaledBitmap(placeBitmap, 100, 100, false)
        style.addImage("marker-icon", scaledPlaceBitmap)

        // Marcador de ubicación actual
        val hereBitmap = BitmapFactory.decodeResource(resources, R.drawable.here)
        val scaledHereBitmap = Bitmap.createScaledBitmap(hereBitmap, 100, 100, false)
        style.addImage("here-icon", scaledHereBitmap)

        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()

        // Lanza corrutina para obtener lugares
        lifecycleScope.launch {
            try {
                val places = withContext(Dispatchers.IO) {
                    database.placeDao().getAllPlaces()
                }

                Log.d("MapFragment", "Total lugares: ${places.size}")

                if (places.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No hay lugares registrados",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Agrega un marcador por cada lugar
                    places.forEach { place ->
                        val placePoint = Point.fromLngLat(place.longitude, place.latitude)
                        val placeMarker = PointAnnotationOptions()
                            .withPoint(placePoint)
                            .withIconImage("marker-icon")

                        pointAnnotationManager.create(placeMarker)
                    }
                }

                // Obtiene ubicación actual
                if (
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                    return@launch
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userPoint = Point.fromLngLat(location.longitude, location.latitude)

                        // Agrega marcador para ubicación actual
                        val hereMarker = PointAnnotationOptions()
                            .withPoint(userPoint)
                            .withIconImage("here-icon")

                        pointAnnotationManager.create(hereMarker)

                        // Opcional: mover cámara al usuario
                        mapView.getMapboxMap().setCamera(
                            com.mapbox.maps.CameraOptions.Builder()
                                .center(userPoint)
                                .zoom(14.0)
                                .build()
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No se pudo obtener la ubicación actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("MapFragment", "Error al obtener lugares: ${e.message}")
            }
        }
    }


    private fun getUserLocationAndShowMarker(style: Style) {
        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userPoint = Point.fromLngLat(location.longitude, location.latitude)

                // 👇 Carga y escala el ícono personalizado
                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker)
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false)
                style.addImage("marker-icon", scaledBitmap)

                val annotationApi = mapView.annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager()

                val pointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(userPoint)
                    .withIconImage("marker-icon") // usa el ID del ícono cargado

                pointAnnotationManager.create(pointAnnotationOptions)

                mapView.getMapboxMap().setCamera(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(userPoint)
                        .zoom(14.0)
                        .build()
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "No se pudo obtener la ubicación",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                    Toast.makeText(
                        requireContext(),
                        "No hay lugares registrados",
                        Toast.LENGTH_SHORT
                    ).show()
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                currentStyle?.let {
                   // getUserLocationAndShowMarker(it)
                   loadPlacesAndShowMarkers(it)
                } ?: Toast.makeText(requireContext(), "Estilo no cargado aún", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permiso de ubicación denegado",
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun showSearchDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.my_places, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)
        val filterSpinner = dialogView.findViewById<Spinner>(R.id.filter_spinner)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.results_recycler_view)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val allPlaces = mutableListOf<String>() // nombres temporales
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, allPlaces)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val resultAdapter = ResultAdapter(allPlaces)
        recyclerView.adapter = resultAdapter


        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Selecciona un filtro", "Por nombre", "Por categoría")
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = allPlaces.filter {
                    it.contains(newText ?: "", ignoreCase = true)
                }
                resultAdapter.updateList(filtered)
                return true
            }
        })


        // Llenar los datos desde Room
        lifecycleScope.launch {
            val places = withContext(Dispatchers.IO) {
                database.placeDao().getAllPlaces()
            }
            allPlaces.clear()
            allPlaces.addAll(places.map { it.name })
            recyclerView.adapter?.notifyDataSetChanged()
        }

        dialog.show()
    }



}
class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(text: String) {
        itemView.findViewById<TextView>(android.R.id.text1).text = text
    }
}

class ResultAdapter(private val items: MutableList<String>) :
    RecyclerView.Adapter<ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
