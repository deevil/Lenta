package com.lenta.bp10.features.good_information.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentSetsBinding
import com.lenta.bp10.databinding.ItemTileSetsBinding
import com.lenta.bp10.databinding.LayoutSetsComponentsBinding
import com.lenta.bp10.databinding.LayoutSetsQuantityBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel

class SetsFragment :
        CoreFragment<FragmentSetsBinding, SetsViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnScanResultListener {

    private lateinit var productInfo: ProductInfo


    companion object {
        fun create(productInfo: ProductInfo): SetsFragment {
            SetsFragment().let {
                it.productInfo = productInfo
                return it
            }
        }

    }

    override fun getLayoutId(): Int = R.layout.fragment_sets

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): SetsViewModel {
        provideViewModel(SetsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.setProductInfo(productInfo)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.set_info)
        topToolbarUiModel.title.value = "${productInfo.getMaterialLastSix()} ${productInfo.description}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        viewLifecycleOwner.apply {
            connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
            connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
            connectLiveData(vm.enabledDetailsCleanBtn, bottomToolbarUiModel.uiModelButton3.enabled)
            vm.selectedPage.observe(this, Observer { pos ->
                bottomToolbarUiModel.uiModelButton3.show(
                        if (pos == 0) ButtonDecorationInfo.details else ButtonDecorationInfo.clean,
                        enabled = false)
            })
        }
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
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutSetsQuantityBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_sets_quantity,
                            container,
                            false).let {
                        it.lifecycleOwner = viewLifecycleOwner
                        it.vm = vm
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

                    val onClickGoodTitle = View.OnClickListener { v ->
                        Logg.d { "onClickListener ${(v as TextView).text.substring(0, 6)}" }
                        vm.eanCode.value = (v as TextView).text.substring(0, 6)
                        vm.onOkInSoftKeyboard()
                    }

                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_sets,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileSetsBinding> {
                                override fun onCreate(binding: ItemTileSetsBinding) {
                                }

                                override fun onBind(binding: ItemTileSetsBinding, position: Int) {
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

}
