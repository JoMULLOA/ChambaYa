package com.example.chambaya.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chambaya.databinding.ActivityRegisterBinding
import com.example.chambaya.viewmodel.LoginViewModel
import com.example.chambaya.viewmodel.RegisterResult

/**
 * Activity de Registro
 * Permite a los usuarios crear una nueva cuenta
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // Botón de Registro
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            viewModel.register(email, password, confirmPassword, name)
        }

        // Link a Login
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        // Observar resultado del registro
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is RegisterResult.Success -> {
                    Toast.makeText(
                        this,
                        "Cuenta creada exitosamente. Por favor inicia sesión",
                        Toast.LENGTH_LONG
                    ).show()

                    // Volver a LoginActivity
                    finish()
                }
                is RegisterResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                null -> {
                    // No hacer nada
                }
            }
        }

        // Observar estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnRegister.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearResults()
    }
}

