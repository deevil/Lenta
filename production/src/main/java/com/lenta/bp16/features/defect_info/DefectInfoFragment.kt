package com.lenta.bp16.features.defect_info

import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentDefectInfoBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class DefectInfoFragment : CoreFragment<FragmentDefectInfoBinding, DefectInfoViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_defect_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("07")

    override fun getViewModel(): DefectInfoViewModel {
        provideViewModel(DefectInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
