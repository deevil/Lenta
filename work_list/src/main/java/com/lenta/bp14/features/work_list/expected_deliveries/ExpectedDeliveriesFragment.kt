package com.lenta.bp14.features.work_list.expected_deliveries

import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentExpectedDeliveriesBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class ExpectedDeliveriesFragment : CoreFragment<FragmentExpectedDeliveriesBinding, ExpectedDeliveriesViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_expected_deliveries

    override fun getPageNumber(): String = "14/21"

    override fun getViewModel(): ExpectedDeliveriesViewModel {
        provideViewModel(ExpectedDeliveriesViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
