package com.lenta.bp7.features.shelf_list

import android.view.View
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentShelfListBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class ShelfListFragment : CoreFragment<FragmentShelfListBinding, ShelfListViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_shelf_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("10")

    override fun getViewModel(): ShelfListViewModel {
        provideViewModel(ShelfListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getString(R.string.title_segment_number, vm.getSegmentNumber())
        topToolbarUiModel.description.value = getString(R.string.list_of_processed_selves)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = true)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            /*R.id.b_3 -> vm.onClickDelete()*/
            R.id.b_5 -> vm.onClickApply()
        }
    }
}
