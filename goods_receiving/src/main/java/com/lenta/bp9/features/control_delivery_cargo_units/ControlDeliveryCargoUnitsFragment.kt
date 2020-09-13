package com.lenta.bp9.features.control_delivery_cargo_units

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class ControlDeliveryCargoUnitsFragment : KeyDownCoreFragment<FragmentControlDeliveryCargoUnitsBinding, ControlDeliveryCargoUnitsViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnScanResultListener,
        OnBackPresserListener {

    private var isUnlockTaskLoadingScreen: Boolean? = null

    override fun getLayoutId(): Int = R.layout.fragment_control_delivery_cargo_units

    override fun getPageNumber() = SCREEN_NUMBER

    override fun getViewModel(): ControlDeliveryCargoUnitsViewModel {
        provideViewModel(ControlDeliveryCargoUnitsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.isUnlockTaskLoadingScreen.value = isUnlockTaskLoadingScreen
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = vm.getDescription()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (vm.taskType == TaskType.ShipmentRC) {
            bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.missing)
            connectLiveData(vm.enabledMissingBtn, bottomToolbarUiModel.uiModelButton3.enabled)
        }
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        connectLiveData(vm.saveEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (vm.taskType) {
            TaskType.OwnProduction -> getPagerEO(container, position)
            TaskType.ShipmentRC -> getPagerShipmentRC(container, position)
            else -> getPagerGE(container, position)
        }
    }

    private fun getPagerGE(container: ViewGroup, position: Int): View {
        if (position == TAB_NOT_PROCESSED) {
            DataBindingUtil
                    .inflate<LayoutControlDeliveryCuBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_control_delivery_cu,
                            container,
                            false)
                    .let { layoutBinding ->
                        layoutBinding.rvConfig = initRecycleAdapterDataBinding<ControlDeliveryCargoUnitItem, ItemTileControlDeliveryCuBinding>(
                                layoutId = R.layout.item_tile_control_delivery_cu,
                                itemId = BR.item,
                                keyHandlerId = TAB_NOT_PROCESSED,
                                recyclerView = layoutBinding.rv,
                                items = vm.listNotProcessed,
                                onClickHandler = vm::onClickItemPosition
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner

                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutControlDeliveryCuBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_control_delivery_cu,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding<ControlDeliveryCargoUnitItem, ItemTileControlDeliveryCuBinding>(
                            layoutId = R.layout.item_tile_control_delivery_cu,
                            itemId = BR.item,
                            keyHandlerId = TAB_PROCESSED,
                            recyclerView = layoutBinding.rv,
                            items = vm.listProcessed,
                            onClickHandler = vm::onClickItemPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    private fun getPagerEO(container: ViewGroup, position: Int): View {
        if (position == TAB_NOT_PROCESSED) {
            DataBindingUtil
                    .inflate<LayoutControlDeliveryEoBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_control_delivery_eo,
                            container,
                            false)
                    .let { layoutBinding ->
                        layoutBinding.rvConfig = initRecycleAdapterDataBinding<ControlDeliveryCargoUnitItem, ItemTileControlDeliveryCuBinding>(
                                layoutId = R.layout.item_tile_control_delivery_cu,
                                itemId = BR.item,
                                keyHandlerId = TAB_NOT_PROCESSED,
                                recyclerView = layoutBinding.rv,
                                items = vm.listNotProcessed,
                                onClickHandler = vm::onClickItemPosition
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner

                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutControlDeliveryEoBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_control_delivery_eo,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding<ControlDeliveryCargoUnitItem, ItemTileControlDeliveryCuBinding>(
                            layoutId = R.layout.item_tile_control_delivery_cu,
                            itemId = BR.item,
                            keyHandlerId = TAB_PROCESSED,
                            recyclerView = layoutBinding.rv,
                            items = vm.listProcessed,
                            onClickHandler = vm::onClickItemPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    private fun getPagerShipmentRC(container: ViewGroup, position: Int): View {
        if (position == TAB_NOT_PROCESSED) {
            DataBindingUtil
                    .inflate<LayoutControlDeliveryShipmentCuBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_control_delivery_shipment_cu,
                            container,
                            false)
                    .let { layoutBinding ->
                        val onClickSelectionListener = View.OnClickListener {
                            (it!!.tag as Int)
                                    .let { position ->
                                        vm.notProcessedSelectionsHelper.revert(position = position)
                                        layoutBinding.rv.adapter?.notifyItemChanged(position)
                                    }
                        }

                        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                layoutId = R.layout.item_tile_control_delivery_shipment_cu,
                                itemId = BR.item,
                                onItemBind = { binding: ItemTileControlDeliveryShipmentCuBinding, position: Int ->
                                    binding.tvItemNumber.tag = position
                                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.notProcessedSelectionsHelper.isSelected(position)

                                },
                                keyHandlerId = TAB_NOT_PROCESSED,
                                recyclerView = layoutBinding.rv,
                                items = vm.listNotProcessed,
                                onClickHandler = vm::onClickItemPosition
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner

                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutControlDeliveryCuBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_control_delivery_cu,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding<ControlDeliveryCargoUnitItem, ItemTileControlDeliveryCuBinding>(
                            layoutId = R.layout.item_tile_control_delivery_cu,
                            itemId = BR.item,
                            keyHandlerId = TAB_PROCESSED,
                            recyclerView = layoutBinding.rv,
                            items = vm.listProcessed,
                            onClickHandler = vm::onClickItemPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_NOT_PROCESSED -> getString(R.string.not_processed)
            TAB_PROCESSED -> getString(R.string.processed)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return TABS
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        fun newInstance(isUnlockTaskLoadingScreen: Boolean?): ControlDeliveryCargoUnitsFragment {
            ControlDeliveryCargoUnitsFragment().let {
                it.isUnlockTaskLoadingScreen = isUnlockTaskLoadingScreen
                return it
            }
        }

        const val SCREEN_NUMBER = "09/26"

        private const val TABS = 2
        private const val TAB_NOT_PROCESSED = 0
        private const val TAB_PROCESSED = 1
    }

}
