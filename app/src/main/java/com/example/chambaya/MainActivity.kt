package com.example.chambaya

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.chambaya.databinding.ActivityMainBinding
import com.example.chambaya.receiver.NetworkChangeReceiver
import com.example.chambaya.utils.ConnectionStatusManager
import com.example.chambaya.utils.ConnectionType
import com.example.chambaya.worker.WalletBalanceReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val networkReceiver = NetworkChangeReceiver()
    private var hideJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        // Inicializar Worker de recordatorio de billetera
        initializeWalletBalanceWorker()

        // Inicializar monitoreo de conexion
        initializeNetworkMonitoring()

        // Configurar Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.jobListFragment,
                R.id.jobMapFragment,
                R.id.publishFragment,
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

    /**
     * Inicializa el Worker de recordatorio de billetera
     * Se ejecuta cada 24 horas para verificar el saldo
     */
    private fun initializeWalletBalanceWorker() {
        WalletBalanceReminderScheduler.scheduleWalletBalanceCheck(
            context = this,
            intervalHours = 24,
            customThreshold = null
        )
    }

    /**
     * Inicializa el monitoreo de conexion a Internet
     */
    private fun initializeNetworkMonitoring() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        ConnectionStatusManager.connectionMessage.observe(this) { message ->
            message?.let {
                showConnectionStatus(it.message, it.type)
                ConnectionStatusManager.clearMessage()
            }
        }
    }

    /**
     * Muestra la barra de estado de conexion
     */
    private fun showConnectionStatus(message: String, type: ConnectionType) {
        hideJob?.cancel()

        binding.connectionStatusBar.tvConnectionStatus.text = message

        val backgroundColor = when (type) {
            ConnectionType.CONNECTED -> ContextCompat.getColor(this, R.color.green)
            ConnectionType.DISCONNECTED -> ContextCompat.getColor(this, R.color.red)
        }
        binding.connectionStatusBar.connectionStatusBar.setBackgroundColor(backgroundColor)

        binding.connectionStatusBar.connectionStatusBar.visibility = View.VISIBLE
        val slideDown = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        binding.connectionStatusBar.connectionStatusBar.startAnimation(slideDown)

        hideJob = CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            hideConnectionStatus()
        }
    }

    /**
     * Oculta la barra de estado de conexion
     */
    private fun hideConnectionStatus() {
        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
        binding.connectionStatusBar.connectionStatusBar.startAnimation(slideUp)
        binding.connectionStatusBar.connectionStatusBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
        hideJob?.cancel()
    }
}
