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

        window.decorView.apply {
            // Hide both the navigation bar
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
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
}