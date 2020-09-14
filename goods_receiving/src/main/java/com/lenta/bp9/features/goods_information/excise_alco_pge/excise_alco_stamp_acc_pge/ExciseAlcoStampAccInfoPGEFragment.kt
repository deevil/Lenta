package com.lenta.bp9.features.goods_information.excise_alco_pge.excise_alco_stamp_acc_pge

import android.os.Bundle
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentExciseAlcoStampAccInfoBinding
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

class ExciseAlcoStampAccInfoPGEFragment : CoreFragment<FragmentExciseAlcoStampAccInfoBinding, ExciseAlcoStampAccInfoPGEViewModel>(),
        ToolbarButtonsClickListener,
        OnScanResultListener,
        OnBackPresserListener,
        OnKeyDownListener {

    companion object {
        fun create(productInfo: TaskProductInfo): ExciseAlcoStampAccInfoPGEFragment {
            ExciseAlcoStampAccInfoPGEFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_stamp_acc_info_pge

    override fun getPageNumber(): String = "09/64"

    override fun getViewModel(): ExciseAlcoStampAccInfoPGEViewModel {
        provideViewModel(ExciseAlcoStampAccInfoPGEViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix()} ${vm.productInfo.value?.description}"
        topToolbarUiModel.description.value = getString(R.string.goods_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledRollbackBtn, bottomToolbarUiModel.uiModelButton2.enabled)
        connectLiveData(vm.enabledAddBtn, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onFragmentResult(arguments: Bundle) {
        if (arguments.getInt("manufacturerSelectedPosition") != 0 && arguments.getString("bottlingDate") != null) {
            super.onFragmentResult(arguments)
            vm.onBatchSignsResult(arguments.getInt("manufacturerSelectedPosition"), arguments.getString("bottlingDate"))
        }else
            vm.onBatchSignsResult()
    }


    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

//    override fun onResume() {
//        super.onResume()
//        vm.requestFocusToCount.value = true
//    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        return when (keyCode) {
            // 504550
            //Блок Мрц 106
            KeyCode.KEYCODE_0 -> {
                vm.onScanResult("147300083204421018001BLLJICQZBJELGE3G4NXDDHMLCAOOHXLNQ4LBRVV2RSXECASCRWL5B2TBUPNVCE4BVMOUONX5OWYCCP4LGBIKTTWYYBDDJYMDOWT7R3YNIAYY3SIVTFWZT5G5JHBO51779")
                true
            }
            //Блок Мрц 100
            KeyCode.KEYCODE_1 -> {
                vm.onScanResult("147300083204421018001BLLJICQZBJELGE3G4NXDDHMLCAOOHXLNQ4LBRVV2RSXECASCRWL5B2TBUPNVCE4BVMOUONX5OWYCCP4LGBIKTTWYYBDDJYMDOWT7R3YNIAYY3SIVTFWZT5G5JHBO51772")
                true
            }
            //пачка
            KeyCode.KEYCODE_2 -> {
                vm.onScanResult("147300083204421018001BLLJICQZBJELGE3G4NXDDHMLCAOOHXLNQ4LBRVV2RSXECASCRWL5B2TBUPNVCE4BVMOUONX5OWYCCP4LGBIKTTWYYBDDJYMDOWT7R3YNIAYY3SIVTFWZT5G5JHBO51771")
                true
            }
            //Коробка обуви
            KeyCode.KEYCODE_3 -> {
                vm.onScanResult("147300083204421018001BLLJICQZBJELGE3G4NXDDHMLCAOOHXLNQ4LBRVV2RSXECASCRWL5B2TBUPNVCE4BVMOUONX5OWYCCP4LGBIKTTWYYBDDJYMDOWT7R3YNIAYY3SIVTFWZT5G5JHBO51770")
                true
            }
            //Марка из этой коробки
            KeyCode.KEYCODE_4 -> {
                vm.onScanResult("010460606832937221bBjpnxLePjMmv.918000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
                true
            }
            //Марка не из этой коробки
            KeyCode.KEYCODE_5 -> {
                vm.onScanResult("010460606832938921q8Pk81bQ/9GPR.918000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
                true
            }
            else -> false
        }
    }

}
