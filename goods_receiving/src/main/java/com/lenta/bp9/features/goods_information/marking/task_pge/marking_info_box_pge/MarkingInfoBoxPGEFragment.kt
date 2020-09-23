package com.lenta.bp9.features.goods_information.marking.task_pge.marking_info_box_pge

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMarkingInfoBoxPgeBinding
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class MarkingInfoBoxPGEFragment : CoreFragment<FragmentMarkingInfoBoxPgeBinding, MarkingInfoBoxPGEViewModel>(),
        ToolbarButtonsClickListener,
        OnScanResultListener,
        OnBackPresserListener
//        , OnKeyDownListener
{

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_marking_info_box_pge

    override fun getPageNumber(): String = PAGE_NUMBER

    override fun getViewModel(): MarkingInfoBoxPGEViewModel {
        provideViewModel(MarkingInfoBoxPGEViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            this.productInfo?.let { vm.initProduct(it) }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.goods_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.boxes)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledBox, bottomToolbarUiModel.uiModelButton2.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.spinnerQuality?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinQuality(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) = Unit
        }

        binding?.etCount?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (vm.enabledApplyButton.value == true) {
                    vm.onClickApply()
                }
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onDestroyView() {
        binding?.etCount?.setOnKeyListener(null)
        binding?.spinnerQuality?.onItemSelectedListener = null
        super.onDestroyView()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickBoxes()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
//        vm.requestFocusToCount.value = false
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "09/61"
        fun newInstance(productInfo: TaskProductInfo): MarkingInfoBoxPGEFragment {
            MarkingInfoBoxPGEFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    // Для тестов
//    override fun onKeyDown(keyCode: KeyCode): Boolean {
//        return when (keyCode) {
//            // 504550
//            //Блок Мрц 106
//            KeyCode.KEYCODE_0 -> {
//                vm.onScanResult("346060680015914878")
//                true
//            }
//            //Блок Мрц 100
//            KeyCode.KEYCODE_1 -> {
//                vm.onScanResult("010467003610609821FFf!:sAV<NmhH")
//                true
//            }
//            //пачка
//            KeyCode.KEYCODE_2 -> {
//                vm.onScanResult("010467003610609821FFu3<d(DYt:c1")
//                true
//            }
//            //Коробка обуви
//            KeyCode.KEYCODE_3 -> {
//                vm.onScanResult("147300083204421018001BLLJICQZBJELGE3G4NXDDHMLCAOOHXLNQ4LBRVV2RSXECASCRWL5B2TBUPNVCE4BVMOUONX5OWYCCP4LGBIKTTWYYBDDJYMDOWT7R3YNIAYY3SIVTFWZT5G5JHBO51770")
//                true
//            }
//            //Марка из этой коробки
//            KeyCode.KEYCODE_4 -> {
//                vm.onScanResult("010460606832937221bBjpnxLePjMmv.918000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                true
//            }
//            //Марка не из этой коробки
//            KeyCode.KEYCODE_5 -> {
//                vm.onScanResult("010460606832938921q8Pk81bQ/9GPR.918000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                true
//            }
//            else -> false
//        }
//    }

}