package com.example.samplewoundsdk.data.repo

import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.data.pojo.measurement.MeasurementMetadata

import com.example.woundsdk.data.pojo.assessment.AssessmentPhoto
import io.reactivex.Observable
import java.io.File

interface SampleAppAssessmentsRepo {

    fun deleteDraftAssessmentByLocalId(assessmentId: Long): Observable<Unit>

    fun getDraftAssessmentObservable(): Observable<List<SampleAssessmentEntity>>

    fun getDraftAssessmentByLocalId(assessmentId: Long): Observable<SampleAssessmentEntity?>

    fun saveDraftAssessmentToDB(
        assessment: SampleAssessmentEntity
    ): Observable<Long>

    fun saveLicenseKey(key:String):Observable<Unit>

    fun getLicenseKey():Observable<String>
}
