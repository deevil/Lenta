package com.lenta.bp10.features.good_information.marked

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentMarkedBinding
import com.lenta.bp10.databinding.ItemMarkedGoodPropertyBinding
import com.lenta.bp10.databinding.LayoutMarkedPropertiesBinding
import com.lenta.bp10.databinding.LayoutMarkedQuantityBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class MarkedFragment : CoreFragment<FragmentMarkedBinding, MarkedViewModel>(),
        ViewPagerSettings, OnScanResultListener, ToolbarButtonsClickListener, OnBackPresserListener {

    var productInfo by state<ProductInfo?>(null)

    var initCount by state<Double?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_marked

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): MarkedViewModel {
        provideViewModel(MarkedViewModel::class.java).let { viewModel ->
            getAppComponent()?.inject(viewModel)

            productInfo?.let {
                viewModel.setProductInfo(it)
            }
            initCount?.let {
                viewModel.initCount(it)
                initCount = null
            }

            return viewModel
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        productInfo?.let {
            topToolbarUiModel.title.value = "${it.getMaterialLastSix()} ${it.description}"
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.enabledApplyButton, bottomToolbarUiModel.uiModelButton5.enabled)
        connectLiveData(vm.enabledDetailsButton, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.selectedPosition, bottomToolbarUiModel.uiModelButton4.requestFocus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
        }

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_QUANTITY -> initMarkedGoodQuantity(container)
            TAB_PROPERTIES -> initMarkedGoodProperties(container)
            else -> View(context)
        }
    }

    private fun initMarkedGoodQuantity(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutMarkedQuantityBinding>(LayoutInflater.from(container.context),
                R.layout.layout_marked_quantity,
                container,
                false
        ).let { layoutBinding ->
            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initMarkedGoodProperties(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutMarkedPropertiesBinding>(LayoutInflater.from(container.context),
                R.layout.layout_marked_properties,
                container,
                false
        ).let { layoutBinding ->
            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemMarkedGoodPropertyBinding>(
                    layoutId = R.layout.item_marked_good_property,
                    itemId = BR.item
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_QUANTITY -> getString(R.string.quantity)
            TAB_PROPERTIES -> getString(R.string.properties)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int = TABS

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        return vm.onBackPressed()
    }

    override fun onFragmentResult(arguments: Bundle) {
        vm.handleFragmentResult(arguments.getFragmentResultCode())

    }

    companion object {
        private const val TABS = 2
        private const val TAB_QUANTITY = 0
        private const val TAB_PROPERTIES = 1

        fun create(productInfo: ProductInfo, initCount: Double): MarkedFragment {
            MarkedFragment().let {
                it.productInfo = productInfo
                it.initCount = initCount
                return it
            }
        }
    }

}