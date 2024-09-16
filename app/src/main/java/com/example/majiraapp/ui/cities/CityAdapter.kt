package com.example.majiraapp.ui.cities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.majiraapp.databinding.ItemCityBinding
import com.example.majiraapp.ui.data.City

class CityAdapter(
    private val onItemClick: (City) -> Unit,
    private val onDeleteClick: (City) -> Unit
) : ListAdapter<City, CityAdapter.ViewHolder>(CityDiffCallback()) {

    private var selectedCity: City? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = getItem(position)
        holder.bind(city)
    }

    inner class ViewHolder(private val binding: ItemCityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: City) {
            binding.tvCityName.text = city.name
            binding.tvTemperature.text = if (city.temperature != 0) "${city.temperature}°C" else ""
            binding.tvTime.text = city.time

            val isSelected = city == selectedCity
            binding.btnDelete.visibility = if (isSelected) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                Log.d("CityAdapter", "Item clicked: ${city.name}")
                if (selectedCity == city) {
                    // If the same city is clicked again, deselect it
                    selectedCity = null
                } else {
                    // Select the clicked city
                    selectedCity = city
                }
                notifyDataSetChanged() // Refresh the list to update visibility of delete buttons
                onItemClick(city)
            }

            binding.btnDelete.setOnClickListener {
                Log.d("CityAdapter", "Delete clicked: ${city.name}")
                onDeleteClick(city)
                selectedCity = null
                notifyDataSetChanged()
            }
        }
    }

    class CityDiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem == newItem
        }
    }
}