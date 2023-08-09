package com.example.samplewoundsdk.ui.screen.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.woundsdk.data.pojo.ErrorType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import timber.log.Timber

abstract class AbsViewModel(

) : ViewModel() {

    private val _showUnknownError = MutableLiveData<String?>()
    val showUnknownError: LiveData<String?>
        get() = _showUnknownError

    init {
    }

    private val disposable = CompositeDisposable()

    fun add(disposable: Disposable) {
        if (this.disposable.isDisposed) {
            return
        }
        this.disposable.addAll(disposable)
    }

    protected fun handleError(
        throwable: Throwable,
        isShowDefaultDialog: Boolean = true
    ): ErrorType {
        Timber.e(throwable)
        return parseError(throwable).apply {
            when (this) {
                ErrorType.UNKNOWN -> {
                    if (isShowDefaultDialog) {
                        var message = ""
                        if (throwable is HttpException) {
                            val errorBody = throwable.response()?.errorBody()
                            message = if (errorBody == null) {
                                throwable.response()?.raw().toString()
                            } else {
                                "Request: " + throwable.response()?.raw()
                                    .toString() + " \n Response:" + errorBody.string()
                            }
                        }
                        _showUnknownError.value = message
                        _showUnknownError.value = null
                    }
                }
                else -> {}
            }
        }
    }

    private fun parseError(throwable: Throwable): ErrorType {
        return ErrorType.UNKNOWN
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}
