package com.example.samplewoundsdk.data.usecase.assessment

import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.samplewoundsdk.data.usecase.base.AbsUseCase
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
