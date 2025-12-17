package io.imito.woundgenius.sample.data.usecase.assessment

import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class SaveAssessmentToDBUseCase @Inject constructor(
    private val assessmentsRepo: SampleAppRepo
) : AbsUseCase<Long, SaveAssessmentToDBUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<Long> =
        assessmentsRepo.saveDraftAssessmentToDB(
            params.assessment
        )

    class Params private constructor(
        val assessment: SampleAssessmentEntity
    ) {
        companion object {
            fun forSaveAssessmentToDB(
                assessment: SampleAssessmentEntity
            ) = Params(
                assessment
            )
        }
    }
}
