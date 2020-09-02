package com.lenta.bp12.features.create_task.good_info

import android.view.View
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentGoodInfoCreateBinding
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

class GoodInfoCreateFragment : CoreFragment<FragmentGoodInfoCreateBinding, GoodInfoCreateViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_info_create

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodInfoCreateViewModel {
        provideViewModel(GoodInfoCreateViewModel::class.java).let {
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
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.close)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        connectLiveData(vm.rollbackVisibility, bottomToolbarUiModel.uiModelButton2.visibility)
        connectLiveData(vm.rollbackEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
        connectLiveData(vm.closeEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.closeVisibility, bottomToolbarUiModel.uiModelButton4.visibility)
        connectLiveData(vm.applyEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
//            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickClose()
//            R.id.b_3 -> vm.onScanResult("147300249826851018001FZSIZAB5I6KZKWEQKPKZJHW6MYKVGAETXLPV7M5AIF7OXTQFIM347EWQGXAK65QGJFKTR7EQDHJQTJFSW5DNWTBU3BRLKVM7D6YZMYRBV6IOQY5ZXLPKLBHUZPBTRFTLQ") // Марка
            //R.id.b_3 -> vm.onScanResult("1734001784926710180016BZ3532QMZKOBPRTXTL7BZMZ3YNNMK53PXMB3ZU66TJ3SNVFR7YTCYVLOPKUNBQIG5XXLKNYYWMWGGUXJLVHB2NLSMF6ACBJDB73IUKGGSAEOWKBY7TW7FZ5BLIT3YT2Y") // SAP-код: 270202156641
            //R.id.b_3 -> vm.onScanResult("236200647504871018001FCCBM6EJ4RTKG5J6SZPIOVDIA4G3QGAZLK3HVONWWBVHXJYO3HOAX633MX756X27L27QPWSTGUNJM5IZL2X67XID6FSVVZAFI5OXWE5XJNHQMELI76JC45KQN2GH5VD7Y") // SAP-код: 444877
            //R.id.b_3 -> vm.onScanResult("22N00000XOIJT87CH2W0123456789012345678901234567890123456789000000001") // Марка 156641
            //R.id.b_3 -> vm.onScanResult("22N00001CRDKFRWFBZ90123456789012345678901234567890123456789000000001") // Марка 377456
            //R.id.b_3 -> vm.onScanResult("22N00002NWKKIF6RWF30123456789012345678901234567890123456789000000004") // Партия
            //R.id.b_3 -> vm.onScanResult("03000048752210319000100516") // Коробка
            //R.id.b_3 -> vm.onScanResult("01000000637810119000001340") // Коробка
            //R.id.b_3 -> vm.onScanResult("03000042907513119000404111") // Коробка 082682
            //R.id.b_3 -> vm.onScanResult("4607055090121") // ШК
            //R.id.b_3 -> vm.onScanResult("4607149780501") // ШК

            R.id.b_3 -> vm.onScanResult("22N00002NWKKIF6RWF30123456789012345678901234567890123456789000000004") // Акиза 351076
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

    override fun onResume() {
        super.onResume()
        vm.updateData()
    }

    companion object {
        const val SCREEN_NUMBER = "12"
    }

}
