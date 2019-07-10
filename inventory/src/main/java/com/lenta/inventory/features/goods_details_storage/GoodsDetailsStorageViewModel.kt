package com.lenta.inventory.features.goods_details_storage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.ProcessExciseAlcoProductService
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.EgaisStampVersion
import com.lenta.shared.models.core.ExciseStamp
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsDetailsStorageViewModel : CoreViewModel() {

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val isGeneralProduct: MutableLiveData<Boolean> = MutableLiveData()
    val isStorePlace: MutableLiveData<Boolean> = MutableLiveData()
    val partly: MutableLiveData<String> = MutableLiveData()
    val vintage: MutableLiveData<String> = MutableLiveData()
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val countedCategories: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()
    val countedProssed: MutableLiveData<List<GoodsDetailsStorageItem>> = MutableLiveData()
    val countedNotProssed: MutableLiveData<List<GoodsDetailsStorageItem>> = MutableLiveData()
    val categoriesSelectionsHelper = SelectionItemsHelper()
    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getInventoryTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    val deleteButtonEnabled: MutableLiveData<Boolean> = categoriesSelectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    init {
        viewModelScope.launch {
            //searchProductDelegate.init(viewModelScope = this@GoodsListViewModel::viewModelScope)
            updateGoodsInfo()
        }

    }

    fun onResume() {
        updateGoodsInfo()
    }

    private fun updateGoodsInfo() {
        updateCategories()
        updateNotProcessed()
        updateProcessed()
    }

    private fun updateCategories() {
        val partlyCount = processServiceManager.
                                        getInventoryTask()!!.
                                        taskRepository.
                                        getExciseStamps().
                                        findExciseStampsOfProduct(productInfo.value!!).
                                        filter {
                                            ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V2.version
                                        }.size
        val vintageCount = processServiceManager.
                                        getInventoryTask()!!.
                                        taskRepository.
                                        getExciseStamps().
                                        findExciseStampsOfProduct(productInfo.value!!).
                                        filter {
                                            ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V3.version
                                        }.size

        val goodsDetailsCategoriesItem: MutableList<GoodsDetailsCategoriesItem> = ArrayList()

        val index = if (partlyCount > 0) 2 else 1

        if (partlyCount > 0) {
            goodsDetailsCategoriesItem.add(
                    GoodsDetailsCategoriesItem(
                            number = 1,
                            name = partly.value!!,
                            quantity = "$partlyCount",
                            even = 1 % 2 == 0,
                            egaisVersion = EgaisStampVersion.V2
                    )
            )
        }

        if (vintageCount > 0) {
            goodsDetailsCategoriesItem.add(
                    GoodsDetailsCategoriesItem(
                            number = index,
                            name = vintage.value!!,
                            quantity = "$vintageCount",
                            even = index % 2 == 0,
                            egaisVersion = EgaisStampVersion.V3
                    )
            )
        }
        countedCategories.postValue(goodsDetailsCategoriesItem.reversed())
        categoriesSelectionsHelper.clearPositions()
    }

    private fun updateNotProcessed() {
        countedNotProssed.postValue(
                processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        getNotProcessedProducts().
                        filter {it.materialNumber == productInfo.value!!.materialNumber}.
                        mapIndexed { index, taskProductInfo ->
                            GoodsDetailsStorageItem(
                                    number = index + 1,
                                    name = taskProductInfo.placeCode,
                                    quantity = taskProductInfo.factCount.toStringFormatted(),
                                    even = index % 2 == 0
                            )
                        }.reversed()
        )
    }

    private fun updateProcessed() {
        countedProssed.postValue(
                processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        getProcessedProducts().
                        filter {it.materialNumber == productInfo.value!!.materialNumber}.
                        mapIndexed { index, taskProductInfo ->
                            GoodsDetailsStorageItem(
                                    number = index + 1,
                                    name = taskProductInfo.placeCode,
                                    quantity = taskProductInfo.factCount.toStringFormatted(),
                                    even = index % 2 == 0
                            )
                        }.reversed()
        )
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickDelete() {
        categoriesSelectionsHelper.selectedPositions.value?.map {position ->
            countedCategories.value!![position].egaisVersion
        }?.let {
            it.map {egaisStampVersion ->
                processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getExciseStamps().
                        findExciseStampsOfProduct(productInfo.value!!).
                        filter {exciseStamp ->
                            (egaisStampVersion == EgaisStampVersion.V2 && ExciseStamp.getEgaisVersion(exciseStamp.code) == EgaisStampVersion.V2.version) ||
                                    (egaisStampVersion == EgaisStampVersion.V3 && ExciseStamp.getEgaisVersion(exciseStamp.code) == EgaisStampVersion.V3.version)
                        }?.
                        let {listTaskExciseStamp ->
                            processExciseAlcoProductService.setFactCount(processExciseAlcoProductService.getFactCount() - listTaskExciseStamp.size)

                            processServiceManager.
                                    getInventoryTask()!!.
                                    taskRepository.
                                    getExciseStamps().
                                    deleteExciseStamps(listTaskExciseStamp)
                        }
            }
        }
        updateGoodsInfo()
    }
}
