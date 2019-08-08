package com.lenta.bp14.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentTaskListBinding
import com.lenta.bp14.databinding.LayoutTaskListBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener {
    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutTaskListBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_task_list,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig<LayoutTaskListBinding>(
                                layoutId = R.layout.item_tile_tasks,
                                itemId = BR.vm
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        return layoutBinding.root
                    }
        } else {
            View(context)
        }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.not_processed else R.string.search)

    override fun countTab(): Int = 2

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String = "14/05"

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.getMarket()}"
        topToolbarUiModel.description.value = getString(R.string.task_list)

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.update)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.filter)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickUpdate()
            R.id.b_4 -> vm.onClickFilter()
            R.id.b_5 -> vm.onClickSave()
        }
    }


}
