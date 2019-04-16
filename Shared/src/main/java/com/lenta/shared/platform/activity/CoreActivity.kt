package com.lenta.shared.platform.activity

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class CoreActivity<T : ViewDataBinding> : AppCompatActivity() {
    var binding: T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreenContent()
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding?.lifecycleOwner = this
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    private fun setupFullScreenContent() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}