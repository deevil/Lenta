package com.lenta.bp7.features.segment_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.account.IPlanogramSessionInfo
import com.lenta.bp7.data.model.Segment
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class SegmentListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: IPlanogramSessionInfo
    @Inject
    lateinit var database: IDatabaseRepo

    val segments: MutableLiveData<List<Segment>> = MutableLiveData()

    val segmentNumber: MutableLiveData<String> = MutableLiveData("")
    val saveButtonEnabled: MutableLiveData<Boolean> = segmentNumber.map { it?.length == 7 }

    init {
        viewModelScope.launch {
            segments.value = sessionInfo.checkStoreData.segments
        }
    }

    fun getStoreNumber(): String? {
        return sessionInfo.marketNumber
    }

    fun onClickSave() {
        sessionInfo.checkStoreData.addSegment(sessionInfo.marketNumber, segmentNumber.value)
        navigator.openShelfListScreen()
    }
}
