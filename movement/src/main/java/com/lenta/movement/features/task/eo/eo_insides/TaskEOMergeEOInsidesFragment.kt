package com.lenta.movement.features.task.eo.eo_insides

import android.os.Bundle
import android.view.View
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskEoMergeEoInsidesBinding
import com.lenta.movement.databinding.LayoutItemEoInsidesGoodsListBinding
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

/** Список вложенных ЕО (при нажатии на элемент в списке ГЕ на экране Объединение ЕО (TaskEOMergeFragment)*/
class TaskEOMergeEOInsidesFragment : KeyDownCoreFragment<FragmentTaskEoMergeEoInsidesBinding, TaskEOMergeEOInsidesViewModel>(),
        OnBackPresserListener {

    private var eo: ProcessingUnit? by state(null)

    override fun getLayoutId() = R.layout.fragment_task_eo_merge_eo_insides

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskEOMergeEOInsidesViewModel {
        provideViewModel(TaskEOMergeEOInsidesViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            if (eo != null) {
                vm.eo.value = eo
            }
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.layout_item_eo_insides_goods_list,
                    itemId = BR.item,
                    onItemBind = { binding: LayoutItemEoInsidesGoodsListBinding, position ->
                        binding.tvCounter.tag = position
                    },
                    recyclerView = recyclerView,
                    items = this@TaskEOMergeEOInsidesFragment.vm.goodsItemList
            )
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getString(R.string.list_of_eo_goods)
        topToolbarUiModel.description.value = vm.getTitle()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "13/21"

        fun newInstance(inputEo: ProcessingUnit): TaskEOMergeEOInsidesFragment {
            return TaskEOMergeEOInsidesFragment().apply {
                this.eo = inputEo
            }
        }
    }

}

