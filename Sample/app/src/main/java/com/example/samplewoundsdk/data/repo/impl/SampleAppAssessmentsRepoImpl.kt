package com.example.samplewoundsdk.data.repo.impl

import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.repo.SampleAppAssessmentsRepo
import com.example.samplewoundsdk.managers.SampleDateTimeManager
import com.example.samplewoundsdk.storage.db.AssessmentRoomDatabase
import com.example.woundsdk.storage.shared.SharedMemory
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File


class SampleAppAssessmentsRepoImpl(
    private val assessmentDB: AssessmentRoomDatabase,
    private val dateTimeManager: SampleDateTimeManager,
    private val sharedMemory: SharedMemory
) : SampleAppAssessmentsRepo {

    override fun saveDraftAssessmentToDB(
        assessment: SampleAssessmentEntity
    ): Observable<Long> =
        Observable.fromCallable {
            assessmentDB.mediaDao().insertAssessment(assessment)
        }.subscribeOn(Schedulers.io())

    override fun saveLicenseKey(key: String): Observable<Unit> = Observable.fromCallable {
        sharedMemory.saveLicenseKey(key)
    }.subscribeOn(Schedulers.io())

    override fun getLicenseKey(): Observable<String> = Observable.fromCallable {
        sharedMemory.getLicenseKey()
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
