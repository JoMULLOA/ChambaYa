package com.example.chambaya.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object ConnectionStatusManager {

    private const val PREFS_NAME = "connection_status"
    private const val KEY_IS_CONNECTED = "is_connected"
    private const val KEY_LAST_CHECK = "last_check"

    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> = _connectionStatus

    private val _connectionMessage = MutableLiveData<ConnectionMessage?>()
    val connectionMessage: LiveData<ConnectionMessage?> = _connectionMessage

    fun updateConnectionStatus(context: Context, isConnected: Boolean) {
        val prefs = getPreferences(context)
        val previousStatus = prefs.getBoolean(KEY_IS_CONNECTED, true)

        if (previousStatus != isConnected) {
            prefs.edit().apply {
                putBoolean(KEY_IS_CONNECTED, isConnected)
                putLong(KEY_LAST_CHECK, System.currentTimeMillis())
                apply()
            }

            _connectionStatus.postValue(isConnected)

            val message = if (isConnected) {
                ConnectionMessage("Conectado", ConnectionType.CONNECTED)
            } else {
                ConnectionMessage("Sin conexion", ConnectionType.DISCONNECTED)
            }
            _connectionMessage.postValue(message)
        }
    }

    fun isConnected(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_CONNECTED, true)
    }

    fun clearMessage() {
        _connectionMessage.postValue(null)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}

data class ConnectionMessage(
    val message: String,
    val type: ConnectionType
)

enum class ConnectionType {
    CONNECTED,
    DISCONNECTED
}

