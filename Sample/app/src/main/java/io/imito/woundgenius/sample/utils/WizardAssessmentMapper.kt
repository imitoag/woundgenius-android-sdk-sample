package io.imito.woundgenius.sample.utils

import android.content.Context
import io.imito.wizard.api.model.mapper.AssessmentWizardMapper
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import io.imito.woundgenius.sdk.internal.managers.wizard.AssessmentWizardResult
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.SERVER_DATE_TIME_PATTERN
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun AssessmentWizardResult.Success.toSampleAssessmentEntity(
    context: Context,
    userId: String? = null,
    patientId: String? = null,
    woundId: String? = null
): SampleAssessmentEntity {
    val imagePath = measurementResultWrapper?.image
    val convertedMetadata = measurementResultWrapper?.toMediaMetadata()

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
        observationsJson = AssessmentWizardMapper.toJson(formData, context),
        magicAssessment = true,
        stomaDocumentation = false
    )
}
