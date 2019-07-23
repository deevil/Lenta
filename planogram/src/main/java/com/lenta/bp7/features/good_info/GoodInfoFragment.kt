package com.lenta.bp7.features.good_info

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.lenta.bp7.R
import com.lenta.bp7.data.model.Good
import com.lenta.bp7.databinding.FragmentGoodInfoBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, GoodInfoViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("31")

    override fun getViewModel(): GoodInfoViewModel {
        provideViewModel(GoodInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.description_info_about_good)

        vm.good.observe(this, Observer<Good> { good ->
            topToolbarUiModel.title.value = getString(R.string.title_good_sap_name, good.getFormattedSapCode(), good.name)
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing, enabled = true)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.apply {
            connectLiveData(vm.missingButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton4.enabled)
            connectLiveData(vm.applyButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return false
    }
}
