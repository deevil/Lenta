package com.lenta.bp12.features.open_task.task_list

import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentTaskListBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp12.BR
import com.lenta.bp12.databinding.ItemTaskListTaskBinding
import com.lenta.bp12.databinding.LayoutTaskListProcessingBinding
import com.lenta.bp12.databinding.LayoutTaskListSearchBinding
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings {

    private var processingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var searchRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("25")

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title
        topToolbarUiModel.description.value = getString(R.string.task_list)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)

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
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_5 -> if (vm.selectedPage.value == 0) vm.onClickUpdate() else vm.onClickFilter()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> initTaskListProcessing(container)
            1 -> initTaskListSearch(container)
            else -> View(context)
        }
    }

    private fun initTaskListProcessing(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_list_processing,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_task_list_task,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTaskListTaskBinding> {
                        override fun onCreate(binding: ItemTaskListTaskBinding) {
                        }

                        override fun onBind(binding: ItemTaskListTaskBinding, position: Int) {
                            processingRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    },
                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        processingRecyclerViewKeyHandler?.let {
                            if (it.isSelected(position)) {
                                vm.onClickItemPosition(position)
                            } else {
                                it.selectPosition(position)
                            }
                        }
                    })

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            processingRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.processing,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = processingRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    private fun initTaskListSearch(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_list_search,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_task_list_task,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTaskListTaskBinding> {
                        override fun onCreate(binding: ItemTaskListTaskBinding) {
                        }

                        override fun onBind(binding: ItemTaskListTaskBinding, position: Int) {
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
                    })

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            searchRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.search,
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

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onResume() {
        super.onResume()
        vm.onClickUpdate()
    }
}