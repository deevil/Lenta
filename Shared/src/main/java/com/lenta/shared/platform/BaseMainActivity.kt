package com.lenta.shared.platform

import android.os.Bundle
import com.lenta.shared.R

abstract class BaseMainActivity : BaseActivity<com.lenta.shared.databinding.ActivityMainBinding>() {

    override fun getLayoutId() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectThis()
    }

    abstract fun injectThis()

}
