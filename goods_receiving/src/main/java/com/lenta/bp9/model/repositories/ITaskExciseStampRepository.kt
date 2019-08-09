package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.ReceivingExciseStamp
import com.lenta.bp9.model.ReceivingProductInfo

interface ITaskExciseStampRepository {
    fun getExciseStamps(): List<ReceivingExciseStamp>
    fun findExciseStampsOfProduct(product: ReceivingProductInfo): List<ReceivingExciseStamp>
    fun findExciseStampsOfProduct(materialNumber: String, isSet: Boolean): List<ReceivingExciseStamp>
    fun addExciseStamp(exciseStamp: ReceivingExciseStamp): Boolean
    fun addExciseStamps(exciseStamps: List<ReceivingExciseStamp>): Boolean
    fun deleteExciseStamp(exciseStamp: ReceivingExciseStamp): Boolean
    fun deleteExciseStamps(exciseStamps: List<ReceivingExciseStamp>): Boolean
    fun deleteExciseStampsForProduct(product: ReceivingProductInfo): Boolean
    fun clear()
}