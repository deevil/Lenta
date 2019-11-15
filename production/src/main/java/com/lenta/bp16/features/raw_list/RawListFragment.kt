package com.lenta.bp16.features.raw_list

import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentRawListBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class RawListFragment : CoreFragment<FragmentRawListBinding, RawListViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_raw_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("7")

    override fun getViewModel(): RawListViewModel {
        provideViewModel(RawListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.pu_number, vm.puNumber)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
