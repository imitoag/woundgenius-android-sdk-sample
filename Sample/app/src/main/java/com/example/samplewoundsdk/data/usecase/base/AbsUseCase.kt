package com.example.samplewoundsdk.data.usecase.base

import com.example.samplewoundsdk.utils.rx.dropBreadcrumb
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class AbsUseCase<T, Params> {

    abstract fun buildUseCaseObservable(params: Params): Observable<T>

    @JvmOverloads
    fun execute(
        params: Params,
        observeScheduler: Scheduler? = AndroidSchedulers.mainThread()
    ): Observable<T> {
        val tObservable = buildUseCaseObservable(params)
        return (if (observeScheduler != null) {
            tObservable.observeOn(observeScheduler)
        } else {
            tObservable
        }).dropBreadcrumb()
    }
}
