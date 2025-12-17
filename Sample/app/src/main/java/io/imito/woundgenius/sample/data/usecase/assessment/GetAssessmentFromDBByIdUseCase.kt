package io.imito.woundgenius.sample.data.usecase.assessment

import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
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
