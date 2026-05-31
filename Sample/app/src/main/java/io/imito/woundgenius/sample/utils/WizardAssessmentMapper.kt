package io.imito.woundgenius.sample.utils

import io.imito.wizard.api.model.WizardMediaModel
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.utils.toMediaMetadata
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD
import io.imito.woundgenius.sdk.internal.managers.wizard.WizardAssessmentResult
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.SERVER_DATE_TIME_PATTERN
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun WizardAssessmentResult.Success.toSampleAssessmentEntity(
    userId: String? = null,
    patientId: String? = null,
    woundId: String? = null
): SampleAssessmentEntity {
    val imagePath = image?.absolutePath
    val convertedMetadata = measurements

    val media: ArrayList<MediaModel>? = if (imagePath != null) {
        val mediaModel = MediaModel(
            id = 0,
            image = imagePath,
            metadata = convertedMetadata,
            originalPictureSize = getImageDimensions(imagePath),
            measurementMethod = if (convertedMetadata?.measurementData?.annotationList?.isNotEmpty() == true) {
                ImitoCameraMode.MANUAL_MEASURE_MODE
            } else {
                ImitoCameraMode.PHOTO_MODE
            }
        )
        arrayListOf(mediaModel)
    } else {
        null
    }

    return SampleAssessmentEntity(
        userId = userId,
        patientId = patientId,
        woundId = woundId,
        datetime = SimpleDateFormat(SERVER_DATE_TIME_PATTERN, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(System.currentTimeMillis()),
        media = media,
        observationsJson = json,
        magicAssessment = true,
        stomaDocumentation = false
    )
}

private fun WizardMediaModel.Metadata.toMediaMetadata(): MediaModel.Metadata =
    MediaModel.Metadata(
        rotation = rotation,
        measurementData = measurementData?.toMediaMeasurementData()
    )

private fun WizardMediaModel.Metadata.MeasurementData.toMediaMeasurementData():
        MediaModel.Metadata.MeasurementData =
    MediaModel.Metadata.MeasurementData(
        annotationList = annotationList?.map { it.toMediaAnnotation() },
        mlAnnotationList = mlAnnotationList?.map { it?.toMediaAnnotation() }
            ?.let { ArrayList(it) },
        calibration = calibration?.let {
            MediaModel.Metadata.MeasurementData.Calibration(
                unit = it.unit,
                unitPerPixel = it.unitPerPixel
            )
        }
    )

private fun WizardMediaModel.Metadata.MeasurementData.Annotation.toMediaAnnotation():
        MediaModel.Metadata.MeasurementData.Annotation =
    MediaModel.Metadata.MeasurementData.Annotation(
        area = area,
        circumference = circumference,
        type = type,
        prefix = prefix,
        length = length,
        width = width,
        depth = depthInCM,
        cluster = cluster,
        order = order,
        id = id,
        points = points?.map { PointD((it.pointX ?: 0).toDouble(), (it.pointY ?: 0).toDouble()) },
        widthPointA = widthPointA?.toMediaPointDouble(),
        widthPointB = widthPointB?.toMediaPointDouble(),
        lengthPointA = lengthPointA?.toMediaPointDouble(),
        lengthPointB = lengthPointB?.toMediaPointDouble(),
        pointA = pointA?.let {
            MediaModel.Metadata.MeasurementData.Annotation.PointA(it.pointX, it.pointY)
        },
        pointB = pointB?.toMediaPointDouble()
    )

private fun WizardMediaModel.Metadata.MeasurementData.Annotation.PointDouble.toMediaPointDouble():
        MediaModel.Metadata.MeasurementData.Annotation.PointDouble =
    MediaModel.Metadata.MeasurementData.Annotation.PointDouble(pointX, pointY)
