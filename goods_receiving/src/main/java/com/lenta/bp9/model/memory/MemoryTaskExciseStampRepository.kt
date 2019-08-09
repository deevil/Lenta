package com.lenta.bp9.model.memory

import com.lenta.bp9.model.ReceivingExciseStamp
import com.lenta.bp9.model.ReceivingProductInfo
import com.lenta.bp9.model.repositories.ITaskExciseStampRepository

class MemoryTaskExciseStampRepository : ITaskExciseStampRepository {

    private val stamps: ArrayList<ReceivingExciseStamp> = ArrayList()

    override fun getExciseStamps(): List<ReceivingExciseStamp> {
        return stamps
    }

    override fun findExciseStampsOfProduct(product: ReceivingProductInfo): List<ReceivingExciseStamp> {
        return findExciseStampsOfProduct(product.materialNumber, product.isSet)
    }

    override fun findExciseStampsOfProduct(materialNumber: String, isSet: Boolean): List<ReceivingExciseStamp> {
        return stamps.filter {stamp ->
            (stamp.materialNumber == materialNumber) ||
                    (isSet && stamp.setMaterialNumber == materialNumber)}
    }

    override fun addExciseStamp(exciseStamp: ReceivingExciseStamp): Boolean {
        var index = -1
        for (i in stamps.indices) {
            if (exciseStamp.code == stamps[i].code) {
                index = i
            }
        }

        if (index == -1) {
            stamps.add(exciseStamp)
            return true
        }
        return false
    }

    override fun addExciseStamps(exciseStamps: List<ReceivingExciseStamp>): Boolean {
        val distinctStamp = ArrayList<ReceivingExciseStamp>()
        for (i in exciseStamps.indices) {
            //убираем дубликаты
            if (!distinctStamp.contains(exciseStamps[i])) {
                distinctStamp.add(exciseStamps[i])
            }
        }

        stamps.removeAll(distinctStamp)
        stamps.addAll(distinctStamp)
        return true
    }

    override fun deleteExciseStamp(exciseStamp: ReceivingExciseStamp): Boolean {
        stamps.map { it }.filter {stamp ->
            if (exciseStamp.code == stamp.code) {
                stamps.remove(stamp)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteExciseStamps(exciseStamps: List<ReceivingExciseStamp>): Boolean {
        return stamps.removeAll(exciseStamps)
    }

    override fun deleteExciseStampsForProduct(product: ReceivingProductInfo): Boolean {
        stamps.map { it }.filter {stamp ->
            if ((stamp.materialNumber == product.materialNumber) ||
                    (product.isSet && stamp.setMaterialNumber == product.materialNumber)) {
                stamps.remove(stamp)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun clear() {
        stamps.clear()
    }
}