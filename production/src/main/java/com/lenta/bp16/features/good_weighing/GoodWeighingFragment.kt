package com.lenta.bp16.features.good_weighing

import android.view.View
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodWeighingBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodWeighingFragment : CoreFragment<FragmentGoodWeighingBinding, GoodWeighingViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener {

    companion object {
        const val SCREEN_NUMBER = "9"
    }

    override fun getLayoutId(): Int = R.layout.fragment_good_weighing

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodWeighingViewModel {
        provideViewModel(GoodWeighingViewModel::class.java).let {
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
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.getWeight)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete, enabled = false)

        connectLiveData(vm.defectVisibility, getBottomToolBarUIModel()!!.uiModelButton2.visibility)
        connectLiveData(vm.completeEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
        connectLiveData(vm.addEnabled, getBottomToolBarUIModel()!!.uiModelButton4.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickDefect()
            R.id.b_3 -> vm.onClickGetWeight()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

}
