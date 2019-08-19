package com.lenta.inventory.features.goods_information.excise_alco.party_signs

import android.os.Bundle
import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentPartySignsBinding
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.DateInputMask
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class PartySignsFragment : CoreFragment<FragmentPartySignsBinding, PartySignsViewModel>(), ToolbarButtonsClickListener {

    companion object {
        fun create(title: String, manufacturers: List<String>, stampLength: Int): PartySignsFragment {
            PartySignsFragment().let {
                it.title = title
                it.manufacturers = manufacturers
                it.stampLength = stampLength
                return it
            }
        }
    }

    private var title by state<String?>(null)

    private var manufacturers by state<List<String>?>(null)

    private var stampLength by state<Int?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_party_signs

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): PartySignsViewModel {
        provideViewModel(PartySignsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.manufacturersName.value = manufacturers
            vm.stampLength.value = stampLength
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DateInputMask(binding?.etBottlingDate!!).listen()
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        topToolbarUiModel.title.value = title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.enabledNextBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }

}
