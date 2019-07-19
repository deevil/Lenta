package com.lenta.inventory.features.job_card

import androidx.lifecycle.MutableLiveData
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.repos.IRepoInMemoryHolder
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.date_time.DateTimeUtil.convertTimeString
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class JobCardViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    private lateinit var tasksItem: TasksItem

    lateinit var typesRecount: List<RecountType>
    lateinit var recountsTitles: List<String>

    val title: String by lazy {
        "${tasksItem.taskType}-${tasksItem.taskNumber}"
    }

    val productType: ProductType? by lazy {
        if (tasksItem.gis == "A") ProductType.ExciseAlcohol else null
    }

    val stock: String by lazy {
        tasksItem.stock
    }

    val isPrimary: Boolean by lazy {
        tasksItem.isRecount.isNotBlank()
    }

    val actualPeriodFrom: String by lazy {
        convertTime(tasksItem.dateFrom)
    }


    val actualPeriodTo: String by lazy {
        convertTime(tasksItem.dateTo)
    }

    val isStrictList: Boolean by lazy {
        tasksItem.isStrict.isNotBlank()
    }

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)


    fun init(taskNumber: String, typesRecount: List<RecountType>, converterTypeToString: (RecountType) -> String) {
        tasksItem = repoInMemoryHolder.tasksListRestInfo.value?.tasks?.first { it.taskNumber == taskNumber }!!
        Logg.d { "taskItem $tasksItem" }

        this.typesRecount = typesRecount
        this.recountsTitles = typesRecount
                .filterIndexed {
                    index, recountType -> if (!isStrictList) true else index == 0 }
                .map {
                    converterTypeToString(it)
                }

    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position

    }

    fun onClickNext() {
        screenNavigator.openLoadingTaskContentsScreen(tasksItem, getSelectedTypeRecount()
                ?: RecountType.None)
    }

    private fun getSelectedTypeRecount(): RecountType? {
        return typesRecount.getOrNull(selectedPosition.value ?: -1)
    }

    private fun convertTime(dateString: String): String {
        return convertTimeString(formatSource = Constants.DATE_FORMAT_yyyy_mm_dd, formatDestination = Constants.DATE_FORMAT_ddmmyy, date = dateString)
    }


}
