package io.imito.woundgenius.sample.data.usecase.license

import io.imito.woundgenius.sample.data.pojo.license.SdkFeatureStatus
import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
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