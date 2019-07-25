package com.lenta.inventory.features.goods_information.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentSetsInfoBinding
import com.lenta.inventory.databinding.ItemTileSetsInfoBinding
import com.lenta.inventory.databinding.LayoutSetsInfoComponentsBinding
import com.lenta.inventory.databinding.LayoutSetsInfoQuantityBinding
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class SetsInfoFragment : CoreFragment<FragmentSetsInfoBinding, SetsInfoViewModel>(),
        ViewPagerSettings,
        OnScanResultListener,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    private var productInfo: TaskProductInfo? = null

    companion object {
        fun create(productInfo: TaskProductInfo): SetsInfoFragment {
            SetsInfoFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_sets_info

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): SetsInfoViewModel {
        provideViewModel(SetsInfoViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            vm.spinList.value = listOf(getString(R.string.quantity))
            vm.titleProgressScreen.value = getString(R.string.data_loading)
            vm.componentNotFound.value = getString(R.string.component_not_found)
            vm.stampAnotherProduct.value = getString(R.string.stamp_another_product)
            vm.alcocodeNotFound.value = getString(R.string.alcocode_not_found)
            vm.limitExceeded.value = getString(R.string.limit_exceeded)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.set_info)
        productInfo?.let {
            topToolbarUiModel.title.value = "${it.getMaterialLastSix()} ${it.description}"
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        viewLifecycleOwner.apply {
            connectLiveData(vm.enabledMissingButton, bottomToolbarUiModel.uiModelButton4.enabled)
            connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
            connectLiveData(vm.enabledDetailsCleanBtn, bottomToolbarUiModel.uiModelButton3.enabled)
            vm.selectedPage.observe(viewLifecycleOwner, Observer { pos ->
                if (pos == 0) {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
                    bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
                    bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing, visible = false)
                }
            })
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickButton3()
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener=this}
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position ==0) {
            DataBindingUtil
                    .inflate<LayoutSetsInfoQuantityBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_sets_info_quantity,
                            container,
                            false).let {
                        it.vm = vm
                        it.lifecycleOwner = viewLifecycleOwner
                        return it.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutSetsInfoComponentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_sets_info_components,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.componentsSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    val onClickGoodTitle = View.OnClickListener {v ->
                        vm.searchCode.value = (v as TextView).text.substring(0,6)
                        vm.onOkInSoftKeyboard()
                    }

                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_sets_info,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileSetsInfoBinding> {
                                override fun onCreate(binding: ItemTileSetsInfoBinding) {
                                }

                                override fun onBind(binding: ItemTileSetsInfoBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.tvGoodTitle.setOnClickListener(onClickGoodTitle)
                                    binding.selectedForDelete = vm.componentsSelectionsHelper.isSelected(position)
                                }

                            }
                    )
                    layoutBinding.vm = vm
                    return layoutBinding.root
                }

    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.quantity else R.string.components)

    override fun countTab(): Int = 2

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
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
        return true
    }


}
