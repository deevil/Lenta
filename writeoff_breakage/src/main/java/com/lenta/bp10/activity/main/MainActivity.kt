package com.lenta.bp10.activity.main

import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.di.AppComponent
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity

class MainActivity : CoreMainActivity() {

    var mainViewModel: MainViewModel? = null

    val appComponent: AppComponent by lazy {
        getAppComponent(coreComponent)
    }

    override fun getViewModel(): MainViewModel {
        appComponent.let { component ->
            component.inject(this)
            foregroundActivityProvider.setActivity(this)

            ViewModelProviders.of(this).get(MainViewModel::class.java).let {
                mainViewModel = it
                component.inject(it)
            }
            return mainViewModel!!
        }
    }

    override fun onClickExit() {
        mainViewModel?.onExitClick()
    }

}
