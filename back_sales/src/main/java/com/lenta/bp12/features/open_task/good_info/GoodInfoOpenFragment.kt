package com.lenta.bp12.features.open_task.good_info

import android.view.View
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentGoodInfoOpenBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoOpenFragment : CoreFragment<FragmentGoodInfoOpenBinding, GoodInfoOpenViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_info_open

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodInfoOpenViewModel {
        provideViewModel(GoodInfoOpenViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_info)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        connectLiveData(vm.rollbackVisibility, getBottomToolBarUIModel()!!.uiModelButton2.visibility)
        connectLiveData(vm.detailsVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
        connectLiveData(vm.missingVisibility, getBottomToolBarUIModel()!!.uiModelButton4.visibility)
        connectLiveData(vm.missingEnabled, getBottomToolBarUIModel()!!.uiModelButton4.enabled)
        connectLiveData(vm.rollbackEnabled, getBottomToolBarUIModel()!!.uiModelButton2.enabled)
        connectLiveData(vm.applyEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickMissing()
            //R.id.b_4 -> vm.onScanResult("147300249826851018001FZSIZAB5I6KZKWEQKPKZJHW6MYKVGAETXLPV7M5AIF7OXTQFIM347EWQGXAK65QGJFKTR7EQDHJQTJFSW5DNWTBU3BRLKVM7D6YZMYRBV6IOQY5ZXLPKLBHUZPBTRFTLQ")
            //R.id.b_4 -> vm.onScanResult("22N00001CRDKFRWFBZ90123456789012345678901234567890123456789000000001") // Марка 156641
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        const val SCREEN_NUMBER = "12"
    }

}
