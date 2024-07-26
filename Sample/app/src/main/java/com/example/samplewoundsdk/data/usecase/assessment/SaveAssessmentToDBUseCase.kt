package com.example.samplewoundsdk.data.usecase.assessment

import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.samplewoundsdk.data.usecase.base.AbsUseCase
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
