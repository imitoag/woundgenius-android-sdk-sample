package com.example.samplewoundsdk.data.usecase.license

import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.woundsdk.data.usecase.base.AbsUseCase
import com.example.woundsdk.di.WoundGeniusSDK
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