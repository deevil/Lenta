package com.lenta.bp7.features.good_list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp7.BR
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentGoodListBinding
import com.lenta.bp7.databinding.ItemGoodBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodListFragment : CoreFragment<FragmentGoodListBinding, GoodListViewModel>(), ToolbarButtonsClickListener {
    override fun getLayoutId(): Int = R.layout.fragment_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("12")

    override fun getViewModel(): GoodListViewModel {
        provideViewModel(GoodListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.list_of_processed_goods)

        vm.segmentNumber.observe(this, Observer<String> { segmentNumber ->
            topToolbarUiModel.title.value = getString(R.string.title_segment_shelf_number, segmentNumber, vm.shelfNumber.value)
        })

        vm.shelfNumber.observe(this, Observer<String> { shelfNumber ->
            topToolbarUiModel.title.value = getString(R.string.title_segment_shelf_number, vm.segmentNumber.value, shelfNumber)
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = true)
    }

    override fun onToolbarButtonClick(view: View) {
        /*when (view.id) {
            R.id.b_5 -> vm.onClickApply()
        }*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.rvConfig = DataBindingRecyclerViewConfig<ItemGoodBinding>(
                layoutId = R.layout.item_good,
                itemId = BR.good
        )
    }
}
