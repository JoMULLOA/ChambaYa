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
    private lateinit var nearbyAdapter: JobsAdapter
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
        observeJobs()
    }

    private fun setupRecyclerViews() {
        nearbyAdapter = JobsAdapter { job ->
            viewModel.selectJob(job)
            findNavController().navigate(R.id.action_jobListFragment_to_jobMapFragment)
        }
        binding.recyclerViewNearby.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearbyAdapter
        }

        newAdapter = JobsAdapter { job ->
            viewModel.selectJob(job)
            findNavController().navigate(R.id.action_jobListFragment_to_jobMapFragment)
        }
        binding.recyclerViewNew.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newAdapter
        }
    }

    private fun observeJobs() {
        viewModel.nearbyJobs.observe(viewLifecycleOwner) { nearbyAdapter.submitList(it) }
        viewModel.newJobs.observe(viewLifecycleOwner) { newAdapter.submitList(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
