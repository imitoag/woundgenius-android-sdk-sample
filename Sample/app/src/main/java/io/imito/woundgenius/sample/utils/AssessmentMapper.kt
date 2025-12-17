package io.imito.woundgenius.sample.utils

import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sdk.data.pojo.assessment.entity.AssessmentEntity

fun AssessmentEntity.toRoomLocalEntity() =
    SampleAssessmentEntity(
        userId = userId ?: "",
        patientId = patientId ?: "",
        widthCm = widthCm,
        datetime = datetime ?: "",
        media = media,
        areaCmSq = areaCmSq,
        circumferenceCm = circumferenceCm,
        originalImageId = originalImageId,
        lengthCm = lengthCm,
        depthCm = depthCm,
        stomaDocumentation = isStoma
    )
