package com.lenta.bp10.features.good_information.sets.component

import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentComponentBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class ComponentFragment : CoreFragment<FragmentComponentBinding, ComponentViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_component

    override fun getPageNumber(): String = "10/11"

    override fun getViewModel(): ComponentViewModel {
        provideViewModel(ComponentViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
