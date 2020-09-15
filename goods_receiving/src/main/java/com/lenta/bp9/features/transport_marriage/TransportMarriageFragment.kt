package com.lenta.bp9.features.transport_marriage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class TransportMarriageFragment : KeyDownCoreFragment<FragmentTransportMarriageBinding, TransportMarriageViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnScanResultListener{

    override fun getLayoutId(): Int = R.layout.fragment_transport_marriage

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): TransportMarriageViewModel {
        provideViewModel(TransportMarriageViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.transport_marriage)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.cancellation)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.processAlternate)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == TAB_GE_LIST) {
                    bottomToolbarUiModel.uiModelButton3.clean()
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = vm.deleteButtonEnabled.value
                            ?: false)
                    connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
                }
            })
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == TAB_GE_LIST) {
            DataBindingUtil.inflate<LayoutTransportMarriageCargoUnitsBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_transport_marriage_cargo_units,
                    container,
                    false)
                    .let { layoutBinding ->
                        layoutBinding.rvConfig = initRecycleAdapterDataBinding<CargoUnitsItem, ItemTileTransportMarriageCargoUnitsBinding>(
                                layoutId = R.layout.item_tile_transport_marriage_cargo_units,
                                itemId = BR.item,
                                keyHandlerId = TAB_GE_LIST,
                                recyclerView = layoutBinding.rv,
                                items = vm.listCargoUnits,
                                onClickHandler = vm::onClickItemPosition
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner

                        return layoutBinding.root
                    }
        }

        DataBindingUtil.inflate<LayoutTransportMarriageActBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_transport_marriage_act,
                        container,
                        false)
                .let { layoutBinding ->
                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.actSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_transport_marriage_act,
                            itemId = BR.item,
                            onItemBind = { binding: ItemTileTransportMarriageActBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.actSelectionsHelper.isSelected(position)
                            },
                            keyHandlerId = TAB_ACT,
                            recyclerView = layoutBinding.rv,
                            items = vm.listAct,
                            onClickHandler = vm::onClickItemPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == TAB_GE_LIST) R.string.list_cargo_units else R.string.act)

    override fun countTab(): Int {
        return TABS
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickCancellation()
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_5 -> vm.onClickProcess()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    companion object {
        const val SCREEN_NUMBER = "09/29"

        private const val TABS = 2
        private const val TAB_GE_LIST = 0
        private const val TAB_ACT = 1
    }

}
