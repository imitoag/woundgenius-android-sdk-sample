package com.example.samplewoundsdk.data.usecase.assessment

import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.samplewoundsdk.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetAssessmentFromDBByIdUseCase @Inject constructor(
    private val assessmentsRepo: SampleAppRepo
) : AbsUseCase<SampleAssessmentEntity?, GetAssessmentFromDBByIdUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<SampleAssessmentEntity?> =
        assessmentsRepo.getDraftAssessmentByLocalId(params.assessmentId)

    class Params private constructor(
        val assessmentId: Long
    ) {
        companion object {
            fun forGetDraftAssessmentById(assessmentId: Long) = Params(assessmentId)
        }
    }
}
