package com.lenta.bp7.features.good_list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp7.BR
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentGoodListBinding
import com.lenta.bp7.databinding.ItemGoodBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodListFragment : CoreFragment<FragmentGoodListBinding, GoodListViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(PAGE_NUMBER)

    override fun getViewModel(): GoodListViewModel {
        provideViewModel(GoodListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.description_list_of_processed_goods)

        viewLifecycleOwner.apply {
            vm.segmentNumber.observe(this, Observer { segmentNumber ->
                topToolbarUiModel.title.value = getString(R.string.title_segment_shelf_number, segmentNumber, vm.shelfNumber.value)
            })

            vm.shelfNumber.observe(this, Observer { shelfNumber ->
                topToolbarUiModel.title.value = getString(R.string.title_segment_shelf_number, vm.segmentNumber.value, shelfNumber)
            })
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.apply {
            connectLiveData(vm.applyButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
        }

        initRvConfig()
    }

    override fun onResume() {
        super.onResume()
        vm.updateGoodList()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickApply()
        }
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = oldInitRecycleAdapterDataBinding<ItemGoodBinding>(
                    layoutId = R.layout.item_good,
                    itemId = BR.good
            )

            oldRecyclerViewKeyHandler = oldInitRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.goods,
                    previousPosInfo = oldRecyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        private const val PAGE_NUMBER = "12"
    }
}