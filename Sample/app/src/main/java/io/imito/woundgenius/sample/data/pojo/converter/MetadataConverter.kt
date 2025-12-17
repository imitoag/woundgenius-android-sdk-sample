package io.imito.woundgenius.sample.data.pojo.converter

import androidx.room.TypeConverter
import com.google.gson.Gson

class MetadataConverter {

    @TypeConverter
    fun fromMetadata(value: Metadata?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMetadata(value: String): Metadata? {
        return Gson().fromJson<Metadata>(value, Metadata::class.java)
    }
}