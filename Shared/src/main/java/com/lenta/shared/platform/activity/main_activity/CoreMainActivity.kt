package com.lenta.shared.platform.activity.main_activity

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lenta.shared.R
import com.lenta.shared.databinding.ActivityMainBinding
import com.lenta.shared.features.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.CoreActivity
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.navigation.FragmentStack
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hideKeyboard
import com.lenta.shared.utilities.extentions.implementationOf
import javax.inject.Inject

abstract class CoreMainActivity : CoreActivity<ActivityMainBinding>(), ToolbarButtonsClickListener {

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider

    private val vm: CoreMainViewModel by lazy {
        getViewModel()
    }

    val fragmentStack: FragmentStack by lazy {
        FragmentStack(supportFragmentManager, R.id.fragments)
    }


    override fun getLayoutId() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentStack.setOnBackStackChangedListener(FragmentManager.OnBackStackChangedListener { onBackStackChanged() })
        if (savedInstanceState == null) {
            onNewEnter()
        }
        binding?.toolbarButtonsClickListener = this

        binding?.vm = vm
        vm.statusBarUiModel.pageNumber.value = "10/01"

        vm.statusBarUiModel.printerTasksCount.value = 2
        vm.statusBarUiModel.batteryLevel.value = 100
        vm.statusBarUiModel.time.value = "10:23"

    }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        registerReceiver(networkStateMonitor, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        foregroundActivityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkStateMonitor)
        foregroundActivityProvider.clear()
    }

    override fun onBackPressed() {
        getCurrentFragment()?.implementationOf(OnBackPresserListener::class.java)?.let {
            if (it.onBackPressed()) {
                super.onBackPressed()
            }
            return
        }
        super.onBackPressed()
    }

    private fun getCurrentFragment(): Fragment? = fragmentStack.peek()

    private fun onBackStackChanged() {
        Logg.d()
        this.hideKeyboard()
    }

    override fun onToolbarButtonClick(view: View) {
        this.hideKeyboard()
        Logg.d { "onToolbarButtonClick ${view.id}" }
        getCurrentFragment()?.implementationOf(ToolbarButtonsClickListener::class.java)?.onToolbarButtonClick(view)
    }

    fun getBottomToolBarUIModel(): BottomToolbarUiModel {
        return vm.bottomToolbarUiModel
    }

    fun getTopToolbarUIModel(): TopToolbarUiModel {
        return vm.topToolbarUiModel
    }

    private fun onNewEnter() {
        Logg.d { "onNewEnter" }
        vm.onNewEnter()
    }


    abstract fun getViewModel(): CoreMainViewModel


}



