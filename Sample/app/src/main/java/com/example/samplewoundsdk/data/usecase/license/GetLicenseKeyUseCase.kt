package com.example.samplewoundsdk.data.usecase.license

import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.woundsdk.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetLicenseKeyUseCase @Inject constructor(
    private val sampleAppRepo: SampleAppRepo
) : AbsUseCase<String, GetLicenseKeyUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<String> =
        sampleAppRepo.getLicenseKey()

    class Params private constructor(
    ) {
        companion object {
            fun forGetLicenseKey() = Params()
        }
    }
}