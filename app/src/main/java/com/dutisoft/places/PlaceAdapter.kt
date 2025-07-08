package com.dutisoft.places

import Place
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaceAdapter(private val places: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageViewPlace)
        val name: TextView = view.findViewById(R.id.textViewPlaceName)
        val category: TextView = view.findViewById(R.id.textViewCategory)
        val description: TextView = view.findViewById(R.id.textViewDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        Log.d("Adapter", "Binding ${place.name}")
        holder.name.text = place.name
        holder.category.text = "Categoría ID: ${place.categoryId ?: "N/A"}"
        holder.description.text = place.description
        // Aquí puedes cargar la imagen desde `place.photoUri` si es necesario
    }

    override fun getItemCount() = places.size
}
