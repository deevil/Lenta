package com.lenta.bp7.features.segment_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class SegmentListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo

    val enabledSaveBtn: LiveData<Boolean> = MutableLiveData()

    var segmentNumber: LiveData<String> = MutableLiveData()
    var storeNumber: LiveData<String> = MutableLiveData()

    fun getTitle(storeNumberPrefix: String): String? {
        return storeNumberPrefix + sessionInfo.market
    }

    fun onClickSave() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
