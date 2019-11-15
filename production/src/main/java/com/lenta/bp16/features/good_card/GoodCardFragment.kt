package com.lenta.bp16.features.good_card

import android.view.View
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodCardBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodCardFragment : CoreFragment<FragmentGoodCardBinding, GoodCardViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_card

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("9")

    override fun getViewModel(): GoodCardViewModel {
        provideViewModel(GoodCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.getWeight)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)

        connectLiveData(vm.completeEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)

        connectLiveData(vm.addEnabled, getBottomToolBarUIModel()!!.uiModelButton4.enabled)
        connectLiveData(vm.addVisibility, getBottomToolBarUIModel()!!.uiModelButton4.visibility)

        connectLiveData(vm.getWeightVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickGetWeight()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

}
