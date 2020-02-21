package com.lenta.bp12.features.basket_good_list

import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentBasketGoodListBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class BasketGoodListFragment : CoreFragment<FragmentBasketGoodListBinding, BasketGoodListViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_basket_good_list

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): BasketGoodListViewModel {
        provideViewModel(BasketGoodListViewModel::class.java).let {
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
