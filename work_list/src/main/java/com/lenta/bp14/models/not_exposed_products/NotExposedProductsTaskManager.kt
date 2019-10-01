package com.lenta.bp14.models.not_exposed_products

import com.google.gson.Gson
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.models.filter.FilterableDelegate
import com.lenta.bp14.models.filter.FilterFieldType.*
import com.lenta.bp14.models.not_exposed_products.repo.NotExposedProductsRepo
import com.lenta.bp14.requests.not_exposed_product.IProductInfoForNotExposedNetRequest
import com.lenta.shared.platform.time.ITimeMonitor
import javax.inject.Inject

class NotExposedProductsTaskManager @Inject constructor(
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson,
        private val productInfoNetNotExposedInfoRequest: IProductInfoForNotExposedNetRequest
) : BaseTaskManager<INotExposedProductsTask, NotExposedProductsTaskDescription>() {

    override fun newTask(taskDescription: NotExposedProductsTaskDescription): INotExposedProductsTask? {
        _task = NotExposedProductsTask(
                taskDescription = taskDescription,
                notExposedProductsRepo = NotExposedProductsRepo(),
                filterableDelegate = FilterableDelegate(
                        supportedFilters = setOf(
                                NUMBER,
                                SECTION,
                                GROUP
                        )
                ),
                productInfoNotExposedInfoRequest = productInfoNetNotExposedInfoRequest
        )
        return _task
    }

}