package io.imito.woundgenius.sample.data.usecase.license

import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class SaveLicenseKeyUseCase @Inject constructor(
    private val sampleAppRepo: SampleAppRepo
) : AbsUseCase<Unit, SaveLicenseKeyUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<Unit> =
        sampleAppRepo.saveLicenseKey(params.key)

    class Params private constructor(
        val key: String
    ) {
        companion object {
            fun forSaveLicenseKey(key: String) = Params(key)
        }
    }
}