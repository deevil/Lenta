package com.lenta.movement.features.home

import android.view.View
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentHomeBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class HomeFragment: CoreFragment<FragmentHomeBinding, HomeViewModel>(),
    ToolbarButtonsClickListener {

    override fun getLayoutId() = R.layout.fragment_home

    override fun getPageNumber(): String = "10/04"

    override fun getViewModel(): HomeViewModel {
        provideViewModel(HomeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = context?.getAppInfo()
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