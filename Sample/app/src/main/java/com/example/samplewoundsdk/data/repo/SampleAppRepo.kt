package com.example.samplewoundsdk.data.repo

import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.pojo.license.SdkFeatureStatus
import com.example.woundsdk.di.WoundGeniusSDK

import io.reactivex.Observable

interface SampleAppRepo {

    fun deleteDraftAssessmentByLocalId(assessmentId: Long): Observable<Unit>

    fun getDraftAssessmentObservable(): Observable<List<SampleAssessmentEntity>>

    fun getDraftAssessmentByLocalId(assessmentId: Long): Observable<SampleAssessmentEntity?>

    fun saveDraftAssessmentToDB(
        assessment: SampleAssessmentEntity
    ): Observable<Long>

    fun saveLicenseKey(key:String):Observable<Unit>
    fun saveUserId(userId:String):Observable<Unit>

    fun getLicenseKey():Observable<String>
    fun getUserId():Observable<String>

    fun saveSdkFeaturesStatus(woundGeniusSDK: WoundGeniusSDK):Observable<Unit>
    fun getSdkFeaturesStatus():Observable<SdkFeatureStatus>
}
