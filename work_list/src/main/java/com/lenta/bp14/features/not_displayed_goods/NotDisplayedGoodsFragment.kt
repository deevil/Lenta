package com.lenta.bp14.features.not_displayed_goods

import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentNotDisplayedGoodsBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener

class NotDisplayedGoodsFragment : CoreFragment<FragmentNotDisplayedGoodsBinding, NotDisplayedGoodsViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_not_displayed_goods

    override fun getPageNumber(): String {
        return "14/74"
    }

    override fun getViewModel(): NotDisplayedGoodsViewModel {
        provideViewModel(NotDisplayedGoodsViewModel::class.java).let {
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
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String {
        return getString(
                when (if (countTab() < 3) position + 1 else position) {
                    0 -> R.string.not_processed
                    1 -> R.string.processed
                    else -> R.string.search
                }
        )
    }

    override fun countTab(): Int {
        return 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickSave()
        }
    }


}
