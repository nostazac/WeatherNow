package com.example.majiraapp.ui.cities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.majiraapp.databinding.FragmentCitiesBinding

private const val TAG = "CitiesFragment"

class CitiesFragment : Fragment() {
    private var _binding: FragmentCitiesBinding? = null
    private val binding get() = _binding!!

    private lateinit var cityAdapter: CityAdapter
    private val viewModel: CitiesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView: Inflating CitiesFragment layout")
        _binding = FragmentCitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up CitiesFragment")

        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        cityAdapter = CityAdapter(
            onItemClick = { city ->
                Log.d(TAG, "City clicked: ${city.name}")
                viewModel.toggleCitySelection(city)
            },
            onDeleteClick = { city ->
                Log.d(TAG, "Delete clicked for city: ${city.name}")
                viewModel.removeCity(city)
            }
        )
        binding.rvCities.layoutManager = LinearLayoutManager(context)
        binding.rvCities.adapter = cityAdapter
    }

    private fun setupSearch() {
        Log.d(TAG, "Setting up search functionality")
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "Search query changed: ${s.toString()}")
                viewModel.filterCities(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeViewModel() {
        Log.d(TAG, "Observing ViewModel")
        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            Log.d(TAG, "Cities updated: ${cities.map { it.name }}")
            cityAdapter.submitList(cities)
        }

        viewModel.selectedCities.observe(viewLifecycleOwner) { selectedCities ->
            Log.d(TAG, "Selected cities updated: ${selectedCities.map { it.name }}")
            cityAdapter.submitList(selectedCities)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up CitiesFragment")
        _binding = null
    }
}