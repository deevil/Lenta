package com.lenta.bp14.features.list_of_differences

import android.os.Bundle
import android.view.View
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentListOfDifferencesBinding
import com.lenta.bp14.databinding.ItemSimpleGoodSelectableBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ListOfDifferencesFragment : CoreFragment<FragmentListOfDifferencesBinding, ListOfDifferencesViewModel>(),
        ToolbarButtonsClickListener {

    private var onClickSkipCallbackID: Int? by state(null)

    override fun getLayoutId(): Int = R.layout.fragment_list_of_differences

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(PAGE_NUMBER)

    override fun getViewModel(): ListOfDifferencesViewModel {
        provideViewModel(ListOfDifferencesViewModel::class.java).let {
            getAppComponent()?.inject(it)
            requireNotNull(onClickSkipCallbackID)
            it.onClickSkipCallbackID = onClickSkipCallbackID
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.list_of_differences)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.skip)

        connectLiveData(vm.missingButtonEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickSkip()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.selectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_simple_good_selectable,
                    itemId = BR.vm,
                    onItemBind = { binding: ItemSimpleGoodSelectableBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                        onAdapterBindHandler(binding, position)
                    },
                    recyclerView = layoutBinding.rv,
                    items = vm.goods,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    companion object {
        private const val PAGE_NUMBER = "36"

        fun newInstance(onClickSkipCallbackID: Int): ListOfDifferencesFragment {
            return ListOfDifferencesFragment().apply {
                this.onClickSkipCallbackID = onClickSkipCallbackID
            }
        }
    }
}
