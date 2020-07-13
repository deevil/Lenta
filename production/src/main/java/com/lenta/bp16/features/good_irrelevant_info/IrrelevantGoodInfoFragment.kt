package com.lenta.bp16.features.good_irrelevant_info

import android.view.View
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodIrrelevantInfoBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.provideViewModel

class IrrelevantGoodInfoFragment : CoreFragment<FragmentGoodIrrelevantInfoBinding, IrrelevantGoodInfoViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_without_manufacturer

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): IrrelevantGoodInfoViewModel {
        provideViewModel(IrrelevantGoodInfoViewModel::class.java).let{
            getAppComponent()?.inject(it)
            it.deviceIp.value = context!!.getDeviceId()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete, enabled = false)

        connectLiveData(vm.completeEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when(view.id){
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    companion object {
        const val SCREEN_NUMBER = com.lenta.bp16.platform.Constants.GOODS_INFO_FRAGMENT
    }

}
