package io.imito.woundgenius.sample.data.usecase.assessment

import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetAssessmentsUseCase @Inject constructor(
    private val assessmentsRepo: SampleAppRepo
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
