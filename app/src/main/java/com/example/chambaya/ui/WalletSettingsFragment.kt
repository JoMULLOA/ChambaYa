package com.example.chambaya.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chambaya.databinding.FragmentWalletSettingsBinding
import com.example.chambaya.worker.WalletBalanceReminderScheduler

/**
 * Fragment para configurar las notificaciones de billetera
 */
class WalletSettingsFragment : Fragment() {

    private var _binding: FragmentWalletSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar si el worker está activo
        updateWorkerStatus()

        // Botón para activar notificaciones
        binding.btnEnableNotifications.setOnClickListener {
            enableWalletNotifications()
        }

        // Botón para desactivar notificaciones
        binding.btnDisableNotifications.setOnClickListener {
            disableWalletNotifications()
        }
    }

    private fun updateWorkerStatus() {
        val isScheduled = WalletBalanceReminderScheduler.isWalletBalanceCheckScheduled(requireContext())

        if (isScheduled) {
            binding.tvWorkerStatus.text = "Notificaciones activas"
            binding.btnEnableNotifications.isEnabled = false
            binding.btnDisableNotifications.isEnabled = true
        } else {
            binding.tvWorkerStatus.text = "Notificaciones desactivadas"
            binding.btnEnableNotifications.isEnabled = true
            binding.btnDisableNotifications.isEnabled = false
        }
    }

    private fun enableWalletNotifications() {
        val threshold = binding.etThreshold.text.toString().toIntOrNull()

        WalletBalanceReminderScheduler.scheduleWalletBalanceCheck(
            context = requireContext(),
            intervalHours = 24,
            customThreshold = threshold
        )

        Toast.makeText(
            requireContext(),
            "Notificaciones de billetera activadas (cada 24 horas)",
            Toast.LENGTH_LONG
        ).show()

        updateWorkerStatus()
    }

    private fun disableWalletNotifications() {
        WalletBalanceReminderScheduler.cancelWalletBalanceCheck(requireContext())

        Toast.makeText(
            requireContext(),
            "Notificaciones de billetera desactivadas",
            Toast.LENGTH_SHORT
        ).show()

        updateWorkerStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

