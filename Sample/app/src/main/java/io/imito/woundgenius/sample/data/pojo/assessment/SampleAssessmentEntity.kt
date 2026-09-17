package io.imito.woundgenius.sample.data.pojo.assessment

import android.os.Parcelable
import androidx.room.*
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import kotlinx.parcelize.Parcelize

@Parcelize
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

    @ColumnInfo(name = "stomaDocumentation")
    var stomaDocumentation: Boolean? = null,

    @ColumnInfo(name = "magicAssessment")
    var magicAssessment: Boolean? = null

    ) : Parcelable
