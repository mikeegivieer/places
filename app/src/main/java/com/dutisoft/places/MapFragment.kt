package com.dutisoft.places

// Android
import AppDatabase
import Place
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

// Google Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

// Mapbox
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions

// Kotlin Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Project
import com.dutisoft.places.databinding.FragmentMapBinding
import com.google.gson.JsonObject


class MapFragment : Fragment() {


    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var currentStyle: Style? = null

    private lateinit var database: AppDatabase
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                currentStyle?.let {
                    loadPlacesAndShowMarkers(it)
                } ?: Toast.makeText(
                    requireContext(), getString(R.string.permiso_no_concecido), Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permiso_de_ubicacion_denegado),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = DatabaseProvider.getDatabase(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapInitOptions = MapInitOptions(
            requireContext(),
            resourceOptions = ResourceOptions.Builder().accessToken(BuildConfig.MAPBOX_TOKEN)
                .build()
        )

        mapView = MapView(requireContext(), mapInitOptions)

        binding.mapContainer.addView(mapView)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            currentStyle = style
            loadPlacesAndShowMarkers(style)
        }


        binding.toolbar.setNavigationOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.toolbar_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_my_places -> {
                        showSearchDialog()
                        true
                    }

                    R.id.menu_settings -> {

                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }

    }

    private fun showSearchDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.my_places, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)
        val filterSpinner = dialogView.findViewById<Spinner>(R.id.filter_spinner)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.results_recycler_view)

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView)
            .setCancelable(true).create()

        val allPlaces = mutableListOf<Place>()
        val resultAdapter = ResultAdapter(allPlaces)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = resultAdapter

        val spinnerAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, listOf("")
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = allPlaces.filter {
                    it.name.contains(newText ?: "", ignoreCase = true)
                }
                resultAdapter.updateList(filtered)
                return true
            }
        })

        lifecycleScope.launch {
            val places = withContext(Dispatchers.IO) {
                database.placeDao().getAllPlaces()
            }
            allPlaces.clear()
            allPlaces.addAll(places)
            resultAdapter.updateList(allPlaces)
        }

        dialog.show()
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


                if (places.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_hay_lugares_registrados),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {


                    places.forEach { place ->
                        val placePoint = Point.fromLngLat(place.longitude, place.latitude)

                        val json = JsonObject().apply {
                            addProperty("id", place.id)
                            addProperty("name", place.name)
                            addProperty("description", place.description ?: "Sin descripción")
                        }

                        val placeMarker = PointAnnotationOptions()
                            .withPoint(placePoint)
                            .withIconImage("marker-icon")
                            .withData(json)

                        pointAnnotationManager.create(placeMarker)

                        pointAnnotationManager.addClickListener { annotation ->
                            val data = annotation.getData()
                            val name = data?.asJsonObject?.get("name")?.asString ?: "Lugar"
                            val description = data?.asJsonObject?.get("description")?.asString ?: "Sin descripción"

                            AlertDialog.Builder(requireContext())
                                .setTitle(name)
                                .setMessage(description)
                                .setPositiveButton("Cerrar", null)
                                .show()

                            true
                        }

                    }



                }

                // Obtiene ubicación actual
                if (ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                    return@launch
                }


                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userPoint = Point.fromLngLat(location.longitude, location.latitude)

                        // Agrega marcador para ubicación actual
                        val hereMarker =
                            PointAnnotationOptions().withPoint(userPoint).withIconImage("here-icon")

                        pointAnnotationManager.create(hereMarker)

                        //Mover cámara al usuario
                        mapView.getMapboxMap().setCamera(
                            CameraOptions.Builder().center(userPoint).zoom(14.0)
                                .build()
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_se_pudo_obtener_la_ubicaci_n_actual),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {/* TODO implement exception */
            }
        }
    }

}


class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(text: String) {
        itemView.findViewById<TextView>(android.R.id.text1).text = text
    }
}

class ResultAdapter(private var items: List<Place>) :
    RecyclerView.Adapter<ResultAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textViewPlaceName)
        val category: TextView = itemView.findViewById(R.id.textViewCategory)
        val description: TextView = itemView.findViewById(R.id.textViewDescription)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_to_go, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = items[position]
        holder.name.text = place.name
        holder.category.text = place.categoryId.toString()
        holder.description.text = place.description


    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<Place>) {
        items = newItems
        notifyDataSetChanged()
    }
}
