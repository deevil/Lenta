package com.lenta.movement.features.task.basket.info

import android.content.Context
import com.lenta.movement.R
import com.lenta.movement.models.Basket
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.isGisControl
import com.lenta.movement.models.toDescriptionResId
import javax.inject.Inject

class BasketPropertiesExtractor @Inject constructor(
        private val context: Context,
        private val taskManager: ITaskManager
) : IBasketPropertiesExtractor {
    override suspend fun extractProperties(basket: Basket?): List<BasketProperty> {
        if (basket == null) return listOf()

        val properties = mutableListOf<BasketProperty>()
        val diffSigns = taskManager.getTaskSettings().signsOfDiv
        for (diffSign in diffSigns) {
            if(diffSign.isGisControl()) continue

            val property = if (diffSign.needStringValue) {
                basket.getStringDescription(diffSign)
            } else {
                val propertyRes = if (basket.isDivisionTrue(diffSign)) R.string.yes else R.string.no
                context.getString(propertyRes)
            }

            properties.add(BasketProperty(diffSign.toDescriptionResId(), property))
        }
        return properties
    }
}