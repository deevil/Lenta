package com.lenta.bp9.features.reject

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentRejectBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class RejectFragment : CoreFragment<FragmentRejectBinding, RejectViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_reject

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): RejectViewModel {
        provideViewModel(RejectViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.reject_recieving)    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.full)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.temporary)
        connectLiveData(vm.buttonsEndbled, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.buttonsEndbled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onClickFull()
            R.id.b_5 -> vm.onClickTemporary()
        }
    }

}
