package io.imito.woundgenius.sample.data.repo.impl

import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.data.pojo.license.SdkFeatureStatus
import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.managers.SampleDateTimeManager
import io.imito.woundgenius.sample.storage.db.AssessmentRoomDatabase
import io.imito.woundgenius.sdk.internal.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.internal.data.storage.shared.SharedMemory
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
        if (WoundGeniusSDK.getConfiguration().availableModes != sharedMemory.getAvailableModes()) {
            sharedMemory.setAvailableModes(WoundGeniusSDK.getConfiguration().availableModes)
        }

        if (WoundGeniusSDK.getConfiguration().isMultipleOutlinesEnabled != sharedMemory.getIsMultipleOutlinesSupported()) {
            sharedMemory.setIsMultipleOutlinesSupported(WoundGeniusSDK.getConfiguration().isMultipleOutlinesEnabled)
        }

        if (WoundGeniusSDK.getConfiguration().isStomaFlow != sharedMemory.getIsStomaFlowEnabled()) {
            sharedMemory.setIsStomaFlowEnabled(WoundGeniusSDK.getConfiguration().isStomaFlow)
        }

        val newAutoDetectionMode = WoundGeniusSDK.getConfiguration().autoDetectionMode ?: WoundAutoDetectionMode.NONE
        if (newAutoDetectionMode != sharedMemory.getAutoDetectionMode()) {
            sharedMemory.setAutoDetectionMode(newAutoDetectionMode)
        }

        if (WoundGeniusSDK.getConfiguration().maxNumberOfMedia != sharedMemory.getMaxNumberOfMedia()) {
            sharedMemory.setMaxNumberOfMedia(WoundGeniusSDK.getConfiguration().maxNumberOfMedia)
        }

        if (WoundGeniusSDK.getConfiguration().minNumberOfMedia != sharedMemory.getMinNumberOfMedia()) {
            sharedMemory.setMinNumberOfMedia(WoundGeniusSDK.getConfiguration().minNumberOfMedia)
        }

        val newIsLiveDetectionEnabled = WoundGeniusSDK.getConfiguration().isLiveWoundDetectionEnabled ?: false
        if (newIsLiveDetectionEnabled != sharedMemory.getIsLiveDetectionEnabled()) {
            sharedMemory.setIsLiveDetectionEnabled(newIsLiveDetectionEnabled)
        }

        if (WoundGeniusSDK.getConfiguration().isAddFromLocalStorageAvailable != sharedMemory.getIsMediaFromGalleryAllowed()) {
            sharedMemory.setIsMediaFromGalleryAllowed(WoundGeniusSDK.getConfiguration().isAddFromLocalStorageAvailable)
        }

        if (WoundGeniusSDK.getConfiguration().isBodyPartPickerAvailable != sharedMemory.getIsBodyPickerAllowed()) {
            sharedMemory.setIsBodyPickerAllowed(WoundGeniusSDK.getConfiguration().isBodyPartPickerAvailable)
        }

        if (WoundGeniusSDK.getConfiguration().isFrontCameraUsageAllowed != sharedMemory.getIsFrontalCameraSupported()) {
            sharedMemory.setIsFrontalCameraSupported(WoundGeniusSDK.getConfiguration().isFrontCameraUsageAllowed)

        }

        if (WoundGeniusSDK.getConfiguration().isLandscapeSupported != sharedMemory.getIsLandScapeSupported()) {
            sharedMemory.setIsLandScapeSupported(WoundGeniusSDK.getConfiguration().isLandscapeSupported)
        }

        if (WoundGeniusSDK.getConfiguration().isMeasurementLineEnabled != sharedMemory.getIsMeasurementLineEnabled()) {
            sharedMemory.setIsMeasurementLineEnabled(WoundGeniusSDK.getConfiguration().isMeasurementLineEnabled)
        }

        if (WoundGeniusSDK.getConfiguration().isSingleAreaEnabled != sharedMemory.getIsSingleAreaEnabled()) {
            sharedMemory.setIsSingleAreaEnabled(WoundGeniusSDK.getConfiguration().isSingleAreaEnabled)
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
            minNumberOfMedia = sharedMemory.getMinNumberOfMedia(),
            isLiveDetectionEnabled = sharedMemory.getIsLiveDetectionEnabled(),
            isMediaFromGalleryAllowed = sharedMemory.getIsMediaFromGalleryAllowed(),
            isBodyPickerAllowed = sharedMemory.getIsBodyPickerAllowed(),
            isFrontalCameraSupported = sharedMemory.getIsFrontalCameraSupported(),
            isMeasurementLineEnabled = sharedMemory.getIsMeasurementLineEnabled(),
            isSingleAreaEnabled = sharedMemory.getIsSingleAreaEnabled(),
            isLandScapeSupported = sharedMemory.getIsLandScapeSupported()
        )
    }.subscribeOn(Schedulers.io())


    override fun deleteDraftAssessmentByLocalId(assessmentId: Long): Observable<Unit> =
        Observable.fromCallable {
            assessmentDB.mediaDao().getAssessmentById(assessmentId)?.let {
                assessmentDB.mediaDao().deleteAssessment(it).apply {
                    it.media?.map {
                        val file = it.image?.let { it1 -> File(it1) }
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
