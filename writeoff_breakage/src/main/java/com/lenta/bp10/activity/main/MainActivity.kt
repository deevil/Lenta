package com.lenta.bp10.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.di.AppComponent
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.crashlytics.android.Crashlytics
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { intent1 ->
            intent1.action?.let { action ->
                Logg.d("onNewIntent action: ${action}")
                Logg.d("extras: ")
                /*intent1.extras.keySet().forEach {
                    Logg.d("${it}:${intent1.extras.get(it)}")
                }*/

                if (action == "com.symbol.datawedge.krittest") {
                    intent1.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string")?.let {
                        Logg.d { "scan data $it" }
                        fragmentStack.peek()?.implementationOf(OnScanResultListener::class.java)?.onScanResult(it)
                    }

                }


            }


        }

    }

    override fun onClickExit() {
        mainViewModel?.onExitClick()
    }

    override fun onPause() {
        super.onPause()
        mainViewModel?.onPause()
        /*startActivity(Intent(applicationContext, this::class.java).apply {
            flags = FLAG_ACTIVITY_REORDER_TO_FRONT
        })*/
    }

    override fun generateNumberScreen(fragment: CoreFragment<*, *>): String? {
        return numberScreenGenerator.generateNumberScreen(fragment)
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return numberScreenGenerator.getPrefixScreen(fragment)
    }

}
