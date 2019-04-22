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

}