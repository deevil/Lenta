package com.lenta.bp16.features.reprint_label

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentReprintLabelBinding
import com.lenta.bp16.databinding.ItemReprintLabelBinding
import com.lenta.bp16.platform.extention.getAppComponent
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

class ReprintLabelFragment : CoreFragment<FragmentReprintLabelBinding, ReprintLabelViewModel>(),
        ToolbarButtonsClickListener {

    companion object {
        const val SCREEN_NUMBER = "07"
    }

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_reprint_label

    override fun getPageNumber(): String ? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): ReprintLabelViewModel {
        provideViewModel(ReprintLabelViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title
        topToolbarUiModel.description.value = getString(R.string.reprint_labels)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.print)

        connectLiveData(vm.printEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickPrint()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.selectionsHelper.let { selectionsHelper ->
                        selectionsHelper.selectedPositions.value?.let { positions ->
                            if (positions.isNotEmpty()) {
                                if (positions.contains(position)) {
                                    selectionsHelper.revert(position)
                                } else {
                                    selectionsHelper.clearPositions()
                                    selectionsHelper.add(position)
                                }
                            } else {
                                selectionsHelper.add(position)
                            }
                        }
                    }

                    layoutBinding.rv.adapter?.notifyDataSetChanged()
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_reprint_label,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemReprintLabelBinding> {
                        override fun onCreate(binding: ItemReprintLabelBinding) {
                        }

                        override fun onBind(binding: ItemReprintLabelBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                        }
                    })

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.labels,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
            )
        }
    }

}
