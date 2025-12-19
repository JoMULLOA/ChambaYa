package com.example.chambaya.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.chambaya.R
import com.example.chambaya.databinding.FragmentPublishBinding
import com.example.chambaya.model.JobType
import com.example.chambaya.viewmodel.PublishResult
import com.example.chambaya.viewmodel.PublishViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PublishViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var locationSelected = false
    
    // Launcher para solicitar permiso de ubicación
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getMyLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso de ubicación denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        setupCategoryDropdown()
        setupClickListeners()
        setupObservers()
    }
    
    private fun setupCategoryDropdown() {
        val categories = arrayOf(
            "Limpieza",
            "Reparaciones",
            "Construcción",
            "Electricidad",
            "Plomería",
            "Jardinería",
            "Pintura",
            "Carpintería",
            "Tecnología",
            "Educación",
            "Transporte",
            "Otros"
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        
        binding.actvCategory.setAdapter(adapter)
    }
    
    private fun setupClickListeners() {
        // Botón usar mi ubicación
        binding.btnUseMyLocation.setOnClickListener {
            requestLocationPermission()
        }
        
        // Botón publicar
        binding.btnPublish.setOnClickListener {
            publishJob()
        }
    }
    
    private fun setupObservers() {
        // Observar resultado de publicación
        viewModel.publishResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PublishResult.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "¡Trabajo publicado exitosamente!",
                        Toast.LENGTH_LONG
                    ).show()
                    clearForm()
                }
                is PublishResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                null -> {
                    // No hacer nada
                }
            }
        }
        
        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnPublish.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnPublish.isEnabled = true
            }
        }
    }
    
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getMyLocation()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    
    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    locationSelected = true
                    binding.tvLocationStatus.text = 
                        "Ubicación: ${String.format("%.4f", currentLatitude)}, ${String.format("%.4f", currentLongitude)}"
                    binding.tvLocationStatus.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.chambaya_green)
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
    }
    
    private fun publishJob() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        
        val type = if (binding.rbOffer.isChecked) {
            JobType.OFFER
        } else {
            JobType.DEMAND
        }
        
        // Validar ubicación
        if (!locationSelected) {
            Toast.makeText(
                requireContext(),
                "Por favor selecciona una ubicación",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Publicar trabajo
        viewModel.publishJob(
            title = title,
            description = description,
            price = price,
            type = type,
            category = category,
            latitude = currentLatitude,
            longitude = currentLongitude
        )
    }
    
    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.etPrice.text?.clear()
        binding.actvCategory.text?.clear()
        binding.rbOffer.isChecked = true
        locationSelected = false
        binding.tvLocationStatus.text = "Ubicación no seleccionada"
        binding.tvLocationStatus.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.gray)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearResults()
        _binding = null
    }
}

