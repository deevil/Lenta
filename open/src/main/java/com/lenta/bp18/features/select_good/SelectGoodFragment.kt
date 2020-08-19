package com.lenta.bp18.features.select_good

import android.view.View
import androidx.lifecycle.observe
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
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectGoodFragment : CoreFragment<FragmentSelectGoodsBinding, SelectGoodViewModel>(), ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_select_goods

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): SelectGoodViewModel {
        provideViewModel(SelectGoodViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun onResume() {
        super.onResume()
        vm.requestFocusToBarcode.value = true
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = requireContext().getAppInfo()
        topToolbarUiModel.description.value = getString(R.string.select_goods)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next, enabled = false)

        vm.barcodeField.observe(viewLifecycleOwner) {
            if (vm.barcodeField.value.toString().length == EAN13)
                bottomToolbarUiModel.uiModelButton5.requestFocus()
        }

        connectLiveData(vm.nextButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        const val SCREEN_NUMBER = Constants.SELECT_GOOD_FRAGMENT
        const val EAN13 = 13
    }

}