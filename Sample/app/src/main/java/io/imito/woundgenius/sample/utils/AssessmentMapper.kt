package io.imito.woundgenius.sample.utils

import android.graphics.BitmapFactory
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import io.imito.woundgenius.sdk.internal.data.pojo.image.ImageResolution
import io.imito.woundgenius.sdk.internal.data.pojo.cluster.ImitoOutlineCluster
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.MeasuredOutline
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.MeasurementResult
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_MEASUREMENT_LINE_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_OUTLINE_TYPE
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.SERVER_DATE_TIME_PATTERN
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.supportedVideoExtensions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


fun MeasurementResult.toSampleAssessmentEntity(
    userId: String? = null,
    patientId: String? = null,
    woundId: String? = null
): SampleAssessmentEntity {


    val annotationList = this.outlines.map { it.toOldAnnotation() }
    val mlAnnotationList = ArrayList(this.mlOutlines.map { it.toOldAnnotation() })


    val oldMetadata = MediaModel.Metadata(
        measurementData = MediaModel.Metadata.MeasurementData(
            annotationList = annotationList,
            mlAnnotationList = mlAnnotationList,
            calibration = MediaModel.Metadata.MeasurementData.Calibration(
                unit = "CM",
                unitPerPixel = this.lengthOfOneCmInPixels
            )
        )
    )


    val mediaModel = MediaModel(
        id = 0,
        image = this.image,
        metadata = oldMetadata,
        originalPictureSize = getImageDimensions(this.image),
        measurementMethod = if (supportedVideoExtensions.any { extension ->
                this.image.lowercase().endsWith(extension)
            }) {
            ImitoCameraMode.VIDEO_MODE
        } else {
            if (areaOfQRCodeInPixels != null) {
                ImitoCameraMode.MARKER_DETECT_MODE
            } else if (outlines.isNotEmpty()) {
                ImitoCameraMode.MANUAL_MEASURE_MODE
            } else {
                ImitoCameraMode.PHOTO_MODE
            }
        }
    )


    val mainOutline = outlines.firstOrNull()
    val isStoma = outlines.find { it.cluster == ImitoOutlineCluster.STOMA } != null

    return SampleAssessmentEntity(
        userId = userId,
        patientId = patientId,
        woundId = woundId,
        datetime = SimpleDateFormat(SERVER_DATE_TIME_PATTERN, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(System.currentTimeMillis()),
        media = arrayListOf(mediaModel),


        areaCmSq = mainOutline?.areaInCM?.div(10),
        lengthCm = mainOutline?.lengthInCM,
        widthCm = mainOutline?.widthInCM,
        circumferenceCm = mainOutline?.circumferenceInCM,
        depthCm = mainOutline?.depthCM?.toDouble(),

        stomaDocumentation = isStoma
    )
}

fun getImageDimensions(filePath: String): ImageResolution {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    BitmapFactory.decodeFile(filePath, options)

    val width = options.outWidth
    val height = options.outHeight

    return if (width != -1 && height != -1) {
        ImageResolution(width, height)
    } else {
        ImageResolution(0, 0)
    }
}


private fun MeasuredOutline.toOldAnnotation(): MediaModel.Metadata.MeasurementData.Annotation {
    return MediaModel.Metadata.MeasurementData.Annotation(
        id = this.id,
        order = this.order,
        area = this.areaInCM?.div(10),
        length = this.lengthInCM,
        width = this.widthInCM,
        circumference = this.circumferenceInCM,
        depth = this.depthCM?.times(10)?.toDouble(),
        cluster = this.cluster.name,
        type = when (this.cluster) {
            ImitoOutlineCluster.WOUND -> ANNOTATION_OUTLINE_TYPE
            ImitoOutlineCluster.STOMA -> ANNOTATION_OUTLINE_TYPE
            ImitoOutlineCluster.LINE -> ANNOTATION_MEASUREMENT_LINE_TYPE
            else -> ANNOTATION_OUTLINE_TYPE
        },


        points = this.points.map {
            PointD(it.x.toInt(), it.y.toInt())
        },


        lengthPointA = this.lengthStartPointPixels?.toPointDouble(),
        lengthPointB = this.lengthEndPointPixels?.toPointDouble(),
        widthPointA = this.widthStartPointPixels?.toPointDouble(),
        widthPointB = this.widthEndPointPixels?.toPointDouble()
    )
}

private fun PointD.toPointDouble() =
    MediaModel.Metadata.MeasurementData.Annotation.PointDouble(this.x, this.y)
