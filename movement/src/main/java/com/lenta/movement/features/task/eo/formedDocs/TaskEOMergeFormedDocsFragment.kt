package com.lenta.movement.features.task.eo.formedDocs

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.os.bundleOf
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskEoMergeDocsBinding
import com.lenta.movement.databinding.LayoutItemDocsListBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.requests.network.models.documentsToPrint.DocumentsToPrintDocument
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.provideViewModel
import kotlinx.android.synthetic.main.fragment_task_eo_merge_docs.*

class TaskEOMergeFormedDocsFragment :
        CoreFragment<FragmentTaskEoMergeDocsBinding, TaskEOMergeFormedDocsViewModel>(),
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    private var docsListRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private val docList by unsafeLazy {
        arguments?.getParcelableArrayList<DocumentsToPrintDocument>(DOC_LIST_KEY)
    }

    override fun getLayoutId() = R.layout.fragment_task_eo_merge_docs

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskEOMergeFormedDocsViewModel {
        provideViewModel(TaskEOMergeFormedDocsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            if (docList != null) {
                vm.docList.value = docList
            }
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onClickSelectionListener = View.OnClickListener { clickListener ->
            val itemPosition = clickListener.tag as Int
            vm.docsSelectionHelper.revert(
                    position = itemPosition
            )
            this.recyclerView.adapter?.notifyItemChanged(itemPosition)
        }

        binding?.rvConfig = DataBindingRecyclerViewConfig(
                layoutId = R.layout.layout_item_docs_list,
                itemId = BR.item,
                realisation = object : DataBindingAdapter<LayoutItemDocsListBinding> {
                    override fun onCreate(binding: LayoutItemDocsListBinding) = Unit

                    override fun onBind(binding: LayoutItemDocsListBinding, position: Int) {
                        binding.counterText.tag = position
                        binding.counterText.setOnClickListener(onClickSelectionListener)
                        vm.docsItemList.value?.let { list ->
                            binding.item = list[position]
                        }
                        binding.selectedToPrint = vm.docsSelectionHelper.isSelected(position)
                        docsListRecyclerViewKeyHandler?.let { handler ->
                            binding.root.isSelected = handler.isSelected(position)
                        }
                    }

                },
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    docsListRecyclerViewKeyHandler?.let { handler ->
                        if (!handler.isSelected(position)) {
                            handler.selectPosition(position)
                        }
                    }
                }
        )

        binding?.recyclerView?.let { recyclerView ->
            binding?.lifecycleOwner?.let { lifecycleOwner ->
                docsListRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                        rv = recyclerView,
                        items = vm.docsItemList,
                        lifecycleOwner = lifecycleOwner,
                        initPosInfo = docsListRecyclerViewKeyHandler?.posInfo?.value
                )
            }
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.task_eo_merge_docs_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.print)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onPrintBtnClick()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "10/06"
        private const val DOC_LIST_KEY = "DOCS_LIST_KEY"

        fun newInstance(docsList: List<DocumentsToPrintDocument>): TaskEOMergeFormedDocsFragment {
            return TaskEOMergeFormedDocsFragment().apply {
                arguments = bundleOf(
                        DOC_LIST_KEY to docsList
                )
            }
        }
    }
}

