package com.lenta.bp18.features.good_info

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp18.R
import com.lenta.bp18.databinding.FragmentGoodInfoBinding
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, GoodInfoViewModel>(), ToolbarButtonsClickListener {

/*    private val selectedEan by unsafeLazy {
        arguments?.getString(KEY_EAN_VALUE) ?: throw IllegalArgumentException("There is no data in bundle at key $KEY_EAN_VALUE")
    }*/

    private val weight by unsafeLazy {
        arguments?.getString(KEY_WEIGHT_VALUE) ?: throw IllegalArgumentException("There is no data in bundle at key $KEY_WEIGHT_VALUE")
    }

    private val goodInfo by unsafeLazy {
       arguments?.getBundle(KEY_GOOD_INFO) ?: throw  IllegalArgumentException("There is no data in bundle at key $KEY_GOOD_INFO")
    }

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodInfoViewModel {
        provideViewModel(GoodInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.deviceIp.value = context!!.getDeviceId()
            it.selectedEan.value = goodInfo.getString("EAN")
            it.weight.value = weight.toInt()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)
        topToolbarUiModel.title.value = getString(R.string.title_good_sap_name,goodInfo.getString("MATERIAL") , goodInfo.getString("NAME"))
        }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete/*, enabled = false*/)
    }

    override fun onToolbarButtonClick(view: View) {
        when(view.id){
            R.id.b_1 -> vm.onClickBack()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    companion object {
        const val SCREEN_NUMBER = Constants.GOODS_INFO_FRAGMENT
        private const val KEY_WEIGHT_VALUE = "KEY_WEIGHT_VALUE"
        private const val KEY_GOOD_INFO = "KEY_GOOD_INFO"

        fun newInstance(goodInfo: Bundle, weight: String?) = GoodInfoFragment().apply {
            arguments = bundleOf(KEY_GOOD_INFO to goodInfo, KEY_WEIGHT_VALUE to weight)
        }
    }

}