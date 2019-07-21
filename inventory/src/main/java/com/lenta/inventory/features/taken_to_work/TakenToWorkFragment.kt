package com.lenta.inventory.features.taken_to_work

import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentTakenToWorkBinding
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class TakenToWorkFragment : CoreFragment<FragmentTakenToWorkBinding, TakenToWorkViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_taken_to_work

    override fun getPageNumber(): String = "11/98"

    override fun getViewModel(): TakenToWorkViewModel {
        provideViewModel(TakenToWorkViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.taken_to_work)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }


    companion object {
        fun create(): TakenToWorkFragment {
            return TakenToWorkFragment()
        }
    }


}
