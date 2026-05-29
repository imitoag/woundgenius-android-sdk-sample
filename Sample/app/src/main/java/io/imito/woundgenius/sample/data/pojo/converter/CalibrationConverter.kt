package io.imito.woundgenius.sample.data.pojo.converter

import androidx.room.TypeConverter
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import com.google.gson.Gson

class CalibrationConverter {

    @TypeConverter
    fun fromCalibration(value: MediaModel.Metadata.MeasurementData.Calibration?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCalibration(value: String): MediaModel.Metadata.MeasurementData.Calibration? {
        return Gson().fromJson<MediaModel.Metadata.MeasurementData.Calibration>(
            value,
            MediaModel.Metadata.MeasurementData.Calibration::class.java
        )
    }
}