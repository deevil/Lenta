package com.lenta.bp16.features.processing_unit_list

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentProcessingUnitListBinding
import com.lenta.bp16.databinding.ItemProcessingUnitBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
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

class ProcessingUnitListFragment : CoreFragment<FragmentProcessingUnitListBinding, ProcessingUnitListViewModel>(),
        OnBackPresserListener, ToolbarButtonsClickListener, OnKeyDownListener {

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_processing_unit_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): ProcessingUnitListViewModel {
        provideViewModel(ProcessingUnitListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_list)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)

        connectLiveData(vm.completeEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_processing_unit,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemProcessingUnitBinding> {
                        override fun onCreate(binding: ItemProcessingUnitBinding) {
                        }

                        override fun onBind(binding: ItemProcessingUnitBinding, position: Int) {
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
                    items = vm.goods,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickPositionFunc = vm::onClickItemPosition
            )
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        return recyclerViewKeyHandler?.onKeyDown(keyCode) ?: false
    }

    override fun onDestroyView() {
        recyclerViewKeyHandler?.onClickPositionFunc = null
        super.onDestroyView()
    }

    companion object {
        const val SCREEN_NUMBER = "61"
    }

}
