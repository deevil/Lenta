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
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map


class NotExposedProductsTask(
        private val taskDescription: NotExposedProductsTaskDescription,
        private val notExposedProductsRepo: INotExposedProductsRepo,
        private val filterableDelegate: FilterableDelegate
) : INotExposedProductsTask, IFilterable by filterableDelegate {


    override var scanInfoResult: ScanInfoResult? = null


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
        scanInfoResult.let {
            requireNotNull(it)
            notExposedProductsRepo.addOrReplaceProduct(
                    NotExposedProductInfo(
                            ean = null,
                            matNr = it.productInfo.materialNumber,
                            name = it.productInfo.description,
                            quantity = quantity,
                            uom = it.productInfo.uom.name,
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
                    if (product.ean?.contains(it.value.value) != true && !product.matNr.contains(it.value.value)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun getProcessedCheckInfo(): INotExposedProductInfo? {
        return getProducts().value?.firstOrNull { it.matNr == scanInfoResult?.productInfo?.materialNumber }
    }


}


interface INotExposedProductsTask : ITask, IFilterable {

    var scanInfoResult: ScanInfoResult?

    fun getProducts(): LiveData<List<INotExposedProductInfo>>

    fun getFilteredProducts(): LiveData<List<INotExposedProductInfo>>

    fun setCheckInfo(quantity: Double?, isEmptyPlaceMarked: Boolean?)

    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)

    fun getProcessedCheckInfo(): INotExposedProductInfo?

}