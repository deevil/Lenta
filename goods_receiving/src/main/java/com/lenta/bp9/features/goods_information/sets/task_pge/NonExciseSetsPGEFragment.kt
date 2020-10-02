package com.lenta.bp9.features.goods_information.sets.task_pge

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentNonExciseSetsPgeBinding
import com.lenta.bp9.databinding.ItemTileNonExciseSetsComponentsBinding
import com.lenta.bp9.databinding.LayoutNonExciseSetsComponentsPgeBinding
import com.lenta.bp9.databinding.LayoutNonExciseSetsCountedPgeBinding
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class NonExciseSetsPGEFragment : CoreFragment<FragmentNonExciseSetsPgeBinding, NonExciseSetsPGEViewModel>(),
        ViewPagerSettings,
        OnScanResultListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    companion object {
        fun create(productInfo: TaskProductInfo, isDiscrepancy: Boolean): NonExciseSetsPGEFragment {
            NonExciseSetsPGEFragment().let {
                it.productInfo = productInfo
                it.isDiscrepancy = isDiscrepancy
                return it
            }
        }
    }

    private var isDiscrepancy by state<Boolean?>(null)
    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_non_excise_sets_pge

    override fun getPageNumber(): String = "09/39"

    override fun getViewModel(): NonExciseSetsPGEViewModel {
        provideViewModel(NonExciseSetsPGEViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            vm.isDiscrepancy.value = this.isDiscrepancy
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value!!.getMaterialLastSix()} ${vm.productInfo.value!!.description}"
        topToolbarUiModel.description.value = getString(R.string.set_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == 1) {
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.clean)
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
                    connectLiveData(vm.enabledCleanButton, bottomToolbarUiModel.uiModelButton2.enabled)
                } else {
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.clean, visible = false)
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details, visible = false)
                }
            })
        }

        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        when (position) {
            0 -> {
                DataBindingUtil
                        .inflate<LayoutNonExciseSetsCountedPgeBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_non_excise_sets_counted_pge,
                                container,
                                false)
                        .let { layoutBinding ->
                            layoutBinding.spinnerQuality.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                                    vm.onClickPositionSpinQuality(position)
                                }

                                override fun onNothingSelected(adapterView: AdapterView<*>) {
                                }
                            }

                            layoutBinding.etCount.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                                    if (vm.enabledApplyButton.value == true) {
                                        vm.onClickApply()
                                    }
                                    return@OnKeyListener true
                                }
                                false
                            })

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            return layoutBinding.root
                        }
            }
            else -> {
                DataBindingUtil
                        .inflate<LayoutNonExciseSetsComponentsPgeBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_non_excise_sets_components_pge,
                                container,
                                false)
                        .let { layoutBinding ->
                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.componentsSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                    layoutId = R.layout.item_tile_non_excise_sets_components,
                                    itemId = BR.item,
                                    onItemBind = { binding: ItemTileNonExciseSetsComponentsBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.componentsSelectionsHelper.isSelected(position)
                                    },
                                    keyHandlerId = position,
                                    recyclerView = layoutBinding.rv,
                                    items = vm.listComponents,
                                    onClickHandler = vm::onClickItemPosition
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            return layoutBinding.root
                        }
            }
        }

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickClean()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun getTextTitle(position: Int): String {
        return getString(if (position == 0) R.string.quantity else R.string.components)
    }

    override fun countTab(): Int {
        return 2
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
        if (vm.selectedPage.value == 0) {
            vm.requestFocusToCount.value = true
        } else {
            vm.requestFocusToEan.value = true
        }
        vm.onResume()
    }
}
