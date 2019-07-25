package com.lenta.inventory.models.task

import com.lenta.inventory.features.goods_information.sets.SetComponentInfo
import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.inventory.requests.network.SetComponentsRestInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.fmp.resources.dao_ext.getComponentsForSet
import com.lenta.shared.fmp.resources.dao_ext.getProductInfo
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

@AppScope
class ProcessSetsService@Inject constructor() : IProcessProductService {

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    private val taskRepository: ITaskRepository by lazy {
        processServiceManager.getInventoryTask()!!.taskRepository!!
    }
    private lateinit var productInfo: TaskProductInfo

    private val currentComponentExciseStamps: ArrayList<TaskExciseStamp> = ArrayList()
    private val currentAllExciseStamps: ArrayList<TaskExciseStamp> = ArrayList()
    private val componentsInfo: ArrayList<SetComponentInfo> = ArrayList()

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    private val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    fun newProcessSetsService(productInfo: TaskProductInfo, componentsRestInfo: List<SetComponentsRestInfo>) : ProcessSetsService {
        this.productInfo = productInfo
        setComponentsForSet(componentsRestInfo)
        return this
    }

    override fun getFactCount(): Double {
        return  productInfo.factCount
    }

    override fun setFactCount(count: Double){
        if (count >= 0.0) {
            if (count > 0.0) {
                taskRepository.getProducts().findProduct(productInfo)?.factCount = count
                taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = true
            } else {
                taskRepository.getProducts().findProduct(productInfo)?.factCount = 0.0
                taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = false
            }
        }
    }

    override fun markMissing(){
        taskRepository.getProducts().findProduct(productInfo)?.factCount = 0.0
        taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = true
    }

    fun getComponentsForSet() : List<SetComponentInfo>{
        return componentsInfo
    }

    private fun setComponentsForSet(componentsRestInfo: List<SetComponentsRestInfo>) : List<SetComponentInfo>{
        componentsInfo.clear()
        componentsRestInfo[0].data.filter{ data ->
            data[1] == productInfo.materialNumber
        }.map {data ->
            zfmpUtz48V001.getProductInfo(data[2]).map {
                val uomInfo = zmpUtz07V001.getUomInfo(data[4])
                componentsInfo.add(SetComponentInfo(
                        setNumber = data[1],
                        number = data[2],
                        name = it.name,
                        count = data[3],
                        uom = Uom(code = uomInfo!!.uom, name = uomInfo!!.name),
                        matrixType = getMatrixType(it.matrType),
                        sectionId = it.abtnr,
                        typeProduct = getProductType(it.isAlco == "X", it.isExc == "X"),
                        placeCode = productInfo.placeCode
                ))
            }
        }
        return componentsInfo
    }

    fun getCountExciseStampsForComponent(componentsInfo: SetComponentInfo) : Int{
        return currentAllExciseStamps.filter {it.materialNumber == componentsInfo.number}.size +
                taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo).filter {it.materialNumber == componentsInfo.number}.size
    }

    fun clearExciseStampsForComponent(componentsInfo: SetComponentInfo){
        currentAllExciseStamps.map {stamp ->
            if (stamp.materialNumber == componentsInfo.number){
                currentAllExciseStamps.remove(stamp)
            }
        }

        taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo).
                filter {stamp ->
                    stamp.materialNumber == componentsInfo.number
                }.
                let {stamps ->
                    taskRepository.getExciseStamps().deleteExciseStamps(stamps)
                }
    }

    fun addCurrentComponentExciseStamps(exciseStamp: TaskExciseStamp){
        currentComponentExciseStamps.add(exciseStamp)
    }

    fun rollback() : Int{
        currentComponentExciseStamps.removeAt(currentComponentExciseStamps.lastIndex)
        return currentComponentExciseStamps.size
    }

    fun applyComponent(){
        currentAllExciseStamps.addAll(currentComponentExciseStamps)
        currentComponentExciseStamps.clear()
    }

    fun discardComponent(){
        currentComponentExciseStamps.clear()
    }

    fun apply(){
        taskRepository.getExciseStamps().addExciseStamps(currentAllExciseStamps)
        currentAllExciseStamps.clear()
        currentComponentExciseStamps.clear()
    }

    fun discard(){
        currentAllExciseStamps.clear()
        currentComponentExciseStamps.clear()
    }

    fun isTaskAlreadyHasExciseStamp(stampCode: String): Boolean{
        return currentComponentExciseStamps.any { stamp ->
            stamp.code == stampCode
        } || currentAllExciseStamps.any { stamp ->
            stamp.code == stampCode
        } || taskRepository.getExciseStamps().getExciseStamps().any {stamp ->
            stamp.code == stampCode
        }
    }
}