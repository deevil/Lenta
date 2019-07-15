package com.lenta.bp7.features.good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class GoodListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo

    private val segmentNumber: MutableLiveData<String> = MutableLiveData("123-456")
    private val shelfNumber: MutableLiveData<String> = MutableLiveData("4")

    fun getTitle(pattern: String): String? {
        return String.format(pattern, segmentNumber.value, shelfNumber.value)
    }

    fun onClickApply() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}