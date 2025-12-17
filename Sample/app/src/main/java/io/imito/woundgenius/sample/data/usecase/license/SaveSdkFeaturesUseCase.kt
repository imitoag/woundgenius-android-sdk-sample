package io.imito.woundgenius.sample.data.usecase.license

import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
import io.imito.woundgenius.sdk.di.WoundGeniusSDK
import io.reactivex.Observable
import javax.inject.Inject

class SaveSdkFeaturesUseCase @Inject constructor(
    private val sampleAppRepo: SampleAppRepo
) : AbsUseCase<Unit, SaveSdkFeaturesUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<Unit> =
        sampleAppRepo.saveSdkFeaturesStatus(params.woundGeniusSDK)

    class Params private constructor(
         var woundGeniusSDK :WoundGeniusSDK
    ) {
        companion object {
            fun forSaveSdkFeatures(woundGeniusSDK: WoundGeniusSDK) = Params(woundGeniusSDK)
        }
    }
}