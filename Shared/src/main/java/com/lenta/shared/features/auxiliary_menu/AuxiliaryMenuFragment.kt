package com.lenta.shared.features.auxiliary_menu

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class AuxiliaryMenuFragment : CoreFragment<com.lenta.shared.databinding.FragmentAuxiliaryMenuBindingImpl, AuxiliaryMenuViewModel>(), OnBackPresserListener, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_auxiliary_menu

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_topbar_1 -> vm.onClickHome()
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.description_auxiliary_menu)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.home)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll(false)
    }

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): AuxiliaryMenuViewModel {
        provideViewModel(AuxiliaryMenuViewModel::class.java).let {
            coreComponent.inject(it)
            return it
        }
    }


}