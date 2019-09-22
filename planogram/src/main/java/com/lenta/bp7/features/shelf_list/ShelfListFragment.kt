package com.lenta.bp7.features.shelf_list

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import com.lenta.bp7.BR
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentShelfListBinding
import com.lenta.bp7.databinding.ItemShelfBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.*

class ShelfListFragment : CoreFragment<FragmentShelfListBinding, ShelfListViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener {

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_shelf_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("10")

    override fun getViewModel(): ShelfListViewModel {
        provideViewModel(ShelfListViewModel::class.java).let {
            getAppComponent()?.inject(it)

            it.marketIp.value = context!!.getDeviceIp()
            it.terminalId.value = context!!.getDeviceId()

            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.description_list_of_processed_selves)

        viewLifecycleOwner.apply {
            vm.segmentNumber.observe(this, Observer { segmentNumber ->
                topToolbarUiModel.title.value = getString(R.string.title_segment_number, segmentNumber)
            })
        }
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.deleteSegment)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.deleteShelf, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        connectLiveData(vm.deleteSegmentButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton2.enabled)
        connectLiveData(vm.deleteShelfButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
        connectLiveData(vm.applyButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    override fun onResume() {
        super.onResume()
        vm.updateShelfList()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickBack()
            R.id.b_2 -> vm.onClickDeleteSegment()
            R.id.b_3 -> vm.onClickDeleteShelf()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.selectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_shelf,
                    itemId = BR.shelf,
                    realisation = object : DataBindingAdapter<ItemShelfBinding> {
                        override fun onCreate(binding: ItemShelfBinding) {
                        }

                        override fun onBind(binding: ItemShelfBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
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
                    items = vm.shelves,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
            )
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return false
    }
}
