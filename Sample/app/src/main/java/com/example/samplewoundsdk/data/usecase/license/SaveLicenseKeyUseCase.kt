package com.example.samplewoundsdk.data.usecase.license

import com.example.samplewoundsdk.data.repo.SampleAppAssessmentsRepo
import com.example.woundsdk.data.repo.MeasurementRepo
import com.example.woundsdk.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class SaveLicenseKeyUseCase @Inject constructor(
    private val sampleAppAssessmentsRepo: SampleAppAssessmentsRepo
) : AbsUseCase<Unit, SaveLicenseKeyUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<Unit> =
        sampleAppAssessmentsRepo.saveLicenseKey(params.key)

    class Params private constructor(
        val key: String
    ) {
        companion object {
            fun forSaveLicenseKey(key: String) = Params(key)
        }
    }
}