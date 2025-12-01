package com.example.chambaya.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

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

                binding.btnContact.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:555-0123")
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

