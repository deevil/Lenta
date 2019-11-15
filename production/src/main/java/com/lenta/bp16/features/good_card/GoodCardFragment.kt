package com.lenta.bp16.features.good_card

import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodCardBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodCardFragment : CoreFragment<FragmentGoodCardBinding, GoodCardViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_good_card

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("9")

    override fun getViewModel(): GoodCardViewModel {
        provideViewModel(GoodCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
