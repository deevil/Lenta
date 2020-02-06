package com.lenta.bp14.features.main_menu

import android.view.View
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentMainMenuBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class MainMenuFragment : CoreFragment<FragmentMainMenuBinding, MainMenuViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_main_menu

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("04")

    override fun getViewModel(): MainMenuViewModel {
        provideViewModel(MainMenuViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = context?.getAppInfo()
        topToolbarUiModel.description.value = getString(R.string.main_menu)

        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
        topToolbarUiModel.uiModelButton3.show(ImageButtonDecorationInfo.authorization)

        connectLiveData(vm.authorizationButtonVisibility, topToolbarUiModel.uiModelButton3.visibility)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.hide()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_topbar_1 -> vm.onClickAuxiliaryMenu()
            R.id.b_topbar_3 -> vm.onClickAuthorization()
        }
    }
}