package com.example.chambaya.ui

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chambaya.R
import com.example.chambaya.databinding.FragmentJobDetailBinding
import com.example.chambaya.model.JobType

class JobDetailFragment : Fragment() {

    private var _binding: FragmentJobDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JobViewModel by activityViewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("JobDetailFragment", "Permiso de notificaciones concedido")
        } else {
            Log.d("JobDetailFragment", "Permiso de notificaciones denegado")
            Toast.makeText(requireContext(), "Las notificaciones están deshabilitadas", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkNotificationPermission()
        setupClickListeners()
        setupObservers()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("JobDetailFragment", "Permiso de notificaciones ya concedido")
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.selectedJob.observe(viewLifecycleOwner) { job ->
            job?.let {
                binding.tvJobTitle.text = it.title
                binding.tvProviderName.text = it.providerName
                binding.tvPrice.text = it.price
                binding.tvDescription.text = it.description
                binding.tvDistance.text = "${it.distance} km"
                binding.tvCategory.text = it.category

                if (it.rating != null) {
                    binding.tvRating.text = "${it.rating}"
                    binding.tvRating.visibility = View.VISIBLE
                    binding.ivStar.visibility = View.VISIBLE
                } else {
                    binding.tvRating.visibility = View.GONE
                    binding.ivStar.visibility = View.GONE
                }

                val typeColor = if (it.type == JobType.OFFER) {
                    resources.getColor(android.R.color.holo_blue_light, null)
                } else {
                    resources.getColor(android.R.color.holo_green_light, null)
                }
                binding.tvJobType.setBackgroundColor(typeColor)
                binding.tvJobType.text = if (it.type == JobType.OFFER) "Oferta" else "Demanda"

                binding.btnViewMap.setOnClickListener {
                    findNavController().navigate(R.id.jobMapFragment)
                }

                if (it.type == JobType.OFFER) {
                    binding.btnHire.visibility = View.VISIBLE
                    binding.btnHire.setOnClickListener { view ->
                        Log.d("JobDetailFragment", "Botón contratar presionado")
                        showConfirmationDialog()
                    }
                } else {
                    binding.btnHire.visibility = View.GONE
                }
            }
        }

        viewModel.hireStatus.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(requireContext(), "¡Contratación exitosa!", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnHire.isEnabled = !isLoading
        }
    }

    private fun showConfirmationDialog() {
        Log.d("JobDetailFragment", "Mostrando diálogo de confirmación")
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar Contratación")
            .setMessage("¿Estás seguro de que quieres contratar este servicio? Se descontará el monto de tu billetera.")
            .setPositiveButton("Confirmar") { _, _ ->
                Log.d("JobDetailFragment", "Usuario confirmó la contratación")
                viewModel.hireJob()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                Log.d("JobDetailFragment", "Usuario canceló la contratación")
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.clearError()
        // Clear hireStatus as well to avoid re-triggering on config change
    }
}

