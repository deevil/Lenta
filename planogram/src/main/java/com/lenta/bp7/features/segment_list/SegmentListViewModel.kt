package com.lenta.bp7.features.segment_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.Segment
import com.lenta.bp7.data.model.SegmentStatus
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
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

    private val storeNumber: MutableLiveData<String> = MutableLiveData("0000")
    val segments: MutableLiveData<List<Segment>> = MutableLiveData()

    init {
        viewModelScope.launch {
            storeNumber.value = sessionInfo.market

            segments.value = listOf(
                    Segment(id = 1, number = "126-652", storeNumber = storeNumber.value, status = SegmentStatus.DELETED),
                    Segment(id = 2, number = "104-236", storeNumber = storeNumber.value, status = SegmentStatus.PROCESSED),
                    Segment(id = 3, number = "008-397", storeNumber = storeNumber.value, status = SegmentStatus.UNFINISHED)
            )
        }
    }

    fun getStoreNumber(): String? {
        return storeNumber.value
    }

    fun onClickSave() {
        // Заглушка
        navigator.openShelfListScreen()
    }
}
