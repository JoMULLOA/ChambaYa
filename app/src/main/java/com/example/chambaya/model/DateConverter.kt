package com.example.chambaya.model

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
