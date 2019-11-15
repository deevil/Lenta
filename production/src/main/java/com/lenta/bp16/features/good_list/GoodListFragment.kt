package com.lenta.bp16.features.good_list

import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodListBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodListFragment : CoreFragment<FragmentGoodListBinding, GoodListViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("6")

    override fun getViewModel(): GoodListViewModel {
        provideViewModel(GoodListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_list)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
