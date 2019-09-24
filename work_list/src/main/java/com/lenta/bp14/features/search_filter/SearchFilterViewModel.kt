package com.lenta.bp14.features.search_filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchFilterViewModel : CoreViewModel() {

    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val section = MutableLiveData("")
    val groupOfGoods = MutableLiveData("")
    val storagePlace = MutableLiveData("")
    val comment = MutableLiveData("")

    private val mapFieldsToTypes by lazy {
        mapOf<MutableLiveData<String>, FilterFieldType>(
                section to FilterFieldType.SECTION,
                groupOfGoods to FilterFieldType.GROUP,
                storagePlace to FilterFieldType.PLACE_STORAGE,
                comment to FilterFieldType.COMMENT
        )
    }


    val task by lazy {
        generalTaskManager.getProcessedTask()!!
    }


    val placeStorageVisibility by lazy {
        task.getSupportedFiltersTypes().contains(FilterFieldType.PLACE_STORAGE)
    }

    val commentVisibility by lazy {
        task.getSupportedFiltersTypes().contains(FilterFieldType.COMMENT)
    }

    init {
        viewModelScope.launch {
            mapFieldsToTypes.forEach {
                it.key.value = task.getFilterValue(it.value)
            }
        }
    }

    fun getTitle(): String? {
        return "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    fun onClickSearch() {
        task.addNewFilters(
                filters = mapFieldsToTypes.map {
                    FilterParameter(it.value, it.key.value ?: "")
                }
        )
        screenNavigator.goBack()

    }


}
