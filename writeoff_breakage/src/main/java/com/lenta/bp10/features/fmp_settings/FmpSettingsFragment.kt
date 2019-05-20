package com.lenta.bp10.features.fmp_settings

import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentFmpSettingsBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class FmpSettingsFragment : CoreFragment<FragmentFmpSettingsBinding, FmpSettingsViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_fmp_settings

    override fun getPageNumber(): String {
        return ""
    }

    override fun getViewModel(): FmpSettingsViewModel {
        provideViewModel(FmpSettingsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            com.lenta.shared.R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(com.lenta.shared.R.string.connection_settings)

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }


}
