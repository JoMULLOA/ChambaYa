package com.example.chambaya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.chambaya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        // Configurar Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.jobListFragment,
                R.id.jobMapFragment,
                R.id.publishFragment,
                R.id.messagesFragment,
                R.id.profileFragment
            ),
            binding.drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navigationView, navController)

        // Configurar BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.jobListFragment,
                R.id.jobMapFragment,
                R.id.publishFragment,
                R.id.messagesFragment,
                R.id.profileFragment -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
                else -> false
            }
        }

        // Sincronizar el item seleccionado con el destino actual
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.menu.findItem(destination.id)?.isChecked = true

            when (destination.id) {
                R.id.jobListFragment,
                R.id.jobMapFragment,
                R.id.publishFragment,
                R.id.messagesFragment,
                R.id.profileFragment -> {
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    binding.topAppBar.title = ""
                }
                else -> {
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
