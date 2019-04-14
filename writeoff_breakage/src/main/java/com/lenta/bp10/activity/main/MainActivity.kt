package com.lenta.bp10.activity.main

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.di.getAppComponent
import com.lenta.shared.features.login.LoginFragment
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.activity.main_activity.BaseMainActivity
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import javax.inject.Inject

class MainActivity : BaseMainActivity() {


    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider

    private val mainViewModel: MainViewModel by lazy {
        getAppComponent().let {
            it.inject(this)
            foregroundActivityProvider.setActivity(this)

            val mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
            it.inject(mainViewModel)
            return@lazy mainViewModel
        }
    }

    override fun getBottomToolBarUIModel(): BottomToolbarUiModel {
        return mainViewModel.bottomToolbarUiModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding?.vm = mainViewModel
    }

    override fun onNewEnter() {
        mainViewModel.onNewEnter()
    }

    override fun onResume() {
        super.onResume()
        foregroundActivityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        foregroundActivityProvider.clear()
    }

}
