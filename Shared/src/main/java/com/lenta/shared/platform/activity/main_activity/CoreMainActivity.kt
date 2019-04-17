package com.lenta.shared.platform.activity.main_activity

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lenta.shared.R
import com.lenta.shared.databinding.ActivityMainBinding
import com.lenta.shared.features.network_state.NetworkStateReceiver
import com.lenta.shared.platform.activity.CoreActivity
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
    lateinit var networkStateReceiver: NetworkStateReceiver

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
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(networkStateReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkStateReceiver)
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

    abstract fun getBottomToolBarUIModel(): BottomToolbarUiModel

    abstract fun getTopToolbarUIModel(): TopToolbarUiModel


    abstract fun onNewEnter()


}



