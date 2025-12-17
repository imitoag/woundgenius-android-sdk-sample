package io.imito.woundgenius.sample.data.pojo.converter

import androidx.room.TypeConverter
import com.google.gson.Gson

class AnnotationConverter {

    @TypeConverter
    fun fromAnnotation(value: Annotation?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toAnnotation(value: String): Annotation? {
        return Gson().fromJson<Annotation>(value, Annotation::class.java)
    }
}