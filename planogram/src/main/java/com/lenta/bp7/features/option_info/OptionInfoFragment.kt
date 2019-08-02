package com.lenta.bp7.features.option_info

import android.view.View
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentOptionInfoBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.bp7.platform.extentions.getAppTitle
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class OptionInfoFragment : CoreFragment<FragmentOptionInfoBinding, OptionInfoViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_option_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("07")

    override fun getViewModel(): OptionInfoViewModel {
        provideViewModel(OptionInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getAppTitle()
        topToolbarUiModel.description.value = getString(R.string.description_auxiliary_menu)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.apply {
            show(ButtonDecorationInfo.next)
            requestFocus()
        }
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }
}
