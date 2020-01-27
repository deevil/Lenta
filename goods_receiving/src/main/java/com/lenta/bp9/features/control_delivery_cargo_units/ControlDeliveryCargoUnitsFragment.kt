package com.lenta.bp9.features.control_delivery_cargo_units

import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.*
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData

class ControlDeliveryCargoUnitsFragment : CoreFragment<FragmentControlDeliveryCargoUnitsBinding, ControlDeliveryCargoUnitsViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnKeyDownListener,
        OnScanResultListener {

    private var notProcessedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_control_delivery_cargo_units

    override fun getPageNumber() = "09/26"

    override fun getViewModel(): ControlDeliveryCargoUnitsViewModel {
        provideViewModel(ControlDeliveryCargoUnitsViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = vm.getDescription()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        connectLiveData(vm.saveEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return if (vm.isTaskPSP) {
            getPagerEO(container, position)
        } else {
            getPagerGE(container, position)
        }
    }

    private fun getPagerGE(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutControlDeliveryCuBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_control_delivery_cu,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_control_delivery_cu,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileControlDeliveryCuBinding> {
                                    override fun onCreate(binding: ItemTileControlDeliveryCuBinding) {
                                    }

                                    override fun onBind(binding: ItemTileControlDeliveryCuBinding, position: Int) {
                                        notProcessedRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    notProcessedRecyclerViewKeyHandler?.let {
                                        if (it.isSelected(position)) {
                                            vm.onClickItemPosition(position)
                                        } else {
                                            it.selectPosition(position)
                                        }
                                    }

                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        notProcessedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.listNotProcessed,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = notProcessedRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutControlDeliveryCuBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_control_delivery_cu,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_control_delivery_cu,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileControlDeliveryCuBinding> {
                                override fun onCreate(binding: ItemTileControlDeliveryCuBinding) {
                                }

                                override fun onBind(binding: ItemTileControlDeliveryCuBinding, position: Int) {
                                    processedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                processedRecyclerViewKeyHandler?.let {
                                    if (it.isSelected(position)) {
                                        vm.onClickItemPosition(position)
                                    } else {
                                        it.selectPosition(position)
                                    }
                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    processedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.listProcessed,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }

    private fun getPagerEO(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutControlDeliveryEoBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_control_delivery_eo,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_control_delivery_cu,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileControlDeliveryCuBinding> {
                                    override fun onCreate(binding: ItemTileControlDeliveryCuBinding) {
                                    }

                                    override fun onBind(binding: ItemTileControlDeliveryCuBinding, position: Int) {
                                        notProcessedRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    notProcessedRecyclerViewKeyHandler?.let {
                                        if (it.isSelected(position)) {
                                            vm.onClickItemPosition(position)
                                        } else {
                                            it.selectPosition(position)
                                        }
                                    }

                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        notProcessedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.listNotProcessed,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = notProcessedRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutControlDeliveryEoBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_control_delivery_eo,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_control_delivery_cu,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileControlDeliveryCuBinding> {
                                override fun onCreate(binding: ItemTileControlDeliveryCuBinding) {
                                }

                                override fun onBind(binding: ItemTileControlDeliveryCuBinding, position: Int) {
                                    processedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                processedRecyclerViewKeyHandler?.let {
                                    if (it.isSelected(position)) {
                                        vm.onClickItemPosition(position)
                                    } else {
                                        it.selectPosition(position)
                                    }
                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    processedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.listProcessed,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }


    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.not_processed)
            1 -> getString(R.string.processed)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.selectedPage.value) {
            0 -> notProcessedRecyclerViewKeyHandler
            1 -> processedRecyclerViewKeyHandler
            else -> null
        }?.let {
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

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

}
