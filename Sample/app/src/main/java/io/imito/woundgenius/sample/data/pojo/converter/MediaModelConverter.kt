package io.imito.woundgenius.sample.data.pojo.converter

import androidx.room.TypeConverter
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MediaModelConverter {

    @TypeConverter
    fun fromMetadata(value: ArrayList<MediaModel>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMetadata(value: String): ArrayList<MediaModel>? {
        return Gson().fromJson<ArrayList<MediaModel>?>(
            value,
            object : TypeToken<ArrayList<MediaModel>?>() {}.type
        )
    }


    @TypeConverter
    fun fromMediaModel(value: MediaModel?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMediaModel(value: String?): MediaModel? {
        return try {
            Gson().fromJson(value, MediaModel::class.java)
        } catch (e: Exception) {
            null
        }
    }
}