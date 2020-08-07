package com.lenta.bp12.features.basket.basket_good_list

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.lenta.bp12.BR
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentBasketGoodListBinding
import com.lenta.bp12.databinding.ItemBasketGoodListGoodBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class BasketGoodListFragment : CoreFragment<FragmentBasketGoodListBinding, BasketGoodListViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_basket_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    private val onClickSelectionListener: View.OnClickListener
        get() = View.OnClickListener {
            (it.tag as Int).let { position ->
                vm.selectionsHelper.revert(position = position)
                binding?.rv?.adapter?.notifyItemChanged(position)
            }
        }

    override fun getViewModel(): BasketGoodListViewModel {
        provideViewModel(BasketGoodListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.basket_good_list)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.properties)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_4 -> vm.onClickProperties()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBasketGoodList()
    }

    private fun initBasketGoodList() {
        binding?.let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemBasketGoodListGoodBinding>(
                    layoutId = R.layout.item_basket_good_list_good,
                    itemId = BR.item
            )

            recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.goods,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    override fun onAdapterBindHandler(bindItem: ViewBinding, position: Int) {
        (bindItem as ItemBasketGoodListGoodBinding).apply {
            bindItem.tvItemNumber.tag = position
            bindItem.tvItemNumber.setOnClickListener(onClickSelectionListener)
            bindItem.selectedForDelete = vm.selectionsHelper.isSelected(position)
            super.onAdapterBindHandler(bindItem, position)
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        const val SCREEN_NUMBER = "13"
    }

}
