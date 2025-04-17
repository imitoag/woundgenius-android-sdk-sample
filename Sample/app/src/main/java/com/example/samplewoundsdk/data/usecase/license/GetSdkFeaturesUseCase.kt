package com.example.samplewoundsdk.data.usecase.license

import com.example.samplewoundsdk.data.pojo.license.SdkFeatureStatus
import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.woundsdk.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetSdkFeaturesUseCase @Inject constructor(
    private val sampleAppRepo: SampleAppRepo
) : AbsUseCase<SdkFeatureStatus, GetSdkFeaturesUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<SdkFeatureStatus> =
        sampleAppRepo.getSdkFeaturesStatus()

    class Params private constructor() {
        companion object {
            fun forGetSdkFeatures() = Params()
        }
    }
}