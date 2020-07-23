package com.lenta.shared.platform.activity.main_activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import app_update.AppUpdateInstaller
import com.lenta.shared.R
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.databinding.ActivityMainBinding
import com.lenta.shared.di.FromParentToCoreProvider
import com.lenta.shared.exception.Failure
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.keys.OnKeyUpListener
import com.lenta.shared.platform.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.CoreActivity
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.activity.INumberScreenGenerator
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.app_update.AppUpdateChecker
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
import javax.inject.Inject
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.scan.atol.AtolScanHelper
import com.lenta.shared.scan.cipherlab.CipherLabScanHelper
import com.lenta.shared.scan.zebra.ZebraScanHelper
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER

abstract class CoreMainActivity : CoreActivity<ActivityMainBinding>(), ToolbarButtonsClickListener, INumberScreenGenerator {

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    @Inject
    lateinit var batteryStateMonitor: BatteryStateMonitor

    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider

    @Inject
    lateinit var scanHelper: IScanHelper

    @Inject
    lateinit var screenNavigator: ICoreNavigator

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper


    val honeywellScanHelper = HoneywellScanHelper()
    val newLandScanHelper = NewLandScanHelper()
    val zebraScanHelper = ZebraScanHelper()
    val atolScanHelper = AtolScanHelper()
    val cipherLabScanHelper = CipherLabScanHelper()

    private val vm: CoreMainViewModel by lazy {
        getViewModel().apply {
            if (getNotGrantedPermissions().isEmpty()) {
                analyticsHelper.onPermissionGranted()
                ANALYTICS_HELPER = analyticsHelper
                analyticsHelper.logAppInfo()
                analyticsHelper.logDeviceInfo()
            }
        }
    }

    val fragmentStack: FragmentStack by lazy {
        FragmentStack(supportFragmentManager, R.id.fragments).apply {
            coreComponent.inject(this)
        }
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

        setupScanner()
    }

    private fun setupScanner() {
        var lastTimeScanned = 0L
        val scanObserver = Observer<String> {
            Logg.d { "scan result: $it" }
            it?.let { code ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTimeScanned > 500L) {
                    getCurrentFragment()?.implementationOf(OnScanResultListener::class.java)?.onScanResult(code)
                }
                lastTimeScanned = currentTime
            }

        }

        scanHelper.scanResult.observe(this, scanObserver)

        honeywellScanHelper.scanResult.observe(this, scanObserver)
        honeywellScanHelper.init(this)

        newLandScanHelper.scanResult.observe(this, scanObserver)

        zebraScanHelper.scanResult.observe(this, scanObserver)

        atolScanHelper.scanResult.observe(this, scanObserver)

        cipherLabScanHelper.scanResult.observe(this, scanObserver)
    }

    override fun onResume() {
        super.onResume()
        getNotGrantedPermissions().let {
            if (it.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                        this,
                        it.toTypedArray(),
                        1
                )
            }
        }

        networkStateMonitor.start(this)
        batteryStateMonitor.start(this)
        scanHelper.startListen(this)
        foregroundActivityProvider.setActivity(this)
        honeywellScanHelper.startListen(this)
        newLandScanHelper.startListen(this)
        zebraScanHelper.startListen(this)
        atolScanHelper.startListen(this)
        cipherLabScanHelper.startListen(this)
        vm.onResume()
    }

    private fun getNotGrantedPermissions(): List<String> {
        return getNotGrantedPermissions(
                mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE).apply {
                    this.addAll(getAdditionalListOfRequiredPermissions())
                }
        )
    }

    open fun getAdditionalListOfRequiredPermissions(): List<String> {
        return emptyList()
    }

    override fun onPause() {
        foregroundActivityProvider.clear()
        super.onPause()
        networkStateMonitor.stop(this)
        batteryStateMonitor.stop(this)
        scanHelper.stopListen(this)
        honeywellScanHelper.stopListen(this)
        newLandScanHelper.stopListen(this)
        zebraScanHelper.stopListen(this)
        atolScanHelper.stopListen(this)
        cipherLabScanHelper.stopListen(this)
        vm.onPause()
    }

    override fun onBackPressed() {
        getCurrentFragment()?.implementationOf(OnBackPresserListener::class.java)?.let {
            if (it.onBackPressed()) {
                analyticsHelper.onGoBack()
                super.onBackPressed()
            }
            return
        }
        if (isHaveBackButton()) {
            analyticsHelper.onGoBack()
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        screenNavigator.finishApp(restart = grantResults.all { it == PERMISSION_GRANTED })
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

        analyticsHelper.onClickToolbarButton(view)

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

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        getCurrentFragment()?.implementationOf(OnKeyUpListener::class.java)?.let { onKeyUpListener ->
            event?.keyCode?.let { keyCode ->
                return onKeyUpListener.onKeyUp(keyCode = KeyCode.detectKeyCode(keyCode))
            }

        }
        return super.onKeyUp(keyCode, event)
    }

    private fun isHaveBackButton(): Boolean {
        getBottomToolBarUIModel().uiModelButton1.let {
            return it.buttonDecorationInfo.value === ButtonDecorationInfo.back && it.enabled.value == true && it.visibility.value == true
        }
    }

    private fun isHaveExitButton(): Boolean {
        return getTopToolbarUIModel().uiModelButton2.buttonDecorationInfo.value === ImageButtonDecorationInfo.exitFromApp
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

    fun showSimpleProgress(title: String, handleFailure: ((Failure) -> Unit)? = null) {
        vm.showSimpleProgress(title, handleFailure)
    }

    fun hideProgress() {
        vm.hideProgress()
    }

    open fun provideFromParentToCoreProvider(): FromParentToCoreProvider? {
        return null
    }

    protected abstract fun getViewModel(): CoreMainViewModel

    abstract fun onClickExit()

    override fun onUserInteraction() {
        super.onUserInteraction()
        vm.onUserInteraction()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            hideKeyboardAfterOutsideTouch(it)
        }

        return super.dispatchTouchEvent(event)
    }

    private fun hideKeyboardAfterOutsideTouch(event: MotionEvent) {
        val view = currentFocus
        if (view is EditText &&
                (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_MOVE) &&
                !view.javaClass.name.startsWith("android.webkit.")
        ) {
            val scrCoords = IntArray(2)
            view.getLocationOnScreen(scrCoords)

            val x: Float = event.rawX + view.getLeft() - scrCoords[0]
            val y: Float = event.rawY + view.getTop() - scrCoords[1]

            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) {
                val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
            }
        }
    }

}



