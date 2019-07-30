package com.lenta.bp7.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.crashlytics.android.Crashlytics
import com.lenta.bp7.di.AppComponent
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.runIfRelease
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.implementationOf
import io.fabric.sdk.android.Fabric


class MainActivity : CoreMainActivity() {

    var mainViewModel: MainViewModel? = null

    private val numberScreenGenerator = NumberScreenGenerator()

    val appComponent: AppComponent by lazy {
        getAppComponent(coreComponent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runIfRelease {
            Fabric.with(this, Crashlytics())
        }
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

    override fun onPause() {
        super.onPause()
        /*startActivity(Intent(applicationContext, this::class.java).apply {
            flags = FLAG_ACTIVITY_REORDER_TO_FRONT
        })*/
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

}
