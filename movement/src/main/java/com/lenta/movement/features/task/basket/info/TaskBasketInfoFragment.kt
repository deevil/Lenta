package com.lenta.movement.features.task.basket.info

import android.os.Bundle
import android.view.View
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskBasketInfoBinding
import com.lenta.movement.databinding.LayoutItemBasketPropertiesBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TaskBasketInfoFragment : CoreFragment<FragmentTaskBasketInfoBinding, TaskBasketInfoViewModel>() {

    private var basketIndex: Int by state(DEFAULT_BASKET_INDEX)

    override fun getLayoutId() = R.layout.fragment_task_basket_info

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskBasketInfoViewModel {
        return provideViewModel(TaskBasketInfoViewModel::class.java).also { viewModel ->
            getAppComponent()?.inject(viewModel)
            viewModel.basketIndex = basketIndex
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title.value
        topToolbarUiModel.description.value = getString(R.string.task_basket_info_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            rvConfig = initRecycleAdapterDataBinding<BasketProperty, LayoutItemBasketPropertiesBinding>(
                    layoutId = R.layout.layout_item_basket_properties,
                    itemId = BR.item,
                    recyclerView = rv,
                    items = this@TaskBasketInfoFragment.vm.propertyItems
            )
        }
    }

    companion object {
        private const val PAGE_NUMBER = "13/06"
        private const val DEFAULT_BASKET_INDEX = -1

        fun newInstance(basketIndex: Int): TaskBasketInfoFragment {
            return TaskBasketInfoFragment().apply {
                this.basketIndex = basketIndex
            }
        }
    }
}