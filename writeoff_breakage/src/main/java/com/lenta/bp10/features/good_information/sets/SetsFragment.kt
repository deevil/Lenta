package com.lenta.bp10.features.good_information.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentSetsBinding
import com.lenta.bp10.databinding.ItemTileSetsBinding
import com.lenta.bp10.databinding.LayoutSetsComponentsBinding
import com.lenta.bp10.databinding.LayoutSetsQuantityBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.state.state

class SetsFragment :
        KeyDownCoreFragment<FragmentSetsBinding, SetsViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnScanResultListener {

    private var productInfo: ProductInfo? by state(null)

    private var quantity by state(0.0)

    override fun getLayoutId(): Int = R.layout.fragment_sets

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): SetsViewModel {
        provideViewModel(SetsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            if (vm.setProductInfo.value == null) {
                vm.setProductInfo.value = productInfo
            }

            if (vm.count.value == null) {
                vm.count.value = quantity.toStringFormatted()
            }

            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.set_info)

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
        connectLiveData(vm.enabledDetailsCleanBtn, bottomToolbarUiModel.uiModelButton3.enabled)

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickButton3()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }

        getBottomToolBarUIModel()?.let { bottomToolbarUiModel ->
            vm.selectedPage.observe(this.viewLifecycleOwner, Observer { pos ->
                bottomToolbarUiModel.uiModelButton3.show(
                        if (pos == 0) ButtonDecorationInfo.details else ButtonDecorationInfo.clean,
                        enabled = false)
            })
        }

        connectLiveData(vm.title, getTopToolBarUIModel()!!.title)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutSetsQuantityBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_sets_quantity,
                            container,
                            false).let {
                        it.vm = vm
                        it.lifecycleOwner = viewLifecycleOwner
                        return it.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutSetsComponentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_sets_components,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.componentsSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = oldInitRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_sets,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileSetsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.componentsSelectionsHelper.isSelected(position)
                                onAdapterBindHandler(binding, position)
                            }
                    )
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    recyclerViewKeyHandler = oldInitRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            items = vm.componentsLiveData,
                            previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                            onClickHandler = vm::onClickItemPosition
                    )
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.quantity else R.string.components)

    override fun countTab(): Int = 2

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onFragmentResult(arguments: Bundle) {
        vm.onResult(arguments.getFragmentResultCode())
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        if (vm.selectedPage.value == KEY_DOWN_SELECTED_PAGE) {
            return recyclerViewKeyHandler?.onKeyDown(keyCode) ?: false
        }
        return false
    }

    companion object {
        private const val KEY_DOWN_SELECTED_PAGE = 1

        fun newInstance(productInfo: ProductInfo, quantity: Double): SetsFragment {
            SetsFragment().let {
                it.productInfo = productInfo
                it.quantity = quantity
                return it
            }
        }
    }

}
