package com.example.samplewoundsdk.data.usecase.license

import com.example.samplewoundsdk.data.repo.SampleAppAssessmentsRepo
import com.example.woundsdk.data.repo.MeasurementRepo
import com.example.woundsdk.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetLicenseKeyUseCase @Inject constructor(
    private val sampleAppAssessmentsRepo: SampleAppAssessmentsRepo
) : AbsUseCase<String, GetLicenseKeyUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<String> =
        sampleAppAssessmentsRepo.getLicenseKey()

    class Params private constructor(
    ) {
        companion object {
            fun forGetLicenseKey() = Params()
        }
    }
}