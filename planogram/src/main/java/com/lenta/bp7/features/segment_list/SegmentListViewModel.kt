package com.lenta.bp7.features.segment_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.data.model.Segment
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class SegmentListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData

    companion object {
        const val SEGMENT_NUMBER_LENGTH = 7
    }

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

    override fun onOkInSoftKeyboard(): Boolean {
        createSegment()
        return true
    }

    private fun createSegment() {
        if (checkData.isExistUnfinishedSegment()) {
            // todo ЭКРАН обнаружен незавершенный сегмент, начало работы с новым невозможно. Перейти к сегменту?

            return
        }

        if (segmentNumber.value?.length == SEGMENT_NUMBER_LENGTH) {
            // Сообщение - Начата обработка сегмента
            /*navigator.showBeganProcessingSegment(segmentNumber.value!!, checkData.countFacings){
                checkData.addSegment(sessionInfo.market!!, segmentNumber.value!!)
                navigator.openShelfListScreen()
            }*/

            checkData.addSegment(sessionInfo.market!!, segmentNumber.value!!)
            navigator.openShelfListScreen()
        }
    }

    fun onClickComplete() {
        // todo ЭКРАН отправить неотправленные сегменты

        // todo ЭКРАН сообщение о результате отправки данных
        // 1. Сообщение об успешной отправке. Выход из приложения.
        // 2. Отправка не удалась. Вопрос о продолжении работы.
    }

    fun onClickItemPosition(position: Int) {
        checkData.currentSegmentIndex = position
        navigator.openShelfListScreen()
    }
}
