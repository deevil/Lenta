package com.lenta.bp7.features.segment_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckStoreData
import com.lenta.bp7.data.model.Segment
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
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
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkStoreData: CheckStoreData

    val segments: MutableLiveData<List<Segment>> = MutableLiveData()
    private val unfinishedSegment: MutableLiveData<Boolean> = MutableLiveData()

    val completeButtonEnabled: MutableLiveData<Boolean> = unfinishedSegment.combineLatest(segments)
            .map { !it?.first!! || it.second!!.size > 1 }

    val marketNumber: MutableLiveData<String> = MutableLiveData("")
    val segmentNumber: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch {
            unfinishedSegment.value = checkStoreData.isExistUnfinishedSegment
            marketNumber.value = sessionInfo.market
            segments.value = checkStoreData.segments
        }
    }

    fun createSegment() {
        Logg.d { "createSegment started!" }
        if (segmentNumber.value?.length == 7) {
            checkStoreData.addSegment(sessionInfo.market!!, segmentNumber.value!!)

            // todo показать экран с сообщением о начале обработки сегмента

            navigator.openShelfListScreen()
        }
    }

    fun onClickComplete() {
        // todo отправить неотправленные сегменты

        // todo показать сообщение о результате отправки данных
        // 1. Сообщение об успешной отправке. / Что должно быть после этого сообщения? Просто выход?
        // 2. Отправка не удалась. Вопрос о продолжении работы.
    }

    fun onClickItemPosition(position: Int) {
        checkStoreData.currentSegmentIndex = position
        navigator.openShelfListScreen()
    }
}
