package com.lenta.shared.features.section_info

import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentSectionInfoBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class SectionInfoFragment : CoreFragment<FragmentSectionInfoBinding, SectionInfoViewModel>() {

    var sectionNumber by state("")

    override fun getLayoutId(): Int = R.layout.fragment_section_info

    override fun getPageNumber() : String? = null

    override fun getViewModel(): SectionInfoViewModel {
        provideViewModel(SectionInfoViewModel::class.java).let {
            coreComponent.inject(it)
            it.sectionNumber.value = sectionNumber
            it.message = getString(R.string.section, sectionNumber)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
    }


    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    companion object {
        fun create(sectionNumber: String): SectionInfoFragment {
            return SectionInfoFragment().apply {
                this.sectionNumber = sectionNumber
            }
        }
    }


}
