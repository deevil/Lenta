package com.lenta.bp9.features.goods_information.marking.task_pge.marking_info_box_pge

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMarkingInfoBoxPgeBinding
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
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
 //       , OnKeyDownListener  для тестов
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

        binding?.etCount?.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
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
        vm.requestFocusToCount.value = false
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
//
//            KeyCode.KEYCODE_0 -> {
//                vm.onScanResult("346060680015914878")
//                true
//            }
//
//            KeyCode.KEYCODE_1 -> {
//                vm.onScanResult("010467003610609821FFu3<d(DYt:c;")
//                true
//            }
//
//            KeyCode.KEYCODE_2 -> {
//                vm.onScanResult("010467003610609821FFf!:sAV<NmhH")
//                true
//            }
//
//            KeyCode.KEYCODE_3 -> {
//                vm.onScanResult("010467003610609821FF64D'U7&jY*0")
//                true
//            }
//            //Марка Лента гн из задания
//            KeyCode.KEYCODE_4 -> {
//                vm.onScanResult("010290000004865221jKU/\"RG(eUD4N\n")
//                true
//            }
//            //Короб Лента не из задания
//            KeyCode.KEYCODE_5 -> {
//                vm.onScanResult("346060680018702229")
//                true
//            }
//            //Марка НЕ Лента
//            KeyCode.KEYCODE_6 -> {
//                vm.onScanResult("010467003610609821FFu3<d(DYt:c1")
//                true
//            }//Корб НЕ Лента
//            KeyCode.KEYCODE_7 -> {
//                vm.onScanResult("346060680018702229")
//                true
//            }
//            else -> false
//        }
//    }

}