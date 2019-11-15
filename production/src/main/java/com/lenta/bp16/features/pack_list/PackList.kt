package com.lenta.bp16.features.pack_list

import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentPackListBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class PackList : CoreFragment<FragmentPackListBinding, PackListViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_pack_list

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): PackListViewModel {
        provideViewModel(PackListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
