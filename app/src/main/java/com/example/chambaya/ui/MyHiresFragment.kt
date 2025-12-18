package com.example.chambaya.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chambaya.databinding.FragmentMyHiresBinding

class MyHiresFragment : Fragment() {

    private var _binding: FragmentMyHiresBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyHiresViewModel by viewModels()
    private lateinit var hiresAdapter: MyHiresAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyHiresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        hiresAdapter = MyHiresAdapter()
        binding.rvMyHires.apply {
            adapter = hiresAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.myHires.observe(viewLifecycleOwner) { hires ->
            hiresAdapter.submitList(hires)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
