package com.lenta.bp9.features.transport_marriage

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
import androidx.lifecycle.Observer
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

class TransportMarriageFragment : CoreFragment<FragmentTransportMarriageBinding, TransportMarriageViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnScanResultListener,
        OnKeyDownListener {

    private var cargoUnitsRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var actRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_transport_marriage

    override fun getPageNumber(): String = "09/29"

    override fun getViewModel(): TransportMarriageViewModel {
        provideViewModel(TransportMarriageViewModel::class.java).let {vm ->
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
                if (it == 0) {
                    bottomToolbarUiModel.uiModelButton3.clean()
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = vm.deleteButtonEnabled.value ?: false)
                    connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
                }
            })
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutTransportMarriageCargoUnitsBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_transport_marriage_cargo_units,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_transport_marriage_cargo_units,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileTransportMarriageCargoUnitsBinding> {
                                    override fun onCreate(binding: ItemTileTransportMarriageCargoUnitsBinding) {
                                    }

                                    override fun onBind(binding: ItemTileTransportMarriageCargoUnitsBinding, position: Int) {
                                        cargoUnitsRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    cargoUnitsRecyclerViewKeyHandler?.let {
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
                        cargoUnitsRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.listCargoUnits,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = cargoUnitsRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutTransportMarriageActBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_transport_marriage_act,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.actSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_transport_marriage_act,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileTransportMarriageActBinding> {
                                override fun onCreate(binding: ItemTileTransportMarriageActBinding) {
                                }

                                override fun onBind(binding: ItemTileTransportMarriageActBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.actSelectionsHelper.isSelected(position)
                                    actRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                actRecyclerViewKeyHandler?.let {
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
                    actRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.listAct,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = actRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.list_cargo_units else R.string.act)

    override fun countTab(): Int {
        return 2
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

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.selectedPage.value) {
            0 -> cargoUnitsRecyclerViewKeyHandler
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

}
