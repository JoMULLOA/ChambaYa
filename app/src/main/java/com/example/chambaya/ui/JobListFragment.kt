package com.example.chambaya.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chambaya.R
import com.example.chambaya.databinding.FragmentJobListBinding

class JobListFragment : Fragment() {

    private var _binding: FragmentJobListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JobViewModel by activityViewModels()
    private lateinit var nearbyAdapter: JobsVerticalAdapter
    private lateinit var newAdapter: JobsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupButtons()
    }

    private fun setupRecyclerViews() {
        // Adapter para "Cerca de ti" (vertical)
        nearbyAdapter = JobsVerticalAdapter { job ->
            viewModel.selectJob(job)
            findNavController().navigate(R.id.jobDetailFragment)
        }
        binding.recyclerViewNearby.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearbyAdapter
        }

        newAdapter = JobsAdapter { job ->
            viewModel.selectJob(job)
            findNavController().navigate(R.id.jobDetailFragment)
        }

        // Observadores de LiveData
        viewModel.nearbyJobs.observe(viewLifecycleOwner) { jobs ->
            nearbyAdapter.submitList(jobs)
        }

        viewModel.newJobs.observe(viewLifecycleOwner) { jobs ->
            newAdapter.submitList(jobs)
        }
    }

    private fun setupButtons() {
        // Botón para ver el mapa
        binding.fabViewMap.setOnClickListener {
            findNavController().navigate(R.id.jobMapFragment)
        }

        // Botón para publicar servicio
        binding.btnPublish.setOnClickListener {
            // TODO: Navegar a pantalla de publicar servicio
            // Por ahora solo mostramos un mensaje
        }

        // Barra de búsqueda
        binding.searchCard.setOnClickListener {
            // TODO: Navegar a pantalla de búsqueda
            // Por ahora solo mostramos un mensaje
        }
