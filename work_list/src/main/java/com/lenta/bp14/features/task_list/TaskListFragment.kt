package com.lenta.bp14.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentTaskListBinding
import com.lenta.bp14.databinding.ItemTaskStatusGoodsBinding
import com.lenta.bp14.databinding.LayoutTaskListProcessingBinding
import com.lenta.bp14.databinding.LayoutTaskListSearchBinding
import com.lenta.bp14.models.data.TaskListTab
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : KeyDownCoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.task_list)
        topToolbarUiModel.title.value = getString(R.string.title_market_number, vm.marketNumber)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.filter)

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == TaskListTab.PROCESSING.position) {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
                } else {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.filter)
                }
            })
        }

        connectLiveData(vm.thirdButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_3 -> vm.onClickFilter()
            R.id.b_5 -> if (vm.selectedPage.value == TAB_PROCESSING) vm.onClickUpdate() else vm.onClickFilter()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this

    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_PROCESSING -> initProcessingGoodList(container)
            TAB_SEARCH -> initSearchGoodList(container)
            else -> View(context)
        }
    }

    private fun initProcessingGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_list_processing,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTaskUi, ItemTaskStatusGoodsBinding>(
                    layoutId = R.layout.item_task_status_goods,
                    itemId = BR.task,
                    keyHandlerId = TAB_PROCESSING,
                    recyclerView = layoutBinding.rv,
                    items = vm.processingTasks,
                    onClickHandler = vm::onClickProcessingTask
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initSearchGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_list_search,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTaskUi, ItemTaskStatusGoodsBinding>(
                    layoutId = R.layout.item_task_status_goods,
                    itemId = BR.task,
                    keyHandlerId = TAB_SEARCH,
                    recyclerView = layoutBinding.rv,
                    items = vm.searchTasks,
                    onClickHandler = vm::onClickSearchTask
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_PROCESSING -> getString(R.string.processing)
            TAB_SEARCH -> getString(R.string.search)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int = TABS

    override fun onBackPressed(): Boolean {
        vm.onClickMenu()
        return false
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    companion object {
        const val SCREEN_NUMBER = "30"

        private const val TABS = 2
        private const val TAB_PROCESSING = 0
        private const val TAB_SEARCH = 1
    }

}
