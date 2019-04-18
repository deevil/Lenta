package com.lenta.bp10.activity.main

import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.di.AppComponent
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity

class MainActivity : CoreMainActivity() {

    val appComponent: AppComponent by lazy {
        getAppComponent()
    }

    override fun getViewModel(): MainViewModel {
        appComponent.let {
            it.inject(this)
            foregroundActivityProvider.setActivity(this)

            val mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
            it.inject(mainViewModel)
            return mainViewModel
        }
    }

}
