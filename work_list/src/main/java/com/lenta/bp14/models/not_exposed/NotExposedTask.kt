package com.lenta.bp14.models.not_exposed

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.di.NotExposedScope
import com.lenta.bp14.models.BaseProductInfo
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.filter.FilterFieldType
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.not_exposed.repo.INotExposedRepo
import com.lenta.bp14.models.not_exposed.repo.NotExposedProductInfo
import com.lenta.bp14.requests.not_exposed_product.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

@NotExposedScope
class NotExposedTask @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val taskDescription: NotExposedTaskDescription,
        private val notExposedRepo: INotExposedRepo,
        private val filterableDelegate: IFilterable,
        private val productInfoNotExposedInfoRequest: IProductInfoForNotExposedNetRequest,
        private val gson: Gson
) : INotExposedTask, StateFromToString, IFilterable by filterableDelegate {

    private val productsInfoMap by lazy {
        taskDescription.additionalTaskInfo?.productsInfo?.map { it.matNr to it }?.toMap()
                ?: emptyMap()
    }

    private val checkPlaces by lazy {
        taskDescription.additionalTaskInfo?.checkPlaces?.map { it.matNr to it }?.toMap()
                ?: emptyMap()
    }

    private val processingProducts by lazy {
        getProducts().map { processedGoodInfo ->
            val processedMaterials = processedGoodInfo?.map { it.matNr }?.toSet() ?: emptySet()
            val positions = taskDescription.additionalTaskInfo?.positions?.filter { !processedMaterials.contains(it.matNr) }
                    ?: emptyList()
            positions.map {
                val productInfo = productsInfoMap[it.matNr]
                NotExposedProductInfo(
                        ean = null,
                        name = productInfo?.name.orEmpty(),
                        matNr = it.matNr,
                        quantity = it.quantity,
                        defaultUnits = null,
                        units = null,
                        isEmptyPlaceMarked = null,
                        section = productInfo?.sectionNumber,
                        group = productInfo?.eKGRP
                )
            }
        }
    }

    init {
        initProcessed()
    }

    private fun initProcessed() {

        taskDescription.additionalTaskInfo?.positions?.filter { it.isProcessed.isSapTrue() || it.quantity > 0 }?.let {
            it.forEach { position ->
                val productInfo = productsInfoMap[position.matNr]
                val checkPlace = checkPlaces[position.matNr]
                notExposedRepo.addOrReplaceProduct(
                        NotExposedProductInfo(
                                ean = null,
                                matNr = position.matNr,
                                name = productInfo?.name.orEmpty(),
                                quantity = position.quantity,
                                isEmptyPlaceMarked = checkPlace?.let { place ->
                                    when {
                                        place.statCheck == "2" -> true
                                        place.statCheck == "3" -> false
                                        else -> null
                                    }
                                },
                                section = productInfo?.sectionNumber,
                                group = productInfo?.eKGRP,
                                defaultUnits = null,
                                units = null
                        )
                )
            }
        }

    }


    private var processedGoodInfo: GoodInfoWithQuantity? = null

    override fun getProcessedProductInfoResult(): GoodInfoWithQuantity? {
        return processedGoodInfo
    }


    override fun getTaskType(): ITaskTypeInfo {
        return generalRepo.getTasksTypeInfo(AppTaskTypes.NotExposedProducts.taskType)!!
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getProducts(): LiveData<List<NotExposedProductInfo>> {
        return notExposedRepo.getProducts()
    }

    override fun getToProcessingProducts(): LiveData<List<NotExposedProductInfo>> {
        return processingProducts
    }

    override fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?) {
        processedGoodInfo.let {
            requireNotNull(it)
            notExposedRepo.addOrReplaceProduct(
                    NotExposedProductInfo(
                            ean = null,
                            matNr = it.goodInfo.productInfo.matNr,
                            name = it.goodInfo.productInfo.name,
                            quantity = quantity,
                            defaultUnits = it.goodInfo.defaultUnits,
                            units = it.goodInfo.units,
                            isEmptyPlaceMarked = isEmptyPlaceMarked,
                            section = it.goodInfo.productInfo.sectionNumber,
                            group = it.goodInfo.productInfo.matKL
                    )
            )
        }

    }

    override fun removeCheckResultsByMatNumbers(matNumbers: Set<String>) {
        notExposedRepo.removeProducts(matNumbers)
    }

    override fun getFilteredProducts(): LiveData<List<NotExposedProductInfo>> {
        return getProducts().combineLatest(filterableDelegate.onFiltersChangesLiveData).map {
            requireNotNull(it)
            val products = it.first
            products.filter { productInfo ->
                filter(productInfo)
            }
        }

    }

    private fun filter(product: NotExposedProductInfo): Boolean {
        filterableDelegate.filtersMap.forEach {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it.key) {
                FilterFieldType.NUMBER -> {
                    if (!filterableDelegate.isHaveAnotherActiveFilter(FilterFieldType.NUMBER) && it.value.value.isBlank()) {
                        return false
                    }

                    if (product.ean?.contains(it.value.value) != true && !product.matNr.contains(it.value.value)) {
                        return false
                    }
                }

                FilterFieldType.GROUP -> {
                    if (product.group?.contains(it.value.value) != true) {
                        return false
                    }
                }

                FilterFieldType.SECTION -> {
                    if (product.section?.contains(it.value.value) != true) {
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun getProcessedCheckInfo(): NotExposedProductInfo? {
        return getProducts().value?.firstOrNull { it.matNr == processedGoodInfo?.goodInfo?.productInfo?.matNr }
    }

    override suspend fun getProductInfoAndSetProcessed(matNr: String?, quantity: Double): Either<Failure, GoodInfo> {
        return productInfoNotExposedInfoRequest(
                NotExposedInfoRequestParams(
                        ean = null,
                        matNr = matNr,
                        tkNumber = taskDescription.tkNumber
                )
        ).rightToLeft { goodInfo ->
            if (!isAllowedProduct(goodInfo.productInfo.matNr)) {
                Failure.InvalidProductForTask
            } else {
                processedGoodInfo = GoodInfoWithQuantity(goodInfo, quantity)
                null
            }
        }
    }


    override fun getReportData(ip: String): NotExposedReport {
        val processingProducts = getToProcessingProducts().value ?: emptyList()
        return NotExposedReport(
                ip = ip,
                description = taskDescription,
                isNotFinish = processingProducts.isNotEmpty(),
                checksResults = notExposedRepo.getProducts().value ?: emptyList(),
                notProcessed = processingProducts
        )
    }

    override fun isEmpty(): Boolean {
        return getProducts().value.isNullOrEmpty()
    }

    override fun isHaveDiscrepancies(): Boolean {
        return getToProcessingProducts().value?.isNotEmpty() == true
    }

    override fun getListOfDifferences(): LiveData<List<BaseProductInfo>> {
        return processingProducts.map { list ->
            list?.map { item ->
                BaseProductInfo(
                        matNr = item.matNr,
                        name = item.name
                )
            }
        }
    }

    override fun setMissing(matNrList: List<String>) {
        matNrList.forEach { matNr ->
            taskDescription.additionalTaskInfo?.productsInfo?.firstOrNull { it.matNr == matNr }?.let {
                notExposedRepo.addOrReplaceProduct(
                        NotExposedProductInfo(
                                ean = null,
                                matNr = it.matNr,
                                name = it.name,
                                quantity = 0.0,
                                defaultUnits = null,
                                units = null,
                                isEmptyPlaceMarked = false,
                                section = it.sectionNumber,
                                group = it.matKL
                        )
                )
            }
        }

    }

    override fun isAllowedProduct(materialNumber: String): Boolean {
        if (!taskDescription.isStrictList) {
            return true
        }
        return taskDescription.additionalTaskInfo?.positions?.any { it.matNr == materialNumber }
                ?: true
    }

    override fun getStateAsString(): String {
        return gson.toJson(NotExposedData(
                taskDescription = taskDescription,
                goods = getProducts().value ?: emptyList()
        ))
    }

    override fun loadStateFromString(state: String) {
        val data = gson.fromJson(state, NotExposedData::class.java)
        data.goods.map { good ->
            notExposedRepo.addOrReplaceProduct(good)
        }
    }

}


interface INotExposedTask : ITask, IFilterable {

    fun getProcessedProductInfoResult(): GoodInfoWithQuantity?

    fun getToProcessingProducts(): LiveData<List<NotExposedProductInfo>>

    fun getProducts(): LiveData<List<NotExposedProductInfo>>

    fun getFilteredProducts(): LiveData<List<NotExposedProductInfo>>

    fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?)

    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)

    fun getProcessedCheckInfo(): NotExposedProductInfo?

    suspend fun getProductInfoAndSetProcessed(matNr: String?, quantity: Double): Either<Failure, GoodInfo>

    fun getReportData(ip: String): NotExposedReport

    fun isAllowedProduct(materialNumber: String): Boolean

}

data class NotExposedData(
        val taskDescription: NotExposedTaskDescription,
        val goods: List<NotExposedProductInfo>
)

