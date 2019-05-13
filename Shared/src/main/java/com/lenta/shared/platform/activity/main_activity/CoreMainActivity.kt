package com.lenta.shared.platform.activity.main_activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lenta.shared.R
import com.lenta.shared.databinding.ActivityMainBinding
import com.lenta.shared.platform.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.CoreActivity
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.battery_state.BatteryStateMonitor
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.navigation.FragmentStack
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hideKeyboard
import com.lenta.shared.utilities.extentions.implementationOf
import javax.inject.Inject

abstract class CoreMainActivity : CoreActivity<ActivityMainBinding>(), ToolbarButtonsClickListener {

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    @Inject
    lateinit var batteryStateMonitor: BatteryStateMonitor

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
        vm.statusBarUiModel.printerTasksCount.value = -1

    }

    override fun onResume() {
        super.onResume()
        networkStateMonitor.start(this)
        batteryStateMonitor.start(this)
        foregroundActivityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        networkStateMonitor.stop(this)
        batteryStateMonitor.stop(this)
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
        Logg.d("onBackStackChanged")
        this.hideKeyboard()
        updateNumberPage()
    }

    private fun updateNumberPage() {
        vm.statusBarUiModel.pageNumber.postValue(getCurrentFragment()?.implementationOf(CoreFragment::class.java)
                ?.getPageNumber() ?: "???")

    }

    override fun onToolbarButtonClick(view: View) {
        this.hideKeyboard()
        Logg.d { "onToolbarButtonClick ${view.id}" }
        if(view.id == R.id.b_1 && getBottomToolBarUIModel().uiModelButton1.buttonDecorationInfo.value == ButtonDecorationInfo.back) {
            onBackPressed()
            return
        }

        if(view.id == R.id.b_topbar_2 && getTopToolbarUIModel().uiModelButton2.buttonDecorationInfo.value == ImageButtonDecorationInfo.exitFromApp) {
            onClickExit()
            return
        }

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

    abstract fun onClickExit()


}



