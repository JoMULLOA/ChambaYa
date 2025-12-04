package com.example.chambaya.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chambaya.MainActivity
import com.example.chambaya.databinding.ActivityLoginBinding
import com.example.chambaya.viewmodel.LoginResult
import com.example.chambaya.viewmodel.LoginViewModel

/**
 * Activity de Login
 * Maneja el inicio de sesión de usuarios utilizando Room Database
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // Botón de Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            viewModel.login(email, password)
        }

        // Link a Registro
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupObservers() {
        // Observar resultado del login
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(
                        this,
                        "Bienvenido ${result.user.name}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navegar a MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is LoginResult.Error -> {
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
                binding.btnLogin.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearResults()
    }
}

