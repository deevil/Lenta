package com.lenta.bp14.features.search_filter

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.filter.FilterFieldType
import com.lenta.bp14.models.filter.FilterParameter
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
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

    val filterable by lazy {
        require( task is IFilterable)
        task as IFilterable
    }


    val placeStorageVisibility by lazy {
        filterable.getSupportedFiltersTypes().contains(FilterFieldType.PLACE_STORAGE)
    }

    val commentVisibility by lazy {
        filterable.getSupportedFiltersTypes().contains(FilterFieldType.COMMENT)
    }

    init {
        launchUITryCatch {
            mapFieldsToTypes.forEach {
                it.key.value = filterable.getFilterValue(it.value)
            }
        }
    }

    fun getTitle(): String? {
        return "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    fun onClickSearch() {
        filterable.addNewFilters(
                filters = mapFieldsToTypes.map {
                    FilterParameter(it.value, it.key.value.orEmpty())
                }
        )
        screenNavigator.goBack()

    }


}
