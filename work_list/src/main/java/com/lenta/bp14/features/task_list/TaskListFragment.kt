package com.lenta.bp14.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.models.data.TaskListTab
import com.lenta.bp14.databinding.*
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener {

    private var processingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var searchRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("30")

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
            R.id.b_5 -> {
                if (vm.selectedPage.value == TaskListTab.PROCESSING.position) {
                    vm.onClickUpdate()
                } else vm.onClickFilter()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this

    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutTaskListProcessingBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_task_list_processing,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemTaskStatusGoodsBinding>(
                                layoutId = R.layout.item_task_status_goods,
                                itemId = BR.task,
                                realisation = object : DataBindingAdapter<ItemTaskStatusGoodsBinding> {
                                    override fun onCreate(binding: ItemTaskStatusGoodsBinding) {
                                    }

                                    override fun onBind(binding: ItemTaskStatusGoodsBinding, position: Int) {
                                        processingRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }
                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    processingRecyclerViewKeyHandler?.let {
                                        if (it.isSelected(position)) {
                                            vm.onClickProcessingTask(position)
                                        } else {
                                            it.selectPosition(position)
                                        }
                                    }
                                })

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        processingRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.processingTasks,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = processingRecyclerViewKeyHandler?.posInfo?.value
                        )

                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutTaskListSearchBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_list_search,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_task_status_goods,
                            itemId = BR.task,
                            realisation = object : DataBindingAdapter<ItemTaskStatusGoodsBinding> {
                                override fun onCreate(binding: ItemTaskStatusGoodsBinding) {
                                }

                                override fun onBind(binding: ItemTaskStatusGoodsBinding, position: Int) {
                                    searchRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }
                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                searchRecyclerViewKeyHandler?.let {
                                    if (it.isSelected(position)) {
                                        vm.onClickSearchTask(position)
                                    } else {
                                        it.selectPosition(position)
                                    }
                                }
                            })

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    searchRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.searchTasks,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = searchRecyclerViewKeyHandler?.posInfo?.value
                    )

                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.processing)
            1 -> getString(R.string.search)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int = 2

    override fun onBackPressed(): Boolean {
        vm.onClickMenu()
        return false
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

}
