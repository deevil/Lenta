package com.lenta.bp16.platform.base

import androidx.lifecycle.LiveData
import com.lenta.bp16.model.ProducerDataStatus
import com.lenta.shared.utilities.extentions.mapSkipNulls

interface IZpartVisibleConditions : IZpartInfo {

    val producerConditions: LiveData<Pair<Boolean, Boolean>>
        get() {
            return zPartDataInfos.mapSkipNulls { zPartDataInfoValue ->

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

                when (visibleStatus) {
                    ProducerDataStatus.GONE -> false to false
                    ProducerDataStatus.VISIBLE -> true to false
                    ProducerDataStatus.ALERT -> true to true
                }
            }
        }
}