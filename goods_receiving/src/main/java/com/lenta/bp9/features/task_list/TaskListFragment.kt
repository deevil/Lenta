package com.lenta.bp9.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ToolbarButtonsClickListener,
        ViewPagerSettings,
        OnScanResultListener {

    private var toProcessRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var searchRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var postponedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber() = "09/05"

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.tkNumber}"
        when (vm.taskListLoadingMode) {
            TaskListLoadingMode.Shipment -> connectLiveData(vm.tasksCount.map { getString(R.string.task_shipment_list_count, it) }, topToolbarUiModel.description)
            TaskListLoadingMode.PGE -> connectLiveData(vm.tasksCount.map { getString(R.string.task_pge_list_count, it) }, topToolbarUiModel.description)
            TaskListLoadingMode.Receiving -> connectLiveData(vm.tasksCount.map { getString(R.string.task_receiving_list_count, it) }, topToolbarUiModel.description)
            else -> topToolbarUiModel.description.value = TaskListLoadingMode.None.taskListLoadingModeString
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                bottomToolbarUiModel.uiModelButton5.show(if (it == 1) ButtonDecorationInfo.search else ButtonDecorationInfo.update)
            })
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_5 -> vm.onClickRight()
        }
    }



    override fun getTextTitle(position: Int): String {
        return when (position) {
            TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> getString(R.string.to_process)
            TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> getString(R.string.search)
            TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> getString(R.string.postponed)
            else -> ""
        }
    }

    override fun countTab(): Int = 3

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> configureToProcessPage(container)
            TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> configureSearchPage(container)
            TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> configurePostponedPage(container)
            else -> configureToProcessPage(container)
        }
    }

    private fun configureToProcessPage(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTasksToProcessBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_tasks_to_process,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_tasks,
                            itemId = BR.item,
                            realisation = object : DataBindingAdapter<ItemTileTasksBinding> {
                                override fun onCreate(binding: ItemTileTasksBinding) {
                                }

                                override fun onBind(binding: ItemTileTasksBinding, position: Int) {
                                    toProcessRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }
                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                toProcessRecyclerViewKeyHandler?.let {
                                    if (it.isSelected(position)) {
                                        vm.onClickItemPosition(position)
                                    } else {
                                        it.selectPosition(position)
                                    }
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.getTasksForPage(0),
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = toProcessRecyclerViewKeyHandler?.posInfo?.value
                    )
                    toProcessRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
                }
    }

    private fun configureSearchPage(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTasksSearchBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_tasks_search,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_tasks,
                            itemId = BR.item,
                            realisation = object : DataBindingAdapter<ItemTileTasksBinding> {
                                override fun onCreate(binding: ItemTileTasksBinding) {
                                }

                                override fun onBind(binding: ItemTileTasksBinding, position: Int) {
                                    searchRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }
                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                searchRecyclerViewKeyHandler?.let {
                                    if (it.isSelected(position)) {
                                        vm.onClickItemPosition(position)
                                    } else {
                                        it.selectPosition(position)
                                    }
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.getTasksForPage(1),
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = searchRecyclerViewKeyHandler?.posInfo?.value
                    )
                    searchRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
                }
    }

    private fun configurePostponedPage(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTasksPostponedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_tasks_postponed,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_tasks,
                            itemId = BR.item,
                            realisation = object : DataBindingAdapter<ItemTileTasksBinding> {
                                override fun onCreate(binding: ItemTileTasksBinding) {
                                }

                                override fun onBind(binding: ItemTileTasksBinding, position: Int) {
                                    postponedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }
                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                postponedRecyclerViewKeyHandler?.let {
                                    if (it.isSelected(position)) {
                                        vm.onClickItemPosition(position)
                                    } else {
                                        it.selectPosition(position)
                                    }
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.getTasksForPage(2),
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = postponedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    postponedRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
                }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
        when (vm.selectedPage.value) {
            TaskListViewPages.TASK_LIST_VIEW_PAGE_TO_PROCESS -> vm.requestFocusPageToProcess.value = true
            TaskListViewPages.TASK_LIST_VIEW_PAGE_SEARCH -> vm.requestFocusPageSearch.value = true
            TaskListViewPages.TASK_LIST_VIEW_PAGE_POSTPONED -> vm.requestFocusPagePostponed.value = true
        }
    }

}