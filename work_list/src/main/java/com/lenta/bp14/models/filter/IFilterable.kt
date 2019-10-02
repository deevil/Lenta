package com.lenta.bp14.models.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface IFilterable {

    val filtersMap: Map<FilterFieldType, FilterParameter>

    val onFiltersChangesLiveData: LiveData<Boolean>

    fun getSupportedFiltersTypes(): Set<FilterFieldType>

    fun getFilterValue(filterFieldType: FilterFieldType): String?

    fun onFilterChanged(filterParameter: FilterParameter)

    fun addNewFilters(filters: List<FilterParameter>)

    fun clearAllFilters()

    fun isHaveAnotherActiveFilter(filterFieldType: FilterFieldType): Boolean
}

class FilterableDelegate(private val supportedFilters: Set<FilterFieldType>) : IFilterable {
    override val filtersMap: MutableMap<FilterFieldType, FilterParameter> = mutableMapOf()
    override val onFiltersChangesLiveData: MutableLiveData<Boolean> = MutableLiveData(false)


    override fun getSupportedFiltersTypes(): Set<FilterFieldType> {
        return supportedFilters
    }

    override fun getFilterValue(filterFieldType: FilterFieldType): String? {
        return filtersMap[filterFieldType]?.value
    }

    override fun onFilterChanged(filterParameter: FilterParameter) {
        filtersMap[filterParameter.filterFieldType] = filterParameter
        onFiltersChangesLiveData.value = true

    }

    override fun addNewFilters(filters: List<FilterParameter>) {
        filtersMap.clear()
        filters.forEach {
            filtersMap[it.filterFieldType] = it
        }
        onFiltersChangesLiveData.value = true
    }

    override fun clearAllFilters() {
        filtersMap.clear()
        onFiltersChangesLiveData.value = true
    }

    override fun isHaveAnotherActiveFilter(filterFieldType: FilterFieldType): Boolean {
        filtersMap.forEach {
            if (it.key != filterFieldType && it.value.value.isNotBlank()) {
                return true
            }
        }
        return false

    }


}