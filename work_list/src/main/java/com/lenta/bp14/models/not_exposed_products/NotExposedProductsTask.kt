package com.lenta.bp14.models.not_exposed_products

import androidx.lifecycle.LiveData
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.filter.FilterableDelegate
import com.lenta.bp14.models.filter.FilterFieldType
import com.lenta.bp14.models.filter.IFilterable
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductInfo
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductsRepo
import com.lenta.bp14.models.not_exposed_products.repo.NotExposedProductInfo
import com.lenta.bp14.requests.not_exposed_product.GoodInfo
import com.lenta.bp14.requests.not_exposed_product.NotExposedInfoRequestParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map


class NotExposedProductsTask(
        private val taskDescription: NotExposedProductsTaskDescription,
        private val notExposedProductsRepo: INotExposedProductsRepo,
        private val filterableDelegate: FilterableDelegate,
        private val productInfoNotExposedInfoRequest: UseCase<GoodInfo, NotExposedInfoRequestParams>
) : INotExposedProductsTask, IFilterable by filterableDelegate {


    private var processedGoodInfo: GoodInfo? = null

    override fun getProcessedProductInfoResult(): GoodInfo? {
        return processedGoodInfo
    }


    override fun getTaskType(): ITaskType {
        return TaskTypes.NotExposedProducts.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getProducts(): LiveData<List<INotExposedProductInfo>> {
        return notExposedProductsRepo.getProducts()
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
                            isEmptyPlaceMarked = isEmptyPlaceMarked
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


}


interface INotExposedProductsTask : ITask, IFilterable {

    fun getProcessedProductInfoResult(): GoodInfo?

    fun getProducts(): LiveData<List<INotExposedProductInfo>>

    fun getFilteredProducts(): LiveData<List<INotExposedProductInfo>>

    fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?)

    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)

    fun getProcessedCheckInfo(): INotExposedProductInfo?

    suspend fun getProductInfoAndSetProcessed(ean: String? = null, matNr: String? = null): Either<Failure, GoodInfo>

}

