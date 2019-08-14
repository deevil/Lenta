package com.lenta.bp14.features.list_of_differences

import android.os.Bundle
import android.view.View
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentListOfDifferencesBinding
import com.lenta.bp14.databinding.ItemTileDifferenceBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class ListOfDifferencesFragment : CoreFragment<FragmentListOfDifferencesBinding, ListOfDifferencesViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_list_of_differences

    override fun getPageNumber(): String = "14/24"

    override fun getViewModel(): ListOfDifferencesViewModel {
        provideViewModel(ListOfDifferencesViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_differences)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.skip)

        connectLiveData(vm.enabledMissingButton, bottomToolbarUiModel.uiModelButton4.enabled)

    }


    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickSkip()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvConfig = DataBindingRecyclerViewConfig<ItemTileDifferenceBinding>(
                layoutId = R.layout.item_tile_difference,
                itemId = BR.vm
        )
    }


}
