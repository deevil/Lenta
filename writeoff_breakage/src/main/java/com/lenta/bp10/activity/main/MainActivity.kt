package com.lenta.bp10.activity.main

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.lenta.bp10.R
import com.lenta.bp10.di.getAppComponent
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.activity.main_activity.BaseMainActivity
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import javax.inject.Inject

class MainActivity : BaseMainActivity() {

    @Inject
    lateinit var foregroundActivityProvider: ForegroundActivityProvider

    private val mainViewModel: MainViewModel by lazy {
        getAppComponent().let {
            it.inject(this)
            foregroundActivityProvider.setActivity(this)

            val mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
            it.inject(mainViewModel)
            return@lazy mainViewModel
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding?.vm = mainViewModel
        binding?.layoutTopToolbar?.bExit?.setOnClickListener {
            mainViewModel.topToolbarUiModel.value = TopToolbarUiModel(title = System.currentTimeMillis().toString(), description = "current time")
            mainViewModel.bottomToolbarUiModel.uiModelButton1.buttonDecorationInfo.value = ButtonDecorationInfo(iconRes = R.drawable.ic_arrow_back_white_24dp, titleRes = R.string.back)
            mainViewModel.bottomToolbarUiModel.uiModelButton2.buttonDecorationInfo.value = ButtonDecorationInfo(iconRes = R.drawable.ic_restore_white_24dp, titleRes = R.string.revert)
            mainViewModel.bottomToolbarUiModel.uiModelButton3.buttonDecorationInfo.value = ButtonDecorationInfo(iconRes = R.drawable.ic_description_white_24dp, titleRes = R.string.details)
            mainViewModel.bottomToolbarUiModel.uiModelButton4.buttonDecorationInfo.value = ButtonDecorationInfo(iconRes = R.drawable.ic_add_white_24dp, titleRes = R.string.add)
            mainViewModel.bottomToolbarUiModel.uiModelButton5.buttonDecorationInfo.value = ButtonDecorationInfo(iconRes = R.drawable.ic_done_white_24dp, titleRes = R.string.apply)

            mainViewModel.bottomToolbarUiModel.visibility.let {
                it.value = !(it.value ?: false)
            }

        }

        mainViewModel.bottomToolbarUiModel.visibility.value = true
        binding?.layoutTopToolbar?.bSettings?.setOnClickListener {
            mainViewModel.bottomToolbarUiModel.uiModelButton1.let {
                it.visible.value = true
                it.enabled.value = true
            }
            mainViewModel.bottomToolbarUiModel.uiModelButton2.let {
                it.visible.value = true
                it.enabled.value = true
            }
            mainViewModel.bottomToolbarUiModel.uiModelButton3.let {
                it.visible.value = true
                it.enabled.value = true
            }
            mainViewModel.bottomToolbarUiModel.uiModelButton4.let {
                it.visible.value = true
                it.enabled.value = true
            }
            mainViewModel.bottomToolbarUiModel.uiModelButton5.let {
                it.visible.value = true
                it.enabled.value = true
            }
        }

        binding?.layoutBottomToolbar?.b2?.setOnClickListener {
            mainViewModel.bottomToolbarUiModel.uiModelButton2.visible.let {
                it.value = !(it.value ?: false)
            }
        }

        binding?.layoutBottomToolbar?.b1?.setOnClickListener {
            mainViewModel.bottomToolbarUiModel.uiModelButton1.enabled.let {
                it.value = !(it.value ?: false)
            }
        }


    }

    override fun onNewEnter() {
        mainViewModel.onNewEnter()
    }

    override fun onResume() {
        super.onResume()
        foregroundActivityProvider.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        foregroundActivityProvider.clear()
    }

}
