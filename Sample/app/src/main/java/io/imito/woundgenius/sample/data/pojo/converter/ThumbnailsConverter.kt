package io.imito.woundgenius.sample.data.pojo.converter

import androidx.room.TypeConverter
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import com.google.gson.Gson

class ThumbnailsConverter {

    @TypeConverter
    fun fromThumbnails(value: MediaModel.Thumbnails?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toThumbnails(value: String): MediaModel.Thumbnails? {
        return Gson().fromJson<MediaModel.Thumbnails>(value, MediaModel.Thumbnails::class.java)
    }
}