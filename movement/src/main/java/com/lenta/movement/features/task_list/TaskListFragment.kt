package com.lenta.movement.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskListBinding
import com.lenta.movement.databinding.LayoutItemTaskListBinding
import com.lenta.movement.databinding.LayoutTaskListToProcessTabBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel

/** Фрагмент списка задач*/
class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnKeyDownListener {

    private var taskListRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getLayoutId() = R.layout.fragment_task_list

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.tasks_to_move)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onUpdateBtnClick()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (TaskListPage.values()[position]) {
            TaskListPage.TO_PROCESS -> inflateToProcessTab(container)
            TaskListPage.SEARCH -> inflateSearchTab(container)
        }
    }

    private fun inflateToProcessTab(container: ViewGroup?) : View {
        return DataBindingUtil.inflate<LayoutTaskListToProcessTabBinding>(
                LayoutInflater.from(context),
                R.layout.layout_task_list_to_process_tab,
                container,
                false
        ).let { layoutBinding ->
            layoutBinding.rvConfig = oldInitRecycleAdapterDataBinding(
                    layoutId = R.layout.layout_item_task_list,
                    itemId = BR.item,
                    onAdapterItemBind = { binding: LayoutItemTaskListBinding, position ->
                        taskListRecyclerViewKeyHandler?.let {
                            binding.root.isSelected = it.isSelected(position)
                        }
                    },
                    onAdapterItemClicked = { position ->
                        taskListRecyclerViewKeyHandler?.onItemClicked(position)
                    }
            )

            layoutBinding.dataBindingViewModel = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            taskListRecyclerViewKeyHandler = oldInitRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.recyclerView,
                    items = vm.taskItemList,
                    previousPosInfo = taskListRecyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickTaskListItem
            )
            layoutBinding.root
        }
    }

    private fun inflateSearchTab(container: ViewGroup?): View {
        return View(context) //TODO 
    }

    override fun getTextTitle(position: Int): String {
        return when (TaskListPage.values()[position]) {
            TaskListPage.TO_PROCESS -> getString(R.string.to_process)
            TaskListPage.SEARCH -> getString(R.string.search)
        }
    }

    override fun countTab() = TaskListPage.values().size

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.currentPage.value) {
            TaskListPage.TO_PROCESS -> taskListRecyclerViewKeyHandler
            else -> null
        }?.let {
            if (!it.onKeyDown(keyCode)) {
                keyCode.digit?.let { digit ->
                    vm.onDigitPressed(digit)
                    return true
                }
                return false
            }
            return true
        }
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "13/19"
    }
}

