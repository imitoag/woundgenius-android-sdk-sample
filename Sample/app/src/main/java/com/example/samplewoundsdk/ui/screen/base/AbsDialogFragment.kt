package com.example.samplewoundsdk.ui.screen.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class AbsDialogFragment<VM : AbsViewModel> : AbsDaggerDialogFragment<VM>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(provideLayoutId(), container, false)
    }

    abstract fun provideLayoutId(): Int
}