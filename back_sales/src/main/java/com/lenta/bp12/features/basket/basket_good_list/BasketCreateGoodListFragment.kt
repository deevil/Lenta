package com.lenta.bp12.features.basket.basket_good_list

import android.os.Bundle
import android.view.View
import com.lenta.bp12.BR
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentBasketCreateGoodListBinding
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

class BasketCreateGoodListFragment : CoreFragment<FragmentBasketCreateGoodListBinding, BasketCreateGoodListViewModel>(),
        ToolbarButtonsClickListener, OnScanResultListener {


    override fun getLayoutId(): Int = R.layout.fragment_basket_create_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    private val onClickSelectionListener: View.OnClickListener
        get() = View.OnClickListener {
            (it.tag as Int).let { position ->
                vm.selectionsHelper.revert(position = position)
                binding?.rv?.adapter?.notifyItemChanged(position)
            }
        }

    override fun getViewModel(): BasketCreateGoodListViewModel {
        provideViewModel(BasketCreateGoodListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.basket_good_list)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.close)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.open)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.properties)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.isCloseBtnVisible, bottomToolbarUiModel.uiModelButton1.visibility)
        connectLiveData(vm.isCloseBtnEnabled, bottomToolbarUiModel.uiModelButton1.enabled)
        connectLiveData(vm.isOpenBtnVisible, bottomToolbarUiModel.uiModelButton2.visibility)
        connectLiveData(vm.isOpenBtnEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickClose()
            R.id.b_2 -> vm.onClickOpen()
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_4 -> vm.onClickProperties()
            R.id.b_5 -> vm.onClickNext()
//ForTesting
//R.id.b_4 -> vm.onScanResult("4607149780488")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBasketGoodList()
    }

    private fun initBasketGoodList() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_basket_good_list_good,
                    itemId = BR.item,
                    onItemBind = { binding: ItemBasketGoodListGoodBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                    },
                    recyclerView = layoutBinding.rv,
                    items = vm.goods,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        private const val SCREEN_NUMBER = "13"
    }

}
