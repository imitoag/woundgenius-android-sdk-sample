package io.imito.woundgenius.sample.data.usecase.user

import io.imito.woundgenius.sample.data.repo.SampleAppRepo
import io.imito.woundgenius.sample.data.usecase.base.AbsUseCase
import io.reactivex.Observable
import javax.inject.Inject

class SaveUserIdUseCase @Inject constructor(
    private val sampleAppRepo: SampleAppRepo
) : AbsUseCase<Unit, SaveUserIdUseCase.Params>() {

    override fun buildUseCaseObservable(params: Params): Observable<Unit> =
        sampleAppRepo.saveUserId(params.userId)

    class Params private constructor(
        val userId: String
    ) {
        companion object {
            fun forSaveUserId(userId: String) = Params(userId)
        }
    }
}