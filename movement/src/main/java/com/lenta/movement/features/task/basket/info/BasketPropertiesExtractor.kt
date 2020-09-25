package com.lenta.movement.features.task.basket.info

import com.lenta.movement.R
import com.lenta.movement.models.*
import com.lenta.movement.platform.IResourceManager
import javax.inject.Inject

class BasketPropertiesExtractor @Inject constructor(
        private val resourceManager: IResourceManager,
        private val taskManager: ITaskManager
) : IBasketPropertiesExtractor {
    override suspend fun extractProperties(basket: Basket?): List<BasketProperty> {
        if (basket == null) return listOf()

        val properties = mutableListOf<BasketProperty>()
        val diffSigns = taskManager.getTaskSettings().signsOfDiv
        for (diffSign in diffSigns) {
            if (diffSign.isGisControl()) continue

            properties.add(BasketProperty(diffSign.toDescriptionResId(), getProperty(diffSign, basket)))
        }
        return properties
    }

    private fun getProperty(diffSign: GoodsSignOfDivision, basket: Basket) = if (diffSign.needStringValue) {
        basket.getStringDescription(diffSign)
    } else {
        if (basket.isDivisionTrue(diffSign)) resourceManager.yes else resourceManager.no
    }
}