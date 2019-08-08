package com.lenta.bp9.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentTaskListBinding
import com.lenta.bp9.databinding.ItemTileTasksBinding
import com.lenta.bp9.databinding.LayoutTasksBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(), ToolbarButtonsClickListener, OnKeyDownListener, ViewPagerSettings, PageSelectionListener {

    private var toProcessRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var searchRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var postponedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.tkNumber}"
        connectLiveData(vm.tasksCount.map { getString(R.string.task_list_count, it) }, topToolbarUiModel.description)

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_5 -> vm.onClickUpdate()
        }
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        return false
    }


    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
        vm.onPageSelected(position)
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.to_process)
            1 -> getString(R.string.search)
            2 -> getString(R.string.postponed)
            else -> ""
        }
    }

    override fun countTab(): Int = 3

    private fun rvKeyHandlerForPage(page: Int): RecyclerViewKeyHandler<*>?
    {
        return when (page) {
            0 -> toProcessRecyclerViewKeyHandler
            1 -> searchRecyclerViewKeyHandler
            2 -> postponedRecyclerViewKeyHandler
            else -> null
        }
    }

    private fun setRecycleViewKeyHandlerForPage(page: Int, rvKeyHandler: RecyclerViewKeyHandler<*>?) {
        when (page) {
            0 -> toProcessRecyclerViewKeyHandler = rvKeyHandler
            1 -> searchRecyclerViewKeyHandler = rvKeyHandler
            2 -> postponedRecyclerViewKeyHandler = rvKeyHandler
        }
    }

    private fun dataForPage(page: Int) : LiveData<List<TaskItemVm>> {
        return when (page) {
            0 -> vm.tasks
            1 -> vm.tasks
            2 -> vm.tasks
            else -> MutableLiveData(emptyList())
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {

        DataBindingUtil
                .inflate<LayoutTasksBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_tasks,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_tasks,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileTasksBinding> {
                                override fun onCreate(binding: ItemTileTasksBinding) {
                                }

                                override fun onBind(binding: ItemTileTasksBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    rvKeyHandlerForPage(position)?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }
                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                rvKeyHandlerForPage(position)?.let {
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
                            items = dataForPage(position),
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = rvKeyHandlerForPage(position)?.posInfo?.value
                    )
                    setRecycleViewKeyHandlerForPage(position, rvKeyHandler)
                    return layoutBinding.root
                }
    }

}