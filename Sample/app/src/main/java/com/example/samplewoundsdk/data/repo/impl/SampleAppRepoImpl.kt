package com.example.samplewoundsdk.data.repo.impl

import android.util.Log
import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.pojo.license.SdkFeatureStatus
import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.samplewoundsdk.managers.SampleDateTimeManager
import com.example.samplewoundsdk.storage.db.AssessmentRoomDatabase
import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.di.WoundGeniusSDK
import com.example.woundsdk.storage.shared.SharedMemory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File


class SampleAppRepoImpl(
    private val assessmentDB: AssessmentRoomDatabase,
    private val dateTimeManager: SampleDateTimeManager,
    private val sharedMemory: SharedMemory
) : SampleAppRepo {

    override fun saveDraftAssessmentToDB(
        assessment: SampleAssessmentEntity
    ): Observable<Long> =
        Observable.fromCallable {
            assessmentDB.mediaDao().insertAssessment(assessment)
        }.subscribeOn(Schedulers.io())

    override fun saveLicenseKey(key: String): Observable<Unit> = Observable.fromCallable {
        sharedMemory.saveLicenseKey(key)
    }.subscribeOn(Schedulers.io())

    override fun saveUserId(userId: String): Observable<Unit> = Observable.fromCallable {
        sharedMemory.saveUserId(userId)
    }.subscribeOn(Schedulers.io())

    override fun getLicenseKey(): Observable<String> = Observable.fromCallable {
        sharedMemory.getLicenseKey()
    }.subscribeOn(Schedulers.io())

    override fun getUserId(): Observable<String> = Observable.fromCallable {
        sharedMemory.getUserId()
    }.subscribeOn(Schedulers.io())

    override fun saveSdkFeaturesStatus(woundGeniusSDK: WoundGeniusSDK): Observable<Unit> = Observable.fromCallable {
        if (woundGeniusSDK.getAvailableModes() != sharedMemory.getAvailableModes()) {
            sharedMemory.setAvailableModes(woundGeniusSDK.getAvailableModes())
        }

        if (woundGeniusSDK.getIsMultipleOutlinesEnabled() != sharedMemory.getIsMultipleOutlinesSupported()) {
            sharedMemory.setIsMultipleOutlinesSupported(woundGeniusSDK.getIsMultipleOutlinesEnabled())
        }

        if (woundGeniusSDK.getIsStomaFlow() != sharedMemory.getIsStomaFlowEnabled()) {
            sharedMemory.setIsStomaFlowEnabled(woundGeniusSDK.getIsStomaFlow())
        }

        val newAutoDetectionMode = woundGeniusSDK.getAutoDetectionMod() ?: WoundAutoDetectionMode.NONE
        if (newAutoDetectionMode != sharedMemory.getAutoDetectionMode()) {
            sharedMemory.setAutoDetectionMode(newAutoDetectionMode)
        }

        if (woundGeniusSDK.getMaxNumberOfMedia() != sharedMemory.getMaxNumberOfMedia()) {
            sharedMemory.setMaxNumberOfMedia(woundGeniusSDK.getMaxNumberOfMedia())
        }

        val newIsLiveDetectionEnabled = woundGeniusSDK.getIsLiveDetectionEnabled() ?: false
        if (newIsLiveDetectionEnabled != sharedMemory.getIsLiveDetectionEnabled()) {
            sharedMemory.setIsLiveDetectionEnabled(newIsLiveDetectionEnabled)
        }

        if (woundGeniusSDK.getIsAddFromLocalStorageAvailable() != sharedMemory.getIsMediaFromGalleryAllowed()) {
            sharedMemory.setIsMediaFromGalleryAllowed(woundGeniusSDK.getIsAddFromLocalStorageAvailable())
        }

        if (woundGeniusSDK.getIsAddBodyPickerAvailable() != sharedMemory.getIsBodyPickerAllowed()) {
            sharedMemory.setIsBodyPickerAllowed(woundGeniusSDK.getIsAddBodyPickerAvailable())
        }

        if (woundGeniusSDK.getIsFrontCameraUsageAllowed() != sharedMemory.getIsFrontalCameraSupported()) {
            sharedMemory.setIsFrontalCameraSupported(woundGeniusSDK.getIsFrontCameraUsageAllowed())

        }

        if (woundGeniusSDK.getIsLandscapeSupported() != sharedMemory.getIsLandScapeSupported()) {
            sharedMemory.setIsLandScapeSupported(woundGeniusSDK.getIsLandscapeSupported())
        }

        Unit
    }.subscribeOn(Schedulers.io())

    override fun getSdkFeaturesStatus(): Observable<SdkFeatureStatus> = Observable.fromCallable {
        SdkFeatureStatus(
            availableModes = sharedMemory.getAvailableModes(),
            isMultipleOutlinesSupported = sharedMemory.getIsMultipleOutlinesSupported(),
            isStomaFlowEnable = sharedMemory.getIsStomaFlowEnabled(),
            autoDetectionMode = sharedMemory.getAutoDetectionMode(),
            maxNumberOfMedia = sharedMemory.getMaxNumberOfMedia(),
            isLiveDetectionEnabled = sharedMemory.getIsLiveDetectionEnabled(),
            isMediaFromGalleryAllowed = sharedMemory.getIsMediaFromGalleryAllowed(),
            isBodyPickerAllowed = sharedMemory.getIsBodyPickerAllowed(),
            isFrontalCameraSupported = sharedMemory.getIsFrontalCameraSupported(),
            isLandScapeSupported = sharedMemory.getIsLandScapeSupported()
        )
    }.subscribeOn(Schedulers.io())


    override fun deleteDraftAssessmentByLocalId(assessmentId: Long): Observable<Unit> =
        Observable.fromCallable {
            assessmentDB.mediaDao().getAssessmentById(assessmentId)?.let {
                assessmentDB.mediaDao().deleteAssessment(it).apply {
                    it.media?.map {
                        val file = it.imagePath?.let { it1 -> File(it1) }
                        if (file?.exists() == true) {
                            file.deleteRecursively()
                        }
                    }
                }
            }
        }.subscribeOn(Schedulers.io())

    override fun getDraftAssessmentObservable(): Observable<List<SampleAssessmentEntity>> {
        return assessmentDB.mediaDao().getAssessmentListObservable().map { assessmentList ->
            assessmentList.map {
                it.timestamp = it.datetime?.let {
                    dateTimeManager.convertServerDateToTimestamp(
                        it
                    )
                }
                it.uiDatetime = it.datetime?.let {
                    dateTimeManager.convertServerDateTimeToChangeableDate(
                        it
                    )
                }
            }
            assessmentList
        }
    }

    override fun getDraftAssessmentByLocalId(assessmentId: Long): Observable<SampleAssessmentEntity?> =
        Observable.fromCallable {
            assessmentDB.mediaDao().getAssessmentById(assessmentId)
        }.subscribeOn(Schedulers.io())

}
