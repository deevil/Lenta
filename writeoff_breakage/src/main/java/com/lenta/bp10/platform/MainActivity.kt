package com.lenta.bp10.platform

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.di.getAppComponent
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.main_activity.BaseMainActivity
import com.lenta.shared.platform.main_activity.MainViewModel
import com.lenta.shared.platform.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logger
import javax.inject.Inject

class MainActivity : BaseMainActivity() {

    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider
    @Inject
    lateinit var screenNavigator: IScreenNavigator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAppComponent().inject(this)

         Logger.d { "screenNavigator hash : ${screenNavigator.hashCode()}" }

        binding?.vm = ViewModelProviders.of(this).get(MainViewModel::class.java)

        binding?.layoutTopToolbar?.bExit?.setOnClickListener {
            binding?.vm?.topToolbarUiModel?.value = TopToolbarUiModel(title = System.currentTimeMillis().toString(), description = "Карточка задания")
        }

    }

    override fun onNewEnter() {

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
