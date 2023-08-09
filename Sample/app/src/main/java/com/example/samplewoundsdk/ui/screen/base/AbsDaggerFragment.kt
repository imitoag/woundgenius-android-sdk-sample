package com.example.samplewoundsdk.ui.screen.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class AbsDaggerFragment<VM : ViewModel> : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: VM? = null

    abstract fun provideViewModelClass(): KClass<VM>?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()
    }

    private fun initViewModel() {
        AndroidSupportInjection.inject(this)

        provideViewModelClass()?.run {
            viewModel = ViewModelProvider(
                this@AbsDaggerFragment,
                viewModelFactory
            ).get(this.java)
        }
    }
}