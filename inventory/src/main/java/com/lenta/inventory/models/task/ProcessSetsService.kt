package com.lenta.inventory.models.task

import com.lenta.inventory.features.goods_information.sets.SetComponentInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.getComponentsForSet
import com.lenta.shared.fmp.resources.dao_ext.getProductInfo
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

@AppScope
class ProcessSetsService @Inject constructor() : IProcessProductService {

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    private var currentProductInfo: TaskProductInfo? = null
    private val currentComponentExciseStamps: ArrayList<TaskExciseStamp> = ArrayList()
    private val currentAllExciseStamps: ArrayList<TaskExciseStamp> = ArrayList()
    private val componentsInfo: ArrayList<SetComponentInfo> = ArrayList()

    private val zmpUtz46V001: ZmpUtz46V001 by lazy {
        ZmpUtz46V001(hyperHive)
    }

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    private val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    fun newProcessSetsService(productInfo: TaskProductInfo): ProcessSetsService? {
        return if (productInfo.type == ProductType.ExciseAlcohol && productInfo.isSet) {
            currentProductInfo = productInfo.сopy()
            currentAllExciseStamps.clear()
            currentComponentExciseStamps.clear()
            setComponentsForSet()
            return this
        } else null
    }

    override fun getFactCount(): Double? {
        return currentProductInfo?.factCount
    }

    override fun setFactCount(count: Double) {
        if (count >= 0.0) {
            if (count > 0.0) {
                processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        changeProduct(currentProductInfo!!.сopy(factCount = count, isPositionCalc = true))
            } else {
                processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        changeProduct(currentProductInfo!!.сopy(factCount = 0.0, isPositionCalc = false))
            }
        }
    }

    override fun markMissing() {
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                changeProduct(currentProductInfo!!.сopy(factCount = 0.0, isPositionCalc = true))
        discard()
    }

    fun getComponentsForSet(): List<SetComponentInfo> {
        return componentsInfo
    }

    private fun setComponentsForSet(): List<SetComponentInfo> {
        componentsInfo.clear()

        zmpUtz46V001.getComponentsForSet(currentProductInfo!!.materialNumber).map { data ->
            zfmpUtz48V001.getProductInfo(data.matnr).map {
                val uomInfo = zmpUtz07V001.getUomInfo(data.meins)
                componentsInfo.add(SetComponentInfo(
                        setNumber = data.matnrOsn,
                        number = data.matnr,
                        name = it.name,
                        count = data.menge.toString(),
                        uom = Uom(code = uomInfo!!.uom, name = uomInfo.name),
                        matrixType = getMatrixType(it.matrType),
                        sectionId = it.abtnr,
                        typeProduct = getProductType(it.isAlco == "X", it.isExc == "X"),
                        placeCode = currentProductInfo!!.placeCode
                ))
            }
        }
        return componentsInfo
    }

    fun getCountExciseStampsForComponents(): Int {
        return currentComponentExciseStamps.size
    }

    fun getCountExciseStampsForComponent(componentsInfo: SetComponentInfo): Int {
        return currentComponentExciseStamps.filter { it.materialNumber == componentsInfo.number }.size
    }

    fun clearExciseStampsForComponent(componentsInfo: SetComponentInfo) {
        currentComponentExciseStamps.filter { stamp ->
            stamp.materialNumber == componentsInfo.number
        }.let {
            currentComponentExciseStamps.removeAll(it)
        }
    }

    fun addCurrentComponentExciseStamp(exciseStamp: TaskExciseStamp) {
        currentComponentExciseStamps.add(exciseStamp)
    }

    fun rollback(): Int {
        currentComponentExciseStamps.removeAt(currentComponentExciseStamps.lastIndex)
        return currentComponentExciseStamps.size
    }

    fun applyComponentsExciseStamps() {
        currentAllExciseStamps.addAll(currentComponentExciseStamps)
        currentComponentExciseStamps.clear()
    }

    fun apply(factCount: Double) {
        setFactCount(factCount)
        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamps(currentAllExciseStamps)
        currentAllExciseStamps.clear()
        currentComponentExciseStamps.clear()
    }

    fun discard() {
        currentProductInfo = null
        componentsInfo.clear()
        currentAllExciseStamps.clear()
        currentComponentExciseStamps.clear()
    }

    fun isTaskAlreadyHasExciseStamp(stampCode: String): Boolean {
        return currentComponentExciseStamps.any { stamp ->
            stamp.code == stampCode
        } || currentAllExciseStamps.any { stamp ->
            stamp.code == stampCode
        } || processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().getExciseStamps().any { stamp ->
            stamp.code == stampCode
        }
    }
}