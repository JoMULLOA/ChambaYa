package com.example.chambaya

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chambaya.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        // Configurar App Bar con los fragmentos principales
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            setOf(R.id.jobListFragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.jobListFragment, R.id.jobMapFragment),
            binding.drawerLayout
        // Ocultar el título en la pantalla principal para un diseño más limpio
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.jobListFragment -> {
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    binding.topAppBar.title = ""
                }
                else -> {
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
            }
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
