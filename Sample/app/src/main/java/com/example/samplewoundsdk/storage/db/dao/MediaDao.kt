package com.example.samplewoundsdk.storage.db.dao

import androidx.room.*
import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import io.reactivex.Observable

@Dao
interface MediaDao {

    @Query("SELECT * FROM sample_assessment_entity")
    fun getAssessmentsObservable(): Observable<List<SampleAssessmentEntity>>

    @Transaction
    @Query("SELECT * FROM sample_assessment_entity")
    fun getAssessments(): List<SampleAssessmentEntity>

    @Transaction
    @Query("SELECT * FROM sample_assessment_entity WHERE id == :assessmentId")
    fun getAssessmentWithMediaByIdQuery(assessmentId: Long): SampleAssessmentEntity

    @Query("DELETE FROM sample_assessment_entity")
    fun clearDatabase()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssessmentQuery(assessment: SampleAssessmentEntity): Long

    @Delete
    fun deleteAssessmentQuery(assessment: SampleAssessmentEntity)

    fun insertAssessment(assessment: SampleAssessmentEntity): Long {
        val assessmentId = insertAssessmentQuery(assessment)
        return assessmentId
    }

    fun getAssessmentListObservable(): Observable<List<SampleAssessmentEntity>> {
        val assessmentWithMediaList = getAssessmentsObservable()
        return assessmentWithMediaList
    }

    fun getAssessmentList(): List<SampleAssessmentEntity> {
        val assessmentWithMediaList = getAssessments()
        return assessmentWithMediaList
    }

    fun getClearDataBase() {
        clearDatabase()
    }

    fun deleteAssessment(assessment: SampleAssessmentEntity) {
        deleteAssessmentQuery(assessment)
    }

    fun getAssessmentById(id: Long): SampleAssessmentEntity? {
        val assessment = getAssessmentWithMediaByIdQuery(id)
        return assessment
    }
}
