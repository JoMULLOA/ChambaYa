package com.example.chambaya.model

import androidx.room.Embedded

data class ContratoInfo(
    @Embedded(prefix = "contrato_")
    val contrato: Contrato,
    @Embedded(prefix = "job_")
    val job: Job
)
