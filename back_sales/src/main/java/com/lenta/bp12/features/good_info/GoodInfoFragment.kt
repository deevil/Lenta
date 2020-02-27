package com.lenta.bp12.features.good_info

import android.view.View
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentGoodInfoBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, GoodInfoViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("12")

    override fun getViewModel(): GoodInfoViewModel {
        provideViewModel(GoodInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title
        topToolbarUiModel.description.value = getString(R.string.good_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.rollbackVisibility, getBottomToolBarUIModel()!!.uiModelButton2.visibility)
        connectLiveData(vm.detailsVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
        connectLiveData(vm.missingVisibility, getBottomToolBarUIModel()!!.uiModelButton4.visibility)
        connectLiveData(vm.rollbackEnabled, getBottomToolBarUIModel()!!.uiModelButton2.enabled)
        connectLiveData(vm.missingEnabled, getBottomToolBarUIModel()!!.uiModelButton4.enabled)
        connectLiveData(vm.applyEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickApply()
        }
    }

}
