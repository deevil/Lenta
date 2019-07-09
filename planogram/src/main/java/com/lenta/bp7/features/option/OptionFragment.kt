package com.lenta.bp7.features.option

import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentOptionBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.bp7.platform.extentions.getAppTitle
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class OptionFragment : CoreFragment<FragmentOptionBinding, OptionViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_option

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("03")

    override fun getViewModel(): OptionViewModel {
        provideViewModel(OptionViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getAppTitle()
        topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel
                .uiModelButton5.show(ButtonDecorationInfo.next)
        bottomToolbarUiModel
                .uiModelButton1.show(ButtonDecorationInfo.back)

        viewLifecycleOwner.connectLiveData(vm.nextButtonFocus, bottomToolbarUiModel.uiModelButton5.requestFocus)
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }
}
