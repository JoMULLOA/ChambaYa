package com.example.chambaya.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.chambaya.databinding.FragmentJobMapBinding
import com.example.chambaya.model.Job
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class JobMapFragment : Fragment() {

    private var _binding: FragmentJobMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JobViewModel by activityViewModels()
    private val markers = mutableListOf<Marker>()
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        setupButtons()
        observeJobs()
    }

    private fun setupMap() {
        val context = requireContext()
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName

        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(19.4326, -99.1332))
        }

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), binding.mapView)
        myLocationOverlay.enableMyLocation()
        binding.mapView.overlays.add(myLocationOverlay)

        requestLocationPermission()
    }

    private fun setupButtons() {
        binding.btnMyLocation.setOnClickListener { moveToUserLocation() }
        binding.btnCloseMap.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            viewModel.clearSelectedJob()
        }
    }

    private fun observeJobs() {
        viewModel.jobs.observe(viewLifecycleOwner) { addMarkersToMap(it) }
        viewModel.selectedJob.observe(viewLifecycleOwner) { job -> job?.let { focusOnJob(it) } }
    }

    private fun requestLocationPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            enableMyLocation()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            enableMyLocation()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.enableFollowLocation()
            moveToUserLocation()
        }
    }

    private fun moveToUserLocation() {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    binding.mapView.controller.animateTo(userLocation)
                    binding.mapView.controller.setZoom(15.0)
                }
            }
        }
    }

    private fun addMarkersToMap(jobs: List<Job>) {
        markers.forEach { binding.mapView.overlays.remove(it) }
        markers.clear()

        jobs.forEach { job ->
            val marker = Marker(binding.mapView)
            marker.position = GeoPoint(job.latitude, job.longitude)
            marker.title = job.title
            marker.snippet = "${job.providerName} - ${job.price}"
            marker.setOnMarkerClickListener { clickedMarker, _ ->
                viewModel.selectJob(job)
                clickedMarker.showInfoWindow()
                true
            }
            binding.mapView.overlays.add(marker)
            markers.add(marker)
        }
        binding.mapView.invalidate()
    }

    private fun focusOnJob(job: Job) {
        val position = GeoPoint(job.latitude, job.longitude)
        binding.mapView.controller.animateTo(position)
        binding.mapView.controller.setZoom(16.0)
        markers.find { it.position.latitude == job.latitude && it.position.longitude == job.longitude }
            ?.showInfoWindow()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 100
    }
}

