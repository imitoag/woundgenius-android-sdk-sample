package com.example.samplewoundsdk.utils

import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.pojo.media.MediaModel
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.data.pojo.measurement.ImageResolution

fun AssessmentEntity.toRoomLocalEntity() =
    SampleAssessmentEntity(
        userId = userId ?: "",
        patientId = patientId ?: "",
        widthCm = widthCm,
        datetime = datetime ?: "",
        media = ArrayList(media?.mapIndexed { index, mediaItem ->
            MediaModel(
                metadata = MediaModel.Metadata(
                    measurementData = MediaModel.Metadata.MeasurementData(
                        annotationList = mediaItem.metadata?.measurementData?.annotationList?.map { annotation ->
                            MediaModel.Metadata.MeasurementData.Annotation(
                                area = annotation?.area,
                                circumference = annotation?.circumference,
                                type = com.example.woundsdk.data.pojo.entity.MediaModel.Metadata.MeasurementData.Annotation.ANNOTATION_OUTLINE_TYPE,
                                points = annotation?.points?.map {
                                    MediaModel.Metadata.MeasurementData.Annotation.PointsItem(
                                        it.pointX,
                                        it.pointY
                                    )
                                },
                                widthPointA = MediaModel.Metadata.MeasurementData.Annotation.PointDouble(
                                    annotation?.widthPointA?.pointX,
                                    annotation?.widthPointA?.pointY
                                ),
                                widthPointB = MediaModel.Metadata.MeasurementData.Annotation.PointDouble(
                                    annotation?.widthPointB?.pointX,
                                    annotation?.widthPointB?.pointY
                                ),
                                lengthPointA = MediaModel.Metadata.MeasurementData.Annotation.PointDouble(
                                    annotation?.lengthPointA?.pointX,
                                    annotation?.lengthPointA?.pointY
                                ),
                                lengthPointB = MediaModel.Metadata.MeasurementData.Annotation.PointDouble(
                                    annotation?.lengthPointB?.pointX,
                                    annotation?.lengthPointB?.pointY
                                ),
                                length = annotation?.length,
                                width = annotation?.width,
                                depth = annotation?.depth
                            )
                        },
                        calibration = MediaModel.Metadata.MeasurementData.Calibration()
                    )
                ),
                imagePath = mediaItem.image,
                originalPictureSize = ImageResolution(mediaItem.originalPictureSize?.width ?:0,mediaItem.originalPictureSize?.height ?:0),
                measurementMethod = mediaItem.measurementMethod,
                bodyPart = mediaItem.bodyPart
            )
        } ?: emptyList()),
        areaCmSq = areaCmSq,
        circumferenceCm = circumferenceCm,
        originalImageId = originalImageId,
        lengthCm = lengthCm,
        depthCm = depthCm,
        isStoma = isStoma
    )
