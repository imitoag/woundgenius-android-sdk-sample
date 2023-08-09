package com.example.samplewoundsdk.data.usecase.assessment

import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.repo.SampleAppAssessmentsRepo
import com.example.samplewoundsdk.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetAssessmentsUseCase @Inject constructor(
    private val assessmentsRepo: SampleAppAssessmentsRepo
) : AbsUseCase<List<SampleAssessmentEntity>, GetAssessmentsUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<List<SampleAssessmentEntity>> =
        assessmentsRepo.getDraftAssessmentObservable()

    class Params private constructor(
    ) {
        companion object {
            fun forGetDraftAssessments() = Params()
        }
    }
}
