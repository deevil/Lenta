package com.lenta.bp9.features.goods_details.marking_goods_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_details.GoodsDetailsCategoriesItem
import com.lenta.bp9.features.goods_details.GoodsDetailsPropertiesItem
import com.lenta.bp9.model.processing.ProcessMarkingBoxProductService
import com.lenta.bp9.model.processing.ProcessMarkingProductService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_ERROR_UPD
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

class MarkingGoodsDetailsViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var processMarkingProductService: ProcessMarkingProductService

    @Inject
    lateinit var processMarkingBoxProductService: ProcessMarkingBoxProductService

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    private val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
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
        val product = productInfo.value
        if (product != null && !product.isNotEdit) {
            categoriesSelectionsHelper.selectedPositions.value
                    ?.forEach { position ->
                        processSelectedPosition(position, product)
                    }
        }
        updateData()
    }

    //https://bitbucket.org/eigenmethodlentatempteam/lenta-pdct-android/pull-requests/534/grz_features_6037/diff
    private fun processSelectedPosition(position: Int, product: TaskProductInfo) {
        val isDiscrepanciesErrorUPD =
                goodsDetails.value
                        ?.getOrNull(position)
                        ?.typeDiscrepancies == TYPE_DISCREPANCIES_REASON_REJECTION_ERROR_UPD

        val materialNumber =
                goodsDetails.value
                        ?.getOrNull(position)
                        ?.materialNumber
                        .orEmpty()

        val typeDiscrepancies =
                goodsDetails.value
                        ?.getOrNull(position)
                        ?.typeDiscrepancies
                        .orEmpty()

        if (!isDiscrepanciesErrorUPD) {
            taskRepository
                    ?.let {
                        it.getProductsDiscrepancies().deleteProductDiscrepancy(materialNumber, typeDiscrepancies)
                        it.getBoxesDiscrepancies().deleteBoxesDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)
                        it.getBlocksDiscrepancies().deleteBlocksDiscrepanciesForProductAndDiscrepancies(materialNumber,typeDiscrepancies)
                    }

            when(getMarkingGoodsRegime(taskManager, product)) {
                MarkingGoodsRegime.UomStWithoutBoxes -> processMarkingProductService.delBlockDiscrepancy(typeDiscrepancies)
                MarkingGoodsRegime.UomStWithBoxes -> processMarkingBoxProductService.delBoxAndBlockDiscrepancy(typeDiscrepancies)
            }
        }
    }

    private fun updateData() {
        updateProperties()
        updateCategories()
    }

    private fun updateCategories() {
        goodsDetails.postValue(
                productInfo.value
                        ?.let { product ->
                            taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(product)
                        }
                        ?.mapIndexed { index, discrepancy ->
                            val isNormDiscrepancies = discrepancy.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
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
                                    zBatchDiscrepancies = null,
                                    even = index % 2 == 0
                            )
                        }
                        ?.reversed()
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
