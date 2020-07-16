package com.lenta.movement.features.task.eo.eo_insides

import android.os.Bundle
import android.view.View
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskEoMergeEoInsidesBinding
import com.lenta.movement.databinding.LayoutItemSimpleBinding
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

/** Список вложенных ЕО (при нажатии на элемент в списке ГЕ на экране Объединение ЕО (TaskEOMergeFragment)*/
class TaskEOMergeEOInsidesFragment : CoreFragment<FragmentTaskEoMergeEoInsidesBinding, TaskEOMergeEOInsidesViewModel>(),
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnKeyDownListener {

    private var eo : ProcessingUnit? by state(null)

    override fun getLayoutId() = R.layout.fragment_task_eo_merge_eo_insides

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskEOMergeEOInsidesViewModel {
        provideViewModel(TaskEOMergeEOInsidesViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            if (eo != null) {
                vm.eo.value = eo
            }
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onClickSelectionListener = View.OnClickListener { clickListener ->
            clickListener?.let {
                val position = it.tag as Int
                vm.selectionsHelper.revert(position = position)
                binding?.recyclerView?.adapter?.notifyItemChanged(position)
            }
        }

        binding?.apply {
            val vm = this@TaskEOMergeEOInsidesFragment.vm
            rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.layout_item_simple,
                    itemId = BR.item,
                    onAdapterItemBind = { binding: LayoutItemSimpleBinding, position ->
                        binding.tvCounter.tag = position
                        binding.tvCounter.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                        onAdapterBindHandler(
                                bindItem = binding,
                                position = position)
                    },
                    onAdapterItemClicked = { position ->
                        recyclerViewKeyHandler?.let {
                            if (it.isSelected(position).not()) {
                                it.selectPosition(position)
                            }
                        }
                    }
            )

            recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = recyclerView,
                    items = this@TaskEOMergeEOInsidesFragment.vm.goodsItemList,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value
            )
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_eo_inside)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.exclude)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.isExcludeBtnEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> onBackPressed()
        }
    }


    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        recyclerViewKeyHandler?.let {
            if (it.onKeyDown(keyCode)) {
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
        private const val PAGE_NUMBER = "10/06"

        fun newInstance(inputEo : ProcessingUnit): TaskEOMergeEOInsidesFragment {
            return TaskEOMergeEOInsidesFragment().apply {
                this.eo = inputEo
            }
        }
    }
}

