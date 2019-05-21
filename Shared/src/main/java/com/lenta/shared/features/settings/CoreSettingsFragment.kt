package com.lenta.shared.features.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel

abstract class CoreSettingsFragment : CoreFragment<com.lenta.shared.databinding.FragmentSettingsBinding, CoreSettingsViewModel>(), OnBackPresserListener, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_settings

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_topbar_1 -> vm.onClickBack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.isMainMenu = if (vm.hyperHive.authAPI.isAuthorized) MutableLiveData(true) else MutableLiveData(false)
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.settings)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.back)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll(false)
     }
}