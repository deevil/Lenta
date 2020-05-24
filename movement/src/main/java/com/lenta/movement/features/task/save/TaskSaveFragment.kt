package com.lenta.movement.features.task.save

import android.os.Bundle
import android.view.View
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskSaveBinding
import com.lenta.movement.databinding.LayoutItemTaskSaveListBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TaskSaveFragment: CoreFragment<FragmentTaskSaveBinding, TaskSaveViewModel>(),
    ToolbarButtonsClickListener {

    private var tasks: List<String>? by state(null)

    companion object {

        fun newInstance(tasks: List<String>): TaskSaveFragment {
            return TaskSaveFragment().apply {
                this.tasks = tasks
            }
        }
    }

    override fun getLayoutId() = R.layout.fragment_task_save

    override fun getPageNumber() = "13/09"

    override fun getViewModel(): TaskSaveViewModel {
        return provideViewModel(TaskSaveViewModel::class.java).also {
            getAppComponent()?.inject(it)
            it.tasks = this.tasks
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.task_save_description)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.rvConfig = DataBindingRecyclerViewConfig<LayoutItemTaskSaveListBinding>(
            layoutId = R.layout.layout_item_task_save_list,
            itemId = BR.item
        )
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onNextClick()
        }
    }

}