package com.lenta.movement.features.selectmarket

import android.view.View
import androidx.lifecycle.Observer
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentSelectMarketBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.ImageButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class SelectMarketFragment : CoreFragment<FragmentSelectMarketBinding, SelectMarketViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_select_market

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): SelectMarketViewModel {
        provideViewModel(SelectMarketViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }
    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.tk_selection)
        topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel
                .uiModelButton5.show(ButtonDecorationInfo.next)
        vm.selectedPosition.observe(viewLifecycleOwner, Observer {
            bottomToolbarUiModel
                    .uiModelButton5.requestFocus()
        })
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }

    companion object {
        private const val PAGE_NUMBER = "13/03"
    }
}