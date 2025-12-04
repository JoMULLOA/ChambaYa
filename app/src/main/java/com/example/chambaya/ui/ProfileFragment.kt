package com.example.chambaya.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.chambaya.databinding.FragmentProfileBinding
import com.example.chambaya.viewmodel.ProfileViewModel
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private var tempImageUri: Uri? = null

    // Launcher para solicitar permiso de galería
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            showPermissionDeniedMessage("galería")
        }
    }

    // Launcher para solicitar permiso de cámara
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showPermissionDeniedMessage("cámara")
        }
    }

    // Launcher para seleccionar imagen de galería
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfileImage(it)
        }
    }

    // Launcher para tomar foto con cámara
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let {
                viewModel.updateProfileImage(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()

        // Cargar datos del usuario
        viewModel.loadCurrentUser()
    }

    private fun setupObservers() {
        // Observar usuario actual
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserName.text = it.name
                binding.tvUserEmail.text = it.email
                binding.tvUserProfile.visibility = View.VISIBLE

                // Cargar imagen de perfil
                loadProfileImage(it.profileImage)
            }
        }

        // Observar actualización de imagen
        viewModel.profileImageUpdated.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "Imagen actualizada",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error al actualizar imagen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Aquí podrías mostrar un ProgressBar si lo deseas
        }
    }

    private fun setupClickListeners() {
        // Botón para editar foto
        binding.ivEditPhoto.setOnClickListener {
            showImagePickerDialog()
        }

        // Click en la imagen para verla en grande o cambiarla
        binding.ivProfileImage.setOnClickListener {
            if (viewModel.currentUser.value?.profileImage != null) {
                showImageOptionsDialog()
            } else {
                showImagePickerDialog()
            }
        }

        // Botón de logout
        binding.btnLogout.setOnClickListener {
            viewModel.logout()

            // Navegar a LoginActivity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Galería", "Cámara")

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun showImageOptionsDialog() {
        val options = arrayOf("Ver imagen", "Cambiar imagen", "Eliminar imagen")

        AlertDialog.Builder(requireContext())
            .setTitle("Opciones de imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Ver imagen (podrías implementar un visor full screen)
                        Toast.makeText(requireContext(), "Ver imagen", Toast.LENGTH_SHORT).show()
                    }
                    1 -> showImagePickerDialog()
                    2 -> confirmRemoveImage()
                }
            }
            .show()
    }

    private fun confirmRemoveImage() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar imagen")
            .setMessage("¿Estás seguro de que deseas eliminar tu imagen de perfil?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.removeProfileImage()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openGallery() {
        // Verificar permiso según versión de Android
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido, abrir galería
                galleryLauncher.launch("image/*")
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Mostrar explicación de por qué necesitamos el permiso
                showPermissionRationale(
                    "Permiso de Galería",
                    "ChambaYa necesita acceso a tus fotos para que puedas seleccionar una imagen de perfil.",
                    permission,
                    galleryPermissionLauncher
                )
            }
            else -> {
                // Solicitar permiso directamente
                galleryPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido, abrir cámara
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Mostrar explicación de por qué necesitamos el permiso
                showPermissionRationale(
                    "Permiso de Cámara",
                    "ChambaYa necesita acceso a tu cámara para que puedas tomar una foto de perfil.",
                    Manifest.permission.CAMERA,
                    cameraPermissionLauncher
                )
            }
            else -> {
                // Solicitar permiso directamente
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        // Crear archivo temporal para la foto
        val photoFile = File(requireContext().cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        tempImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        cameraLauncher.launch(tempImageUri)
    }

    private fun showPermissionRationale(
        title: String,
        message: String,
        permission: String,
        launcher: androidx.activity.result.ActivityResultLauncher<String>
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Permitir") { _, _ ->
                launcher.launch(permission)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPermissionDeniedMessage(feature: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Permiso denegado")
            .setMessage("No se puede acceder a $feature sin el permiso necesario. Puedes activarlo en Configuración → Aplicaciones → ChambaYa → Permisos.")
            .setPositiveButton("Ir a Configuración") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun loadProfileImage(imagePath: String?) {
        if (imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .apply(RequestOptions().transform(CircleCrop()))
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .error(android.R.drawable.ic_menu_myplaces)
                    .into(binding.ivProfileImage)
            } else {
                setDefaultProfileImage()
            }
        } else {
            setDefaultProfileImage()
        }
    }

    private fun setDefaultProfileImage() {
        Glide.with(this)
            .load(android.R.drawable.ic_menu_myplaces)
            .apply(RequestOptions().transform(CircleCrop()))
            .into(binding.ivProfileImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

