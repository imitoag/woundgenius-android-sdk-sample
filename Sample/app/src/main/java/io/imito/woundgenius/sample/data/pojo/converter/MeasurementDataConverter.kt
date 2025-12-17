package io.imito.woundgenius.sample.data.pojo.converter

import androidx.room.TypeConverter
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel
import com.google.gson.Gson

class MeasurementDataConverter {

                @TypeConverter
                fun fromMeasurementData(value: MediaModel.Metadata.MeasurementData?): String {
                    return Gson().toJson(value)
                }

                @TypeConverter
                fun toMeasurementData(value: String): MediaModel.Metadata.MeasurementData? {
                    return Gson().fromJson<MediaModel.Metadata.MeasurementData>(value, MediaModel.Metadata.MeasurementData::class.java)
                }
            }