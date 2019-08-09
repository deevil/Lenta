package com.lenta.bp9.features.loading.fast

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentFastDataLoadingBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class FastDataLoadingFragment : CoreFragment<FragmentFastDataLoadingBinding, FastDataLoadingViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_fast_data_loading

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): FastDataLoadingViewModel {
        provideViewModel(FastDataLoadingViewModel::class.java).let {
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
