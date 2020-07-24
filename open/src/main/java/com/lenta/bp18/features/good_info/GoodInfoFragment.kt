package com.lenta.bp18.features.good_info

import android.view.View
import androidx.lifecycle.Observer
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

class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, GoodInfoViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodInfoViewModel {
        provideViewModel(GoodInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.deviceIp.value = context!!.getDeviceId()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)

        viewLifecycleOwner.apply {
            vm.good.observe(this, Observer { good ->
                topToolbarUiModel.title.value = getString(R.string.title_good_sap_name, good.getFormattedMaterial(), good.name)
            })
        }
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
        private const val KEY_EAN_VALUE = "KEY_EAN_VALUE"

        fun newInstance(ean: String) = GoodInfoFragment().apply {
            arguments = bundleOf(KEY_EAN_VALUE to ean)
        }
    }

}