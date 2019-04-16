package com.lenta.bp10.activity.main

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.di.AppComponent
import com.lenta.bp10.di.getAppComponent
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

class MainActivity : CoreMainActivity() {

    val appComponent: AppComponent by lazy {
        getAppComponent()
    }

    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider

    private val mainViewModel: MainViewModel by lazy {
        appComponent.let {
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
        Logg.d { "onNewEnter" }
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
