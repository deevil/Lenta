package com.lenta.bp15.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp15.R
import com.lenta.bp15.BR
import com.lenta.bp15.databinding.*
import com.lenta.bp15.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : KeyDownCoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title
        topToolbarUiModel.description.value = getString(R.string.setting_tasks)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == 0) {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
                } else {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.filter)
                }
            })
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> if (vm.selectedPage.value == 0) vm.onClickUpdate() else vm.onClickFilter()
        }
    }

    override fun onResume() {
        super.onResume()

        if (vm.isNeedUpdateTaskList()) {
            vm.updateTaskList()
        }

        if (vm.selectedPage.value == 0) {
            vm.requestFocusToProcessingField.value = true
        } else {
            vm.requestFocusToSearchField.value = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_TASKS -> initTaskList(container)
            TAB_SEARCH -> initSearchList(container)
            else -> View(context)
        }
    }

    private fun initTaskList(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutTaskListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_list_processing,
                container,
                false)

        layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTaskUi, ItemTaskListBinding>(
                layoutId = R.layout.item_task_list,
                itemId = BR.item,
                keyHandlerId = TAB_TASKS,
                recyclerView = layoutBinding.rv,
                items = vm.processingList,
                onClickHandler = vm::onClickItemProcessingPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }

    private fun initSearchList(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutTaskListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_list_search,
                container,
                false)

        layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTaskUi, ItemTaskListBinding>(
                layoutId = R.layout.item_task_list,
                itemId = BR.item,
                keyHandlerId = TAB_SEARCH,
                recyclerView = layoutBinding.rv,
                items = vm.searchList,
                onClickHandler = vm::onClickItemSearchPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }


    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_TASKS -> getString(R.string.to_processing)
            TAB_SEARCH -> getString(R.string.search)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return TABS
    }

    companion object {
        const val SCREEN_NUMBER = "6"

        private const val TABS = 2
        private const val TAB_TASKS = 0
        private const val TAB_SEARCH = 1
    }

}