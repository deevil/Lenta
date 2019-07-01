package com.lenta.shared.platform.activity.main_activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.lenta.shared.R
import com.lenta.shared.databinding.ActivityMainBinding
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.CoreActivity
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.activity.INumberScreenGenerator
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.battery_state.BatteryStateMonitor
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.navigation.FragmentStack
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.scan.honeywell.HoneywellScanHelper
import com.lenta.shared.scan.newland.NewLandScanHelper
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hideKeyboard
import com.lenta.shared.utilities.extentions.implementationOf
import javax.inject.Inject

abstract class CoreMainActivity : CoreActivity<ActivityMainBinding>(), ToolbarButtonsClickListener, INumberScreenGenerator {

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    @Inject
    lateinit var batteryStateMonitor: BatteryStateMonitor

    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider

    @Inject
    lateinit var scanHelper: IScanHelper

    val honeywellScanHelper = HoneywellScanHelper()
    val newLandScanHelper = NewLandScanHelper()

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

        scanHelper.scanResult.observe(this, Observer<String> {
            Logg.d { "scan result: $it" }
            it?.let { code ->
                getCurrentFragment()?.implementationOf(OnScanResultListener::class.java)?.onScanResult(code)
            }

        })

        honeywellScanHelper.scanResult.observe(this, Observer<String> {
            Logg.d { "scan result: $it" }
            it?.let { code ->
                getCurrentFragment()?.implementationOf(OnScanResultListener::class.java)?.onScanResult(code)
            }

        })

        honeywellScanHelper.init(this)

        newLandScanHelper.scanResult.observe(this, Observer<String> {
            Logg.d { "scan result: $it" }
            it?.let { code ->
                getCurrentFragment()?.implementationOf(OnScanResultListener::class.java)?.onScanResult(code)
            }
        })

    }

    override fun onResume() {
        super.onResume()
        networkStateMonitor.start(this)
        batteryStateMonitor.start(this)
        scanHelper.startListen(this)
        foregroundActivityProvider.setActivity(this)
        honeywellScanHelper.startListen(this)
        newLandScanHelper.startListen(this)
    }

    override fun onPause() {
        foregroundActivityProvider.clear()
        super.onPause()
        networkStateMonitor.stop(this)
        batteryStateMonitor.stop(this)
        scanHelper.stopListen(this)
        honeywellScanHelper.stopListen(this)
        newLandScanHelper.stopListen(this)
    }

    override fun onBackPressed() {
        getCurrentFragment()?.implementationOf(OnBackPresserListener::class.java)?.let {
            if (it.onBackPressed()) {
                super.onBackPressed()
            }
            return
        }
        if (isHaveBackButton()) {
            super.onBackPressed()
        }
    }

    private fun getCurrentFragment(): Fragment? = fragmentStack.peek()

    private fun onBackStackChanged() {
        Logg.d("onBackStackChanged")
        this.hideKeyboard()
        updateNumberPage()
    }

    private fun updateNumberPage() {
        getCurrentFragment()?.implementationOf(CoreFragment::class.java)?.getPageNumber()?.let {
            vm.statusBarUiModel.pageNumber.postValue(it)
        }


    }

    override fun onToolbarButtonClick(view: View) {
        this.hideKeyboard()
        Logg.d { "onToolbarButtonClick ${view.id}" }

        if (view.id == R.id.b_1 && isHaveBackButton()) {
            onBackPressed()
            return
        }

        if (view.id == R.id.b_topbar_2 && isHaveExitButton()) {
            onClickExit()
            return
        }

        getCurrentFragment()?.implementationOf(ToolbarButtonsClickListener::class.java)?.onToolbarButtonClick(view)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val onKeyDownListener = getCurrentFragment()?.implementationOf(OnKeyDownListener::class.java)
        var handled = false
        val detectedKeyCode = KeyCode.detectKeyCode(event.keyCode)
        if (onKeyDownListener != null) {
            handled = onKeyDownListener.onKeyDown(detectedKeyCode)
        }
        if (!handled && detectedKeyCode === KeyCode.KEYCODE_ESCAPE) {
            onBackPressed()
            handled = true
        }

        return if (!handled) {
            super.onKeyDown(keyCode, event)
        } else true

    }

    private fun isHaveBackButton(): Boolean {
        getBottomToolBarUIModel().uiModelButton1.let {
            return it.buttonDecorationInfo.value == ButtonDecorationInfo.back && it.enabled.value == true && it.visibility.value == true
        }
    }

    private fun isHaveExitButton(): Boolean {
        return getTopToolbarUIModel().uiModelButton2.buttonDecorationInfo.value == ImageButtonDecorationInfo.exitFromApp
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

    fun showSimpleProgress(title: String) {
        vm.showSimpleProgress(title)
    }

    fun hideProgress() {
        vm.hideProgress()
    }

    protected abstract fun getViewModel(): CoreMainViewModel

    abstract fun onClickExit()

}



