package com.lenta.bp7.features.segment_list

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.lenta.bp7.BR
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentSegmentListBinding
import com.lenta.bp7.databinding.ItemSegmentBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SegmentListFragment : CoreFragment<FragmentSegmentListBinding, SegmentListViewModel>(),
        ToolbarButtonsClickListener {

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_segment_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("08")

    override fun getViewModel(): SegmentListViewModel {
        provideViewModel(SegmentListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.description_list_of_processed_segments)

        vm.marketNumber.observe(this, Observer<String> { marketNumber ->
            topToolbarUiModel.title.value = getString(R.string.title_store_number, marketNumber)
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete, enabled = false)

        viewLifecycleOwner.apply {
            connectLiveData(source = vm.completeButtonEnabled, target = bottomToolbarUiModel.uiModelButton5.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
        initSegmentNumberField()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_segment,
                    itemId = BR.segment,
                    realisation = object : DataBindingAdapter<ItemSegmentBinding> {
                        override fun onCreate(binding: ItemSegmentBinding) {
                        }

                        override fun onBind(binding: ItemSegmentBinding, position: Int) {
                            recyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    },
                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        recyclerViewKeyHandler?.let {
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
            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.segments,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
            )
        }
    }

    private fun initSegmentNumberField() {
        binding?.etSegmentNumber?.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    vm.createSegment()
                    true
                }
                else -> false
            }
        }
    }
}
