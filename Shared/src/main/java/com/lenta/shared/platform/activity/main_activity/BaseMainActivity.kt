package com.lenta.shared.platform.activity.main_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lenta.shared.R
import com.lenta.shared.databinding.ActivityMainBinding
import com.lenta.shared.platform.activity.BaseActivity
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.navigation.FragmentStack
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hideKeyboard
import com.lenta.shared.utilities.extentions.implementationOf

abstract class BaseMainActivity : BaseActivity<ActivityMainBinding>() {

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

    abstract fun getBottomToolBarUIModel() : BottomToolbarUiModel


    abstract fun onNewEnter()


}



