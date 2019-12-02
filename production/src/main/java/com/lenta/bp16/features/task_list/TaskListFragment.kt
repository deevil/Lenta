package com.lenta.bp16.features.task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentTaskListBinding
import com.lenta.bp16.databinding.ItemTaskListBinding
import com.lenta.bp16.databinding.LayoutTaskListBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener {

    private var processingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("5")

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)

            it.deviceIp.value = context!!.getDeviceIp()

            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.work_with_pu_quantity, vm.tasks.value?.size ?: 0)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_5 -> vm.onClickRefresh()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil.inflate<LayoutTaskListBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_task_list,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                        layoutId = R.layout.item_task_list,
                        itemId = BR.item,
                        realisation = object : DataBindingAdapter<ItemTaskListBinding> {
                            override fun onCreate(binding: ItemTaskListBinding) {
                            }

                            override fun onBind(binding: ItemTaskListBinding, position: Int) {
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

        DataBindingUtil.inflate<LayoutTaskListBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_list,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_task_list,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTaskListBinding> {
                        override fun onCreate(binding: ItemTaskListBinding) {
                        }

                        override fun onBind(binding: ItemTaskListBinding, position: Int) {
                            processedRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    },
                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        processedRecyclerViewKeyHandler?.let {
                            if (it.isSelected(position)) {
                                vm.onClickItemPosition(position)
                            } else {
                                it.selectPosition(position)
                            }
                        }
                    })

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            processedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.processed,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.processing)
            1 -> getString(R.string.processed)
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

}
