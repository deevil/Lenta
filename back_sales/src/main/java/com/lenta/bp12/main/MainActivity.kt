package com.lenta.bp12.main

import androidx.lifecycle.ViewModelProvider
import com.lenta.bp12.di.AppComponent
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.lenta.shared.platform.fragment.CoreFragment

class MainActivity : CoreMainActivity() {

    private var mainViewModel: MainViewModel? = null
    private val numberScreenGenerator = NumberScreenGenerator()

    val appComponent: AppComponent by lazy {
        getAppComponent(coreComponent)
    }

    override fun getViewModel(): MainViewModel {
        appComponent.let { component ->
            component.inject(this)
            foregroundActivityProvider.setActivity(this)
            ViewModelProvider(this).get(MainViewModel::class.java).let {
                mainViewModel = it
                component.inject(it)
            }
            return mainViewModel!!
        }
    }

    override fun onClickExit() {
        mainViewModel?.onExitClick()
    }

    override fun generateNumberScreenFromPostfix(postfix: String?): String? {
        return numberScreenGenerator.generateNumberScreenFromPostfix(postfix)
    }

    override fun generateNumberScreen(fragment: CoreFragment<*, *>): String? {
        return numberScreenGenerator.generateNumberScreen(fragment)
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return numberScreenGenerator.getPrefixScreen(fragment)
    }

    override fun getAdditionalListOfRequiredPermissions(): List<String> {
        return emptyList()
    }
}
