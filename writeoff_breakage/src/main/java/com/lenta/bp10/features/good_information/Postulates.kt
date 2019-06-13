package com.lenta.bp10.features.good_information

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.utilities.extentions.toStringFormatted

fun getCountWithUom(count: Double?, productInfo: MutableLiveData<ProductInfo>): String {
    productInfo.value.let {
        if (it == null) {
            return count.toStringFormatted()
        }
        return "${count.toStringFormatted()} ${it.uom.name}"
    }
}

fun isEnabledApplyButtons(count: Double?,
                          productInfo: ProductInfo?,
                          reason: WriteOffReason,
                          taskRepository: ITaskRepository): Boolean {
    return count != 0.0 &&
            reason != WriteOffReason.empty &&
            productInfo != null &&
            taskRepository.getTotalCountForProduct(productInfo, reason) + (count ?: 0.0) >= 0.0
}

fun isEnabledDetailsButton(totalProcessCount: Double): Boolean {
    return totalProcessCount > 0.0
}



