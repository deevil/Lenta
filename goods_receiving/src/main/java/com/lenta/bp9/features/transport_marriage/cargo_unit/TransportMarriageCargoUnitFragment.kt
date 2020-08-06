package com.lenta.bp9.features.transport_marriage.cargo_unit

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentTransportMarriageCargoUnitBinding
import com.lenta.bp9.databinding.ItemTileTransportMarriageActBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TransportMarriageCargoUnitFragment : CoreFragment<FragmentTransportMarriageCargoUnitBinding, TransportMarriageCargoUnitViewModel>(),
        ToolbarButtonsClickListener,
        OnKeyDownListener,
        OnScanResultListener {

    private var cargoUnitNumber by state<String?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_transport_marriage_cargo_unit

    override fun getPageNumber(): String = "09/30"

    override fun getViewModel(): TransportMarriageCargoUnitViewModel {
        provideViewModel(TransportMarriageCargoUnitViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.cargoUnitNumber.value = this.cargoUnitNumber
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = "${getString(R.string.transport_marriage_cargo_unit_head)}-${vm.cargoUnitNumber.value}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.cancellation)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.entirely)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.applyButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickCancellation()
            R.id.b_2 -> vm.onClickEntirely()
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.actSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_transport_marriage_act,
                    itemId = BR.item,
                    onAdapterItemBind = { binding: ItemTileTransportMarriageActBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.actSelectionsHelper.isSelected(position)
                    }
            )
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        recyclerViewKeyHandler?.let {
            if (!it.onKeyDown(keyCode)) {
                keyCode.digit?.let { digit ->
                    vm.onDigitPressed(digit)
                    return true
                }
                return false
            }
            return true
        }
        return false
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.onResult(arguments.getFragmentResultCode())
    }

    companion object {
        fun create(cargoUnitNumber: String): TransportMarriageCargoUnitFragment {
            TransportMarriageCargoUnitFragment().let {
                it.cargoUnitNumber = cargoUnitNumber
                return it
            }
        }
    }

}
