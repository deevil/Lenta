package com.lenta.bp7.features.segment_list

import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentSegmentListBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SegmentListFragment : CoreFragment<FragmentSegmentListBinding, SegmentListViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_segment_list

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): SegmentListViewModel {
        provideViewModel(SegmentListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
