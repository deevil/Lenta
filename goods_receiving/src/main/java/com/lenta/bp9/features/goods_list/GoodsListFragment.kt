package com.lenta.bp9.features.goods_list

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentGoodsListBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.PageSelectionListener

class GoodsListFragment : CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ViewPagerSettings,
        OnScanResultListener,
        PageSelectionListener {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list

    override fun getPageNumber(): String = "09/15"

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.batchsProducts)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.counted else R.string.without_barcode)

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}
