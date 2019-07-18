package com.lenta.bp7.features.segment_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Segment
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
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
    lateinit var checkData: CheckData

    val segments: MutableLiveData<List<Segment>> = MutableLiveData()
    private val unfinishedSegment: MutableLiveData<Boolean> = MutableLiveData()

    val marketNumber: MutableLiveData<String> = MutableLiveData("")
    val segmentNumber: MutableLiveData<String> = MutableLiveData("")

    val completeButtonEnabled: MutableLiveData<Boolean> = segments.combineLatest(unfinishedSegment).map { pair ->
        pair?.first?.isNotEmpty() ?: false && pair?.second == false
    }

    init {
        viewModelScope.launch {
            unfinishedSegment.value = checkData.isExistUnfinishedSegment()
            marketNumber.value = sessionInfo.market
            segments.value = checkData.segments
        }
    }

    fun createSegment() {
        if (unfinishedSegment.value == true) {
            // todo ЭКРАН сообщение наличии незавершенного сегмента

            return
        }

        if (segmentNumber.value?.length == 7) {
            checkData.addSegment(sessionInfo.market!!, segmentNumber.value!!)

            // todo ЭКРАН сообщение о начале обработки сегмента

            // !Перенести на другой экран
            navigator.openShelfListScreen()
        }
    }

    fun onClickComplete() {
        // todo ЭКРАН отправить неотправленные сегменты

        // todo ЭКРАН сообщение о результате отправки данных
        // 1. Сообщение об успешной отправке. / Что должно быть после этого сообщения? Просто выход?
        // 2. Отправка не удалась. Вопрос о продолжении работы.
    }

    fun onClickItemPosition(position: Int) {
        checkData.currentSegmentIndex = position
        navigator.openShelfListScreen()
    }
}
