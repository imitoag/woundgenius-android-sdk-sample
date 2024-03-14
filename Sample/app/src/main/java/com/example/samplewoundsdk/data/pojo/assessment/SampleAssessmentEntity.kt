package com.example.samplewoundsdk.data.pojo.assessment

import androidx.room.*
import com.example.samplewoundsdk.data.pojo.media.MediaModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.Serializable

@Entity(tableName = "sample_assessment_entity")
data class SampleAssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0,

    @ColumnInfo(name = "userId")
    var userId: String? = null,

    @ColumnInfo(name = "patientId")
    var patientId: String? = null,

    @ColumnInfo(name = "width_cm")
    var widthCm: Double? = null,

    @ColumnInfo(name = "datetime")
    var datetime: String? = null,

    @Ignore
    var timestamp: Long? = null,

    @Ignore
    var uiDatetime: String? = null,

    @ColumnInfo(name = "media")
    var media: ArrayList<MediaModel>? = null,

    @ColumnInfo(name = "area_cm_sq")
    var areaCmSq: Double? = null,

    @ColumnInfo(name = "wound_id")
    var woundId: String? = null,

    @ColumnInfo(name = "circumference_cm")
    var circumferenceCm: Double? = null,

    @ColumnInfo(name = "original_image_id")
    var originalImageId: String? = null,

    @ColumnInfo(name = "length_cm")
    var lengthCm: Double? = null,

    @ColumnInfo(name = "depth_cm")
    var depthCm: Double? = null,

    @ColumnInfo(name = "created_by_user_id")
    var createdByUserId: String? = null,

    @ColumnInfo(name = "created_by")
    var createdBy: String? = null,

    @ColumnInfo(name = "observationsJson")
    var observationsJson: String? = null,

    @ColumnInfo(name = "isStoma")
    var isStoma: Boolean? = null

    ) : Serializable {

    class Converter {

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
    }
}
