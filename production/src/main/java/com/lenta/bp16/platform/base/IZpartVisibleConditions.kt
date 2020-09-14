package com.lenta.bp16.platform.base

import com.lenta.bp16.model.ProducerDataStatus
import com.lenta.shared.utilities.orIfNull

interface IZpartVisibleConditions : IZpartInfo {

    val producerConditions: Pair<Boolean, Boolean>
        get() {
            return zPartDataInfo.value?.let { zPartDataInfoValue ->

                val producersList = zPartDataInfoValue.map { it.prodName }

                var fullItemCount = 0
                for (zPartName in producersList) {
                    if (zPartName.isNotEmpty()) {
                        fullItemCount++ //Считаем количество не пустых полей в списке
                    }
                }

                val visibleStatus = when {
                    (fullItemCount == 0) -> ProducerDataStatus.GONE
                    (fullItemCount == producersList.size) -> ProducerDataStatus.VISIBLE
                    else -> ProducerDataStatus.ALERT
                }

                return when (visibleStatus) {
                    ProducerDataStatus.GONE -> false to false
                    ProducerDataStatus.VISIBLE -> true to false
                    ProducerDataStatus.ALERT -> true to true
                }

            }.orIfNull {
                false to false
            }
        }
}