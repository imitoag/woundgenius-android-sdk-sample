package com.example.samplewoundsdk.ui.screen.base

import android.os.Bundle


abstract class AbsActivity<VM : AbsViewModel> : AbsDaggerActivity<VM>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        provideLayoutId()?.let { setContentView(it) }

        viewModel?.apply {
            showUnknownError.observe(this@AbsActivity) {
            }
        }
    }

    abstract fun provideLayoutId(): Int?

    abstract fun initListeners()

    override fun onStart() {
        super.onStart()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    protected abstract fun onKeyboardOpen()
    protected abstract fun onKeyboardClose()

}
