package com.lenta.inventory.models.task

import com.lenta.inventory.features.goods_information.excise_alco.GoodsInfoCountExciseStamps
import com.lenta.inventory.features.goods_information.excise_alco.GoodsInfoCountType
import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.shared.models.core.EgaisStampVersion
import com.lenta.shared.models.core.ExciseStamp

class ProcessExciseAlcoProductService(private val taskDescription: TaskDescription,
                                      private val taskRepository: ITaskRepository,
                                      private val productInfo: TaskProductInfo) : IProcessProductService {

    private var currentProductInfo: TaskProductInfo = productInfo
    private val currentExciseStamps: ArrayList<TaskExciseStamp> = ArrayList()
    private val currentCountExciseStamps: ArrayList<GoodsInfoCountExciseStamps> = ArrayList()

    override fun getFactCount(): Double {
        return  currentProductInfo.factCount
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

    //for stamps 150
    fun addCurrentExciseStamp(exciseStamp: TaskExciseStamp){
        currentCountExciseStamps.add(
                GoodsInfoCountExciseStamps(
                    countLastExciseStamp = 1,
                    countType = GoodsInfoCountType.VINTAGE.number
                )
        )
        currentProductInfo.factCount += 1
        currentExciseStamps.add(exciseStamp)
        apply()
    }

    //for box
    fun addCurrentExciseStamps(exciseStamps: List<TaskExciseStamp>){
        currentCountExciseStamps.add(
                GoodsInfoCountExciseStamps(
                        countLastExciseStamp = exciseStamps.size,
                        countType = GoodsInfoCountType.VINTAGE.number
                )
        )
        currentProductInfo.factCount += exciseStamps.size
        currentExciseStamps.addAll(exciseStamps)
        apply()
    }

    //for stamps 68
    fun add(count: Int, exciseStamp: TaskExciseStamp){
        currentCountExciseStamps.add(
                GoodsInfoCountExciseStamps(
                        countLastExciseStamp = count,
                        countType = GoodsInfoCountType.PARTLY.number
                )
        )
        currentProductInfo.factCount += count
        currentExciseStamps.add(exciseStamp)
        apply()
    }

    fun rollback() : GoodsInfoCountExciseStamps{
        currentProductInfo.factCount -= currentCountExciseStamps.last().countLastExciseStamp
        taskRepository.getProducts().findProduct(productInfo)!!.factCount = currentProductInfo.factCount
        for (i in 1..currentCountExciseStamps.last().countLastExciseStamp) {
            taskRepository.getExciseStamps().deleteExciseStamp(currentExciseStamps[currentExciseStamps.lastIndex])
            currentExciseStamps.removeAt(currentExciseStamps.lastIndex)
        }
        currentCountExciseStamps.removeAt(currentCountExciseStamps.lastIndex)
        return GoodsInfoCountExciseStamps(
                countLastExciseStamp = currentCountExciseStamps.let {countExciseStampsList ->
                    countExciseStampsList.isNullOrEmpty().let {
                        if (it) 0 else countExciseStampsList.last().countLastExciseStamp
                    }
                },
                countType = currentCountExciseStamps.let {countExciseStampsList ->
                    countExciseStampsList.isNullOrEmpty().let {
                        if (it) GoodsInfoCountType.QUANTITY.number else countExciseStampsList.last().countType
                    }
                }
        )
    }

    fun isTaskAlreadyHasExciseStamp(stampCode: String): Boolean{
        return currentExciseStamps.any { currExciseStamp ->
            currExciseStamp.code == stampCode
        } || taskRepository.getExciseStamps().getExciseStamps().any {repExciseStamp ->
            repExciseStamp.code == stampCode
        }
    }

    fun isTaskAlreadyHasExciseStampBox(boxNumber: String): Boolean{
        return currentExciseStamps.any { currExciseStamp ->
            currExciseStamp.boxNumber == boxNumber
        } || taskRepository.getExciseStamps().getExciseStamps().any {repExciseStamp ->
            repExciseStamp.boxNumber == boxNumber
        }
    }

    fun apply(){
        taskRepository.getProducts().findProduct(productInfo)!!.factCount = currentProductInfo.factCount
        taskRepository.getExciseStamps().addExciseStamps(currentExciseStamps)
    }

    fun isLinkingOldStamps(): Boolean{
        return taskDescription.linkOldStamp
    }

    fun updateCurrentData(){
        val delExciseStamps: ArrayList<TaskExciseStamp> = ArrayList()
        delExciseStamps.clear()
        val delCountExciseStamps: ArrayList<GoodsInfoCountExciseStamps> = ArrayList()
        delCountExciseStamps.clear()

        currentExciseStamps.forEach { taskExciseStamp ->
            taskRepository.
                    getExciseStamps().
                    findExciseStampsOfProduct(currentProductInfo).
                    filter {
                        taskExciseStamp.code == it.code
                    }.isNullOrEmpty().
                    let {
                        if (it) {
                            delExciseStamps.add(taskExciseStamp)
                        }
                    }
        }

        currentExciseStamps.removeAll(delExciseStamps)
        delExciseStamps.clear()
        currentProductInfo.factCount = taskRepository.getProducts().findProduct(currentProductInfo)!!.factCount

        taskRepository.
                getExciseStamps().
                findExciseStampsOfProduct(currentProductInfo).
                filter {
                    ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V2.version
                }.isNullOrEmpty().
                let {
                    if (it) {
                        currentCountExciseStamps.filter {countExciseStamps ->
                            countExciseStamps.countType == GoodsInfoCountType.PARTLY.number
                        }.let {countExciseStampsList ->
                            delCountExciseStamps.addAll(countExciseStampsList)
                        }
                    }
                }
        currentCountExciseStamps.removeAll(delCountExciseStamps)
        delCountExciseStamps.clear()

        taskRepository.
                getExciseStamps().
                findExciseStampsOfProduct(currentProductInfo).
                filter {
                    ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V3.version
                }.isNullOrEmpty().
                let {
                    if (it) {
                        currentCountExciseStamps.filter {countExciseStamps ->
                            countExciseStamps.countType == GoodsInfoCountType.VINTAGE.number
                        }.let {countExciseStampsList ->
                            delCountExciseStamps.addAll(countExciseStampsList)
                        }
                    }
                }
        currentCountExciseStamps.removeAll(delCountExciseStamps)
        delCountExciseStamps.clear()
    }

    fun getLastCountExciseStamp() : GoodsInfoCountExciseStamps{
        return GoodsInfoCountExciseStamps(
                countLastExciseStamp = currentCountExciseStamps.let {countExciseStampsList ->
                    countExciseStampsList.isNullOrEmpty().let {
                        if (it) 0 else countExciseStampsList.last().countLastExciseStamp
                    }
                },
                countType = currentCountExciseStamps.let {countExciseStampsList ->
                    countExciseStampsList.isNullOrEmpty().let {
                        if (it) GoodsInfoCountType.QUANTITY.number else countExciseStampsList.last().countType
                    }
                }
        )
    }

    fun discard(){
        taskRepository.getProducts().findProduct(productInfo)!!.factCount -= currentProductInfo.factCount
        taskRepository.getExciseStamps().deleteExciseStamps(currentExciseStamps)
    }
}