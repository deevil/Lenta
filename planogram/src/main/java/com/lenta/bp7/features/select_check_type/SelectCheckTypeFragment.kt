package com.lenta.bp7.features.select_check_type

import android.view.View
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentSelectCheckTypeBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectCheckTypeFragment : CoreFragment<FragmentSelectCheckTypeBinding, SelectCheckTypeViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_select_check_type

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("05")

    override fun getViewModel(): SelectCheckTypeViewModel {
        provideViewModel(SelectCheckTypeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.description_main_menu)
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromPleApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll(false)
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_topbar_2) {
            vm.onClickExit()
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
