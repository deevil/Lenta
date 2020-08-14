package com.lenta.bp9.features.goods_details.marking_goods_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_details.GoodsDetailsCategoriesItem
import com.lenta.bp9.features.goods_details.GoodsDetailsPropertiesItem
import com.lenta.bp9.model.processing.ProcessMarkingProductService
import com.lenta.bp9.model.processing.ProcessMercuryProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskMarkingGoodsProperties
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class MarkingGoodsDetailsViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var processMarkingProductService: ProcessMarkingProductService

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val goodsDetails: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()
    val goodsProperties: MutableLiveData<List<GoodsDetailsPropertiesItem>> = MutableLiveData()
    val markingGoodsProperties: MutableLiveData<List<TaskMarkingGoodsProperties>> by lazy {
        repoInMemoryHolder.markingGoodsProperties
    }
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val categoriesSelectionsHelper = SelectionItemsHelper()
    val enabledDelBtn: MutableLiveData<Boolean> = categoriesSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }
    val visibilityDelButton: MutableLiveData<Boolean> = selectedPage.map {
        if (markingGoodsProperties.value.isNullOrEmpty()) {
            it == SELECTED_PAGE_DEFAULT
        } else {
            it == SELECTED_PAGE_CATEGORIES
        }
    }

    init {
        launchUITryCatch {
            val qualityInfoForDiscrepancy =
                    dataBase.getQualityInfoForDiscrepancy()
                            ?.map {
                                it.convertToReasonRejectionInfo()
                            }.orEmpty()
            val allReasonRejectionInfo = dataBase.getAllReasonRejectionInfo().orEmpty()
            val discrepancyErrorUPD = dataBase.getQualityErrorUPD()
                    ?.map {
                        it.convertToReasonRejectionInfo()
                    }.orEmpty()
            reasonRejectionInfo.value = qualityInfoForDiscrepancy + allReasonRejectionInfo + discrepancyErrorUPD

            updateData()
        }

    }

    fun getTitle(): String {
        return "${productInfo.value?.getMaterialLastSix().orEmpty()} ${productInfo.value?.description.orEmpty()}"
    }

    fun onClickDelete() {
        if (productInfo.value != null && productInfo.value?.isNotEdit == false) {
            categoriesSelectionsHelper.selectedPositions.value
                    ?.forEach { position ->
                        val isDiscrepanciesErrorUPD =
                                (goodsDetails.value
                                        ?.get(position)
                                        ?.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_ERROR_UPD)

                        if (!isDiscrepanciesErrorUPD) {
                            taskManager.getReceivingTask()
                                    ?.taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.deleteProductDiscrepancy(
                                            materialNumber = goodsDetails.value
                                                    ?.get(position)
                                                    ?.materialNumber
                                                    .orEmpty(),
                                            typeDiscrepancies = goodsDetails.value
                                                    ?.get(position)
                                                    ?.typeDiscrepancies
                                                    .orEmpty()
                                    )

                            taskManager.getReceivingTask()
                                    ?.taskRepository
                                    ?.getBlocksDiscrepancies()
                                    ?.deleteBlocksDiscrepanciesForProductAndDiscrepancies(
                                            materialNumber = goodsDetails.value
                                                    ?.get(position)
                                                    ?.materialNumber
                                                    .orEmpty(),
                                            typeDiscrepancies = goodsDetails.value
                                                    ?.get(position)
                                                    ?.typeDiscrepancies
                                                    .orEmpty()
                                    )

                            processMarkingProductService.delBlockDiscrepancy(
                                    typeDiscrepancies = goodsDetails.value
                                            ?.get(position)
                                            ?.typeDiscrepancies
                                            .orEmpty()
                            )
                        }
                    }
        }
        updateData()
    }

    private fun updateData() {
        updateProperties()
        updateCategories()
    }

    private fun updateCategories() {
        goodsDetails.postValue(
                productInfo.value
                        ?.let { product ->
                            taskManager.getReceivingTask()
                                    ?.taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.findProductDiscrepanciesOfProduct(product)
                        }?.mapIndexed { index, discrepancy ->
                            val isNormDiscrepancies = discrepancy.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                            GoodsDetailsCategoriesItem(
                                    number = index + 1,
                                    name = reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name.orEmpty(),
                                    nameBatch = "",
                                    visibilityNameBatch = false,
                                    quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}",
                                    isNormDiscrepancies = isNormDiscrepancies,
                                    typeDiscrepancies = discrepancy.typeDiscrepancies,
                                    materialNumber = discrepancy.materialNumber,
                                    batchDiscrepancies = null,
                                    even = index % 2 == 0
                            )
                        }?.reversed()
        )

        categoriesSelectionsHelper.clearPositions()

    }

    private fun updateProperties() {
        goodsProperties.postValue(
                repoInMemoryHolder.markingGoodsProperties.value
                        ?.mapIndexed { index, taskMarkingGoodsProperties ->
                            GoodsDetailsPropertiesItem(
                                    number = index + 1,
                                    ean = taskMarkingGoodsProperties.ean,
                                    properties = taskMarkingGoodsProperties.properties,
                                    value = taskMarkingGoodsProperties.value,
                                    even = index % 2 == 0
                            )
                        }
                        ?.reversed()
        )
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    companion object {
        private const val SELECTED_PAGE_DEFAULT = 0
        private const val SELECTED_PAGE_CATEGORIES = 1
    }

}
