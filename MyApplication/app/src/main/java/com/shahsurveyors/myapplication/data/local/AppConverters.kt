package com.shahsurveyors.myapplication.data.local

import androidx.room.TypeConverter
import com.shahsurveyors.myapplication.models.DocType

class AppConverters {
    @TypeConverter
    fun fromDocType(value: DocType): String = value.name

    @TypeConverter
    fun toDocType(value: String): DocType = DocType.valueOf(value)
}
