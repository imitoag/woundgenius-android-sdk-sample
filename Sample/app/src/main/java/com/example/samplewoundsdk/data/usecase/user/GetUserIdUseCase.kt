package com.example.samplewoundsdk.data.usecase.user

import com.example.samplewoundsdk.data.repo.SampleAppRepo
import com.example.woundsdk.data.usecase.base.AbsUseCase
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