package com.lenta.bp9.features.main_menu

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMainMenuBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class MainMenuFragment : CoreFragment<FragmentMainMenuBinding, MainMenuViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_main_menu

    override fun getPageNumber(): String = "09/04"

    override fun getViewModel(): MainMenuViewModel {
        provideViewModel(MainMenuViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = context?.getAppInfo(withHash = true)
        topToolbarUiModel.description.value = getString(R.string.main_menu)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.hide()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            com.lenta.shared.R.id.b_topbar_1 -> vm.onClickAuxiliaryMenu()
        }
    }

}
