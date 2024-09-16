package com.example.majiraapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.majiraapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ForecastBottomSheetFragment : BottomSheetDialogFragment() {

    private var temperature: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        temperature = arguments?.getString(ARG_TEMPERATURE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forecast_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update the UI with the temperature data
        val  temperatureTextView = view.findViewById<TextView>(R.id.temperatureTextView)
        temperatureTextView.text = temperature
    }

    companion object {
        private const val ARG_TEMPERATURE = "temperature"

        @JvmStatic
        fun newInstance(temperature: String) =
            ForecastBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TEMPERATURE, temperature)
                }
            }
    }
}
