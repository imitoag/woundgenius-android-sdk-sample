package io.imito.woundgenius.sample.data.usecase.license

import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
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