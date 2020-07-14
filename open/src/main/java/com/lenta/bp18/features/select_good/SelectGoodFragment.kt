package com.lenta.bp18.features.select_good

import android.view.View
import com.lenta.bp18.R
import com.lenta.bp18.databinding.FragmentSelectGoodsBinding
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectGoodFragment : CoreFragment<FragmentSelectGoodsBinding, SelectGoodViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_select_goods

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): SelectGoodViewModel {
        provideViewModel(SelectGoodViewModel::class.java).let{
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.select_goods)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next, enabled = false)
    }

    override fun onToolbarButtonClick(view: View) {
        when(view.id){
           /* R.id.b_1 -> vm.onClickBack()
            R.id.b_5 -> vm.onClickComplete()*/
        }
    }

    companion object {
        const val SCREEN_NUMBER = Constants.SELECT_GOODS_FRAGMENT
    }

}