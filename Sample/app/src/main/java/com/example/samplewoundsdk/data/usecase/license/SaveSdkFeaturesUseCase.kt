package com.example.samplewoundsdk.data.usecase.license

import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.woundsdk.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class SaveSdkFeaturesUseCase @Inject constructor(
    private val sampleAppRepo: SampleAppRepo
) : AbsUseCase<Unit, SaveSdkFeaturesUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<Unit> =
        sampleAppRepo.saveSdkFeaturesStatus()

    class Params private constructor() {
        companion object {
            fun forSaveSdkFeatures() = Params()
        }
    }
}