package com.lenta.bp10.features.good_information.general

import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentGoodInfoBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, GoodInfoViewModel>(), ToolbarButtonsClickListener, OnBackPresserListener {

    private lateinit var productInfo: ProductInfo

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String = "10/07"

    override fun getViewModel(): GoodInfoViewModel {
        provideViewModel(GoodInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.setProductInfo(productInfo)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_info)
        topToolbarUiModel.title.value = "${productInfo.getMaterialLastSix()} ${productInfo.description}"

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getTopToolBarUIModel()?.let {
            it.title.value = getString(R.string.app_title)
        }
    }


    companion object {
        fun create(productInfo: ProductInfo): GoodInfoFragment {
            GoodInfoFragment().let {
                it.productInfo = productInfo
                return it
            }
        }

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_4 -> vm.onClickAdd()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

}
