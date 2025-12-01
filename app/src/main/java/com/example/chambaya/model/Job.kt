package com.example.chambaya.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.annotations.SerializedName

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: String,

    @SerializedName("type")
    val type: JobType, // Oferta o Demanda

    @SerializedName("provider_name")
    val providerName: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("distance")
    val distance: Double, // en km

    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("category")
    val category: String = ""
)

enum class JobType {
    OFFER,
    DEMAND
}

// Type converter for Room
class JobTypeConverter {
    @TypeConverter
    fun fromJobType(type: JobType): String {
        return type.name
    }

    @TypeConverter
    fun toJobType(value: String): JobType {
        return JobType.valueOf(value)
    }
}

