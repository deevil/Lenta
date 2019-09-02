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
import com.lenta.bp14.data.TaskListTab
import com.lenta.bp14.databinding.FragmentTaskListBinding
import com.lenta.bp14.databinding.LayoutTaskListBinding
import com.lenta.bp14.databinding.LayoutTaskListFilteredBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener {


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
        topToolbarUiModel.description.value = getString(R.string.task_list)

        vm.marketNumber.observe(this, Observer<String> { marketNumber ->
            topToolbarUiModel.title.value = getString(R.string.title_market_number, marketNumber)
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.filter)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.update)

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == TaskListTab.PROCESSING.position) {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
                    connectLiveData(vm.saveButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
                } else {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.filter)
                }
            })
        }

        connectLiveData(vm.thirdButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
        connectLiveData(vm.fourthButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton4.visibility)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickBack()
            R.id.b_3 -> vm.onClickFilter()
            R.id.b_4 -> vm.onClickUpdate()
            R.id.b_5 -> {
                if (vm.selectedPage.value == TaskListTab.PROCESSING.position) {
                    vm.onClickSave()
                } else vm.onClickFilter()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this

    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutTaskListBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_task_list,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig<LayoutTaskListBinding>(
                                layoutId = R.layout.item_tile_tasks,
                                itemId = BR.vm,
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    vm.onClickUnprocessedTask(position)
                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        layoutBinding.root
                    }
        } else {
            DataBindingUtil
                    .inflate<LayoutTaskListFilteredBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_task_list_filtered,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig<LayoutTaskListBinding>(
                                layoutId = R.layout.item_tile_tasks,
                                itemId = BR.vm,
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    vm.onClickProcessedTask(position)
                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        layoutBinding.root
                    }
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.processing)
            1 -> getString(R.string.search)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return false
    }

}
