package com.shahsurveyors.myapplication.data.local

import androidx.room.TypeConverter
import com.shahsurveyors.myapplication.models.DocType

class AppConverters {

    @TypeConverter
    fun fromDocType(value: DocType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toDocType(value: String?): DocType? {
        if (value.isNullOrBlank()) {
            return null
        }

        return try {
            DocType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}