package com.lenta.shared.platform.activity

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.utilities.databinding.dataBindingHelpHolder

abstract class CoreActivity<T : ViewDataBinding> : AppCompatActivity() {
    var binding: T? = null
    val coreComponent: CoreComponent by lazy {
        CoreInjectHelper.provideCoreComponent(this.applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreenContent()
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding?.lifecycleOwner = this
        coreComponent.inject(dataBindingHelpHolder)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemUI()
        }
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

    private fun hideSystemUI() {
        val decorView: View = window.decorView
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = newUiOptions
    }

}