package io.imito.woundgenius.sample.data.usecase.user

import androidx.annotation.Keep
import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val sampleAppRepo: SampleAppRepo
) : AbsUseCase<String, GetUserIdUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<String> =
        sampleAppRepo.getUserId()

    class Params private constructor(
    ) {
        companion object {
            fun forGetUserId() = Params()
        }
    }
}