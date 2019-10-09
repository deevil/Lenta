package com.lenta.bp14.models.not_exposed_products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.di.NotExposedScope
import com.lenta.bp14.models.BaseProductInfo
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.filter.FilterFieldType
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductInfo
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductsRepo
import com.lenta.bp14.models.not_exposed_products.repo.NotExposedProductInfo
import com.lenta.bp14.requests.not_exposed_product.GoodInfo
import com.lenta.bp14.requests.not_exposed_product.IProductInfoForNotExposedNetRequest
import com.lenta.bp14.requests.not_exposed_product.NotExposedInfoRequestParams
import com.lenta.bp14.requests.not_exposed_product.NotExposedReport
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

@NotExposedScope
class NotExposedProductsTask @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val taskDescription: NotExposedProductsTaskDescription,
        private val notExposedProductsRepo: INotExposedProductsRepo,
        private val filterableDelegate: IFilterable,
        private val productInfoNotExposedInfoRequest: IProductInfoForNotExposedNetRequest
) : INotExposedProductsTask, IFilterable by filterableDelegate {

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
                        name = productInfo?.name ?: "",
                        matNr = it.matNr,
                        quantity = it.quantity,
                        uom = null,
                        isEmptyPlaceMarked = null,
                        section = productInfo?.sectionNumber,
                        group = productInfo?.eKGRP
                ) as INotExposedProductInfo
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
                notExposedProductsRepo.addOrReplaceProduct(
                        NotExposedProductInfo(
                                ean = null,
                                matNr = position.matNr,
                                name = productInfo?.name ?: "",
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
                                uom = null
                        )
                )
            }
        }

    }


    private var processedGoodInfo: GoodInfo? = null

    override fun getProcessedProductInfoResult(): GoodInfo? {
        return processedGoodInfo
    }


    override fun getTaskType(): ITaskTypeInfo {
        return generalRepo.getTasksTypeInfo(AppTaskTypes.NotExposedProducts.taskType)!!
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getProducts(): LiveData<List<INotExposedProductInfo>> {
        return notExposedProductsRepo.getProducts()
    }

    override fun getToProcessingProducts(): LiveData<List<INotExposedProductInfo>> {
        return processingProducts
    }

    override fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?) {
        processedGoodInfo.let {
            requireNotNull(it)
            notExposedProductsRepo.addOrReplaceProduct(
                    NotExposedProductInfo(
                            ean = null,
                            matNr = it.productInfo.matNr,
                            name = it.productInfo.name,
                            quantity = quantity,
                            uom = it.uom,
                            isEmptyPlaceMarked = isEmptyPlaceMarked,
                            section = it.productInfo.sectionNumber,
                            group = it.productInfo.matKL
                    )
            )
        }

    }

    override fun removeCheckResultsByMatNumbers(matNumbers: Set<String>) {
        notExposedProductsRepo.removeProducts(matNumbers)
    }

    override fun getFilteredProducts(): LiveData<List<INotExposedProductInfo>> {
        return getProducts().combineLatest(filterableDelegate.onFiltersChangesLiveData).map {
            requireNotNull(it)
            val products = it.first
            products.filter { productInfo ->
                filter(productInfo)
            }
        }

    }

    private fun filter(product: INotExposedProductInfo): Boolean {
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

    override fun getProcessedCheckInfo(): INotExposedProductInfo? {
        return getProducts().value?.firstOrNull { it.matNr == processedGoodInfo?.productInfo?.matNr }
    }

    override suspend fun getProductInfoAndSetProcessed(ean: String?, matNr: String?): Either<Failure, GoodInfo> {
        return productInfoNotExposedInfoRequest(
                NotExposedInfoRequestParams(
                        ean = ean,
                        matNr = matNr,
                        tkNumber = taskDescription.tkNumber
                )

        ).also {
            it.map {
                processedGoodInfo = it
            }
        }
    }


    override fun getReportData(ip: String, isNotFinish: Boolean): NotExposedReport {
        return NotExposedReport(
                ip = ip,
                description = taskDescription,
                isNotFinish = isNotFinish,
                checksResults = notExposedProductsRepo.getProducts().value ?: emptyList()
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
            } }
    }

    override fun setMissing(matNrList: List<String>) {
        //TODO implement this
    }


}


interface INotExposedProductsTask : ITask, IFilterable {

    fun getProcessedProductInfoResult(): GoodInfo?

    fun getToProcessingProducts(): LiveData<List<INotExposedProductInfo>>

    fun getProducts(): LiveData<List<INotExposedProductInfo>>

    fun getFilteredProducts(): LiveData<List<INotExposedProductInfo>>

    fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?)

    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)

    fun getProcessedCheckInfo(): INotExposedProductInfo?

    suspend fun getProductInfoAndSetProcessed(ean: String? = null, matNr: String? = null): Either<Failure, GoodInfo>

    fun getReportData(ip: String, isNotFinish: Boolean): NotExposedReport

}

