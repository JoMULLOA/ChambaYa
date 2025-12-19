package com.example.chambaya.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.chambaya.data.local.ChambaYaDatabase
import com.example.chambaya.model.ContratoInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class MyHiresViewModel(application: Application) : AndroidViewModel(application) {

    private val contratoDao = ChambaYaDatabase.getDatabase(application).contratoDao()
    private val userDao = ChambaYaDatabase.getDatabase(application).userDao()

    val myHires: LiveData<List<ContratoInfo>> = userDao.getLoggedInUserFlow().flatMapLatest { user ->
        user?.let {
            contratoDao.getContratosInfoBySolicitanteId(it.id)
        } ?: flowOf(emptyList())
    }.asLiveData(viewModelScope.coroutineContext)
}
