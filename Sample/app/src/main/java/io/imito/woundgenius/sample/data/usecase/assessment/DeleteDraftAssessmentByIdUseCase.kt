package io.imito.woundgenius.sample.data.usecase.assessment

import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class DeleteDraftAssessmentByIdUseCase @Inject constructor(
    private val assessmentsRepo: SampleAppRepo
) : AbsUseCase<Unit, DeleteDraftAssessmentByIdUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<Unit> =
        assessmentsRepo.deleteDraftAssessmentByLocalId(params.assessmentId)

    class Params private constructor(
        val assessmentId: Long
    ) {
        companion object {
            fun forDeleteAssessment(assessmentId: Long) = Params(assessmentId)
        }
    }
}
