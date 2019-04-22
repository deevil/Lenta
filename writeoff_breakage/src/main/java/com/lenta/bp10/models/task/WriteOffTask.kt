package com.lenta.bp10.models.task

import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType

class WriteOffTask(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {

    fun deleteProducts(products: List<ProductInfo>): WriteOffTask {
        // (Артем И., 09.04.2019) удалить перечень продуктов (products), причины списания и марки
        for (i in products.indices) {
            taskRepository.getExciseStamps().deleteExciseStampsForProduct(products[i])
            taskRepository.getWriteOffReasons().deleteWriteOffReasonsForProduct(products[i])
            taskRepository.getProducts().deleteProduct(products[i])
        }
        return this
    }

    fun processGeneralProduct(product: ProductInfo): ProcessGeneralProductService? {
        // (Артем И., 09.04.2019) search product taskRepository если есть то (проверяем, что обычный продукт - не алкоголь) create ProcessGeneralProductService
        // (Артем И., 10.04.2019) поиск продукта в репозитории не делать, проверять только тип товара на General и возвращать ProcessGeneralProductService
        return if (product.type === ProductType.General) {
            ProcessGeneralProductService(taskDescription, taskRepository, product)
        } else null

    }

    fun processNonExciseAlcoProduct(product: ProductInfo): ProcessNonExciseAlcoProductService? {
        // (Артем И., 11.04.2019) тоже самое, что и в ProcessGeneralProductService
        return if (product.type === ProductType.NonExciseAlcohol) {
            ProcessNonExciseAlcoProductService(taskDescription, taskRepository, product)
        } else null

    }

    fun processExciseAlcoProduct(product: ProductInfo): ProcessExciseAlcoProductService? {
        // (Артем И., 11.04.2019) тоже самое, что и в ProcessGeneralProductService
        return if (product.type === ProductType.ExciseAlcohol) {
            ProcessExciseAlcoProductService(taskDescription, taskRepository, product)
        } else null

    }

    fun getProcessedProducts(): List<ProductInfo> {
        return taskRepository.getProducts().getProducts()
    }

    fun getProductCount(): Int {
        // (Артем И., 11.04.2019) данный метод пока оставить так
        return taskRepository.getProducts().lenght()
    }

    fun getTotalCountOfProduct(product: ProductInfo): Double {
        // считать ИТОГО причин списания, а для акцизного товара ИТОГО + кол-во марок
        val totalCount: Double
        when (product.type) {
            ProductType.General -> totalCount = processGeneralProduct(product)!!.getTotalCount()
            ProductType.NonExciseAlcohol -> totalCount = processNonExciseAlcoProduct(product)!!.getTotalCount()
            ProductType.ExciseAlcohol -> totalCount = processExciseAlcoProduct(product)!!.getTotalCount()
            else -> totalCount = 0.0
        }

        return totalCount
    }

    fun getTaskSaveModel(): TaskSaveModel {
        return TaskSaveModel(taskDescription, taskRepository)
    }

    fun clearTask() {
        // (Артем И., 11.04.2019) очистить все репозитории, taskDescription не очищать
        taskRepository.getProducts().clear()
        taskRepository.getWriteOffReasons().clear()
        taskRepository.getExciseStamps().clear()
    }

}