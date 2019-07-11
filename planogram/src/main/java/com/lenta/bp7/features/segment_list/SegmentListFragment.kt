package com.lenta.bp7.features.segment_list

import android.os.Bundle
import android.view.View
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentSegmentListBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class SegmentListFragment : CoreFragment<FragmentSegmentListBinding, SegmentListViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_segment_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("08")

    override fun getViewModel(): SegmentListViewModel {
        provideViewModel(SegmentListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.cleanAll()
        topToolbarUiModel.title.value = getString(R.string.title_store_number, vm.getStoreNumber())
        topToolbarUiModel.description.value = getString(R.string.list_of_processed_segments)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save, enabled = true)
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickSave()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initSegmentNumberField()
    }

    private fun initSegmentNumberField() {

    }
}
