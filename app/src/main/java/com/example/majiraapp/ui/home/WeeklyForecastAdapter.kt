package com.example.majiraapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.majiraapp.R
import java.text.SimpleDateFormat
import java.util.*

class WeeklyForecastAdapter : ListAdapter<WeeklyForecastItem, WeeklyForecastAdapter.ViewHolder>(WeeklyForecastDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weekly_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val minMaxTempTextView: TextView = itemView.findViewById(R.id.minMaxTempTextView)
        private val weatherIconImageView: ImageView = itemView.findViewById(R.id.weatherIconImageView)

        fun bind(item: WeeklyForecastItem) {
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            dayTextView.text = dayFormat.format(item.date)
            dateTextView.text = dateFormat.format(item.date)
            minMaxTempTextView.text = "${item.minTemp}° / ${item.maxTemp}°"

            // Set weather icon based on the icon code from the API
            val iconResId = when (item.weatherIcon) {
                "01d", "01n" -> R.drawable.ic_sun
                "02d", "02n", "03d", "03n", "04d", "04n" -> R.drawable.ic_partly_cloudy
                "09d", "09n", "10d", "10n" -> R.drawable.ic_rain
                "11d", "11n" -> R.drawable.ic_thunderstorm
                "13d", "13n" -> R.drawable.ic_snow
                else -> R.drawable.ic_partly_cloudy
            }
            weatherIconImageView.setImageResource(iconResId)
        }
    }
}

class WeeklyForecastDiffCallback : DiffUtil.ItemCallback<WeeklyForecastItem>() {
    override fun areItemsTheSame(oldItem: WeeklyForecastItem, newItem: WeeklyForecastItem): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: WeeklyForecastItem, newItem: WeeklyForecastItem): Boolean {
        return oldItem == newItem
    }
}

data class WeeklyForecastItem(
    val date: Date,
    val minTemp: Int,
    val maxTemp: Int,
    val weatherIcon: String
)