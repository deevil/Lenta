package com.lenta.bp7.features.segment_list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp7.BR
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentSegmentListBinding
import com.lenta.bp7.databinding.ItemSegmentBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.bp7.util.MaskWatcher
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
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
        topToolbarUiModel.description.value = getString(R.string.list_of_processed_segments)

        vm.marketNumber.observe(this, Observer<String> { marketNumber ->
            topToolbarUiModel.title.value = getString(R.string.title_store_number, marketNumber)
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save, enabled = false)

        viewLifecycleOwner.apply {
            connectLiveData(source = vm.saveButtonEnabled, target = bottomToolbarUiModel.uiModelButton5.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickSave()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
        //initSegmentNumberField()
    }

    private fun initRvConfig() {
        binding?.rvConfig = DataBindingRecyclerViewConfig<ItemSegmentBinding>(
                layoutId = R.layout.item_segment,
                itemId = BR.segment
        )
    }

    private fun initSegmentNumberField() {
        binding?.etSegmentNumber?.addTextChangedListener(MaskWatcher("###-###"))
    }
}
