package com.lenta.bp18.features.sync

import com.lenta.bp18.R
import com.lenta.bp18.databinding.FragmentSyncBinding
import com.lenta.bp18.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.provideViewModel

class SyncFragment : CoreFragment<FragmentSyncBinding, SyncViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_sync

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): SyncViewModel {
        provideViewModel(SyncViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.deviceIp.value = context!!.getDeviceId()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.load_data)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.hide()
    }

    companion object{
        const val SCREEN_NUMBER = "101" //Потом поменять
    }

}