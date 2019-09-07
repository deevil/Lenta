package com.lenta.bp14.features.work_list.good_sales

import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp14.R
import com.lenta.bp14.data.model.Good
import com.lenta.bp14.databinding.FragmentGoodSalesBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodSalesFragment : CoreFragment<FragmentGoodSalesBinding, GoodSalesViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_sales

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("22")

    override fun getViewModel(): GoodSalesViewModel {
        provideViewModel(GoodSalesViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.details_of_goods)

        vm.good.observe(this, Observer<Good> { good ->
            topToolbarUiModel.title.value = good.getFormattedMaterialWithName()
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.update)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickUpdate()
        }
    }

}
