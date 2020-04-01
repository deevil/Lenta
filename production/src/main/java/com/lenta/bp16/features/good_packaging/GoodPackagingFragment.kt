package com.lenta.bp16.features.good_packaging

import android.view.View
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodWeighingBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodPackagingFragment : CoreFragment<FragmentGoodWeighingBinding, GoodPackagingViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_packaging

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("33")

    override fun getViewModel(): GoodPackagingViewModel {
        provideViewModel(GoodPackagingViewModel::class.java).let {
            getAppComponent()?.inject(it)

            it.deviceIp.value = context!!.getDeviceIp()

            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.defect)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete, enabled = false)

        connectLiveData(vm.defectVisibility, getBottomToolBarUIModel()!!.uiModelButton2.visibility)
        connectLiveData(vm.completeEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickDefect()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

}
