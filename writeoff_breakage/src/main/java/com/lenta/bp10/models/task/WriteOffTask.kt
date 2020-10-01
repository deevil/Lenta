package com.lenta.bp10.models.task

import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.requests.network.PrintProduct
import com.lenta.bp10.requests.network.PrintTask
import com.lenta.bp10.requests.network.SendWriteOffDataParams
import com.lenta.bp10.requests.network.pojo.ExciseStamp
import com.lenta.bp10.requests.network.pojo.MaterialNumber
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType

class WriteOffTask(
        val taskDescription: TaskDescription,
        internal val taskRepository: ITaskRepository
) {

    fun deleteProducts(products: List<ProductInfo>): WriteOffTask {
        // (Артем И., 09.04.2019) удалить перечень продуктов (products), причины списания и марки
        products.forEach {
            deleteProduct(it)
        }
        return this
    }

    private fun deleteProduct(productInfo: ProductInfo) {
        taskRepository.getExciseStamps().deleteExciseStampsForProduct(productInfo)
        taskRepository.getWriteOffReasons().deleteWriteOffReasonsForProduct(productInfo)
        taskRepository.getProducts().deleteProduct(productInfo)
    }

    fun processGeneralProduct(product: ProductInfo): ProcessGeneralProductService? {
        // (Артем И., 09.04.2019) search product taskRepository если есть то (проверяем, что обычный продукт - не алкоголь) create ProcessGeneralProductService
        // (Артем И., 10.04.2019) поиск продукта в репозитории не делать, проверять только тип товара на General и возвращать ProcessGeneralProductService
        return if (product.type == ProductType.General || product.type == ProductType.NonExciseAlcohol) {
            ProcessGeneralProductService(taskDescription, taskRepository, product)
        } else null

    }


    fun processExciseAlcoProduct(product: ProductInfo): ProcessExciseAlcoProductService? {
        // (Артем И., 11.04.2019) тоже самое, что и в ProcessGeneralProductService
        return if (product.type == ProductType.ExciseAlcohol) {
            ProcessExciseAlcoProductService(taskDescription, taskRepository, product)
        } else null

    }

    fun processMarkedGoodProduct(product: ProductInfo): ProcessMarkedGoodProductService? {
        return if (product.type == ProductType.Marked) {
            ProcessMarkedGoodProductService(taskDescription, taskRepository, product)
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
        return when (product.type) {
            ProductType.General -> processGeneralProduct(product)?.getTotalCount() ?: 0.0
            ProductType.NonExciseAlcohol -> processGeneralProduct(product)?.getTotalCount() ?: 0.0
            ProductType.ExciseAlcohol -> processExciseAlcoProduct(product)?.getTotalCount() ?: 0.0
            ProductType.Marked -> processMarkedGoodProduct(product)?.getTotalCount() ?: 0.0
            else -> 0.0
        }
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

    fun deleteTaskWriteOffReason(taskWriteOffReason: TaskWriteOffReason) {
        taskRepository.getWriteOffReasons().deleteWriteOffReason(taskWriteOffReason)

        taskRepository.getProducts().findProduct(taskWriteOffReason.materialNumber)?.let { productInfo ->
            if (getTotalCountOfProduct(productInfo) <= 0) {
                deleteProduct(productInfo)
            }
        }

        taskRepository.getExciseStamps().apply {
            findExciseStampsOfProduct(taskWriteOffReason.materialNumber)
                    .filter { it.writeOffReason == taskWriteOffReason.writeOffReason.code }
                    .forEach { taskExciseStamp ->
                        deleteExciseStamp(taskExciseStamp)
                    }
        }
    }

}


fun WriteOffTask.getPrinterTask(): PrintTask {
    with(taskDescription) {
        return PrintTask(
                typeTask = taskType.code,
                tkNumber = tkNumber,
                storloc = stock,
                printerName = printer,
                products = getProductsPrint()
        )
    }
}

private fun WriteOffTask.getProductsPrint(): List<PrintProduct> {
    return taskRepository.getWriteOffReasons()
            .getWriteOffReasons().map {
                PrintProduct(
                        materialNumber = it.materialNumber,
                        quantity = it.count,
                        uomCode = taskRepository.getProducts().findProduct(it.materialNumber)?.uom?.code.orEmpty(),
                        reasonCode = it.writeOffReason.code
                )
            }
}


fun WriteOffTask.getReport(): SendWriteOffDataParams {
    with(taskDescription) {
        return SendWriteOffDataParams(
                perNo = perNo,
                printer = printer,
                taskName = taskName,
                taskType = taskType.code,
                tkNumber = tkNumber,
                storloc = stock,
                ipAdress = ipAddress,
                materials = getMaterials(),
                exciseStamps = getStamps()
        )
    }
}

fun WriteOffTask.getStamps(): List<ExciseStamp> {
    return taskRepository.getExciseStamps().getExciseStamps().map {
        ExciseStamp(
                matnr = it.materialNumber,
                stamp = it.code,
                matnrOsn = it.setMaterialNumber,
                writeOffCause = it.writeOffReason,
                packNumber = it.packNumber,
                reg = if (it.isBadStamp) "X" else ""
        )
    }
}

fun WriteOffTask.getMaterials(): List<MaterialNumber> {

    return taskRepository.getWriteOffReasons()
            .getWriteOffReasons().map {
                MaterialNumber(
                        matnr = it.materialNumber,
                        writeOffCause = it.writeOffReason.code,
                        kostl = "",
                        amount = it.count.toString()
                )
            }
}