package com.lenta.inventory.models.task

import com.lenta.inventory.features.goods_information.excise_alco.GoodsInfoCountExciseStamps
import com.lenta.inventory.features.goods_information.excise_alco.GoodsInfoCountType
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.EgaisStampVersion
import com.lenta.shared.models.core.ExciseStamp
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

@AppScope
class ProcessExciseAlcoProductService
@Inject constructor() : IProcessProductService {

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    private var currentProductInfo: TaskProductInfo? = null
    private val currentExciseStamps: ArrayList<TaskExciseStamp> = ArrayList()
    private val currentCountExciseStamps: ArrayList<GoodsInfoCountExciseStamps> = ArrayList()

    fun newProcessExciseAlcoProductService(productInfo: TaskProductInfo) : ProcessExciseAlcoProductService? {
        return if (productInfo.type == ProductType.ExciseAlcohol){
            currentProductInfo = productInfo.copy()
            currentExciseStamps.clear()
            currentCountExciseStamps.clear()
            this
        }
        else null
    }

    override fun getFactCount(): Double? {
        return currentProductInfo?.factCount
    }

    override fun setFactCount(count: Double){
        if (count >= 0.0) {
            if (count > 0.0) {
                processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        changeProduct(currentProductInfo!!.copy(factCount = count, isPositionCalc = true))
            } else {
                processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        changeProduct(currentProductInfo!!.copy(factCount = 0.0, isPositionCalc = false))
            }
        }
    }

    override fun markMissing(){
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                changeProduct(currentProductInfo!!.copy(factCount = 0.0, isPositionCalc = true))
        discard()
    }

    //for stamps 150
    fun addCurrentExciseStamp(exciseStamp: TaskExciseStamp){
        currentCountExciseStamps.add(
                GoodsInfoCountExciseStamps(
                    countLastExciseStamp = 1,
                    countType = GoodsInfoCountType.VINTAGE.number
                )
        )
        currentProductInfo = currentProductInfo!!.copy(factCount = currentProductInfo!!.factCount + 1)
        currentExciseStamps.add(exciseStamp)
    }

    //for box
    fun addCurrentExciseStamps(exciseStamps: List<TaskExciseStamp>){
        currentCountExciseStamps.add(
                GoodsInfoCountExciseStamps(
                        countLastExciseStamp = exciseStamps.size,
                        countType = GoodsInfoCountType.VINTAGE.number
                )
        )
        currentProductInfo = currentProductInfo!!.copy(factCount = currentProductInfo!!.factCount + exciseStamps.size)
        currentExciseStamps.addAll(exciseStamps)
    }

    //for stamps 68
    fun add(count: Int, exciseStamp: TaskExciseStamp){
        currentCountExciseStamps.add(
                GoodsInfoCountExciseStamps(
                        countLastExciseStamp = count,
                        countType = GoodsInfoCountType.PARTLY.number
                )
        )
        currentProductInfo = currentProductInfo!!.copy(factCount = currentProductInfo!!.factCount + count)
        currentExciseStamps.add(exciseStamp)
    }

    fun rollback() : GoodsInfoCountExciseStamps{
        currentProductInfo = currentProductInfo!!.
                                            copy(
                                                factCount = currentProductInfo!!.factCount - currentCountExciseStamps.last().countLastExciseStamp,
                                                isPositionCalc = currentProductInfo!!.factCount > 0.0
        )
        currentExciseStamps.removeAt(currentExciseStamps.lastIndex)
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
        } || processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().getExciseStamps().any {repExciseStamp ->
            repExciseStamp.code == stampCode
        }
    }

    fun isTaskAlreadyHasExciseStampBox(boxNumber: String): Boolean{
        return currentExciseStamps.any { currExciseStamp ->
            currExciseStamp.boxNumber == boxNumber
        } || processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().getExciseStamps().any {repExciseStamp ->
            repExciseStamp.boxNumber == boxNumber
        }
    }

    fun apply(){
        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getProducts().
                changeProduct(currentProductInfo!!.copy(isPositionCalc = currentProductInfo!!.factCount > 0.0))

        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().addExciseStamps(currentExciseStamps)
        discard()
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
        currentProductInfo = null
        currentExciseStamps.clear()
        currentCountExciseStamps.clear()
    }

    fun getCountPartlyStamps() : Int{
        var countPartlyStamps = 0
        currentCountExciseStamps.map {
            if (it.countType == GoodsInfoCountType.PARTLY.number){
                countPartlyStamps += it.countLastExciseStamp
            }
        }

        countPartlyStamps +=
                (processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        findProduct(currentProductInfo!!)!!.
                        factCount.toInt() -
                        processServiceManager.
                                getInventoryTask()!!.
                                taskRepository.
                                getExciseStamps().
                                findExciseStampsOfProduct(currentProductInfo!!).
                                filter {
                                    ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V3.version
                                }.size)

        return countPartlyStamps
    }

    fun getCountVintageStamps() : Int{
        var countVintageStamps = 0
        currentCountExciseStamps.map {
            if (it.countType == GoodsInfoCountType.VINTAGE.number){
                countVintageStamps += it.countLastExciseStamp
            }
        }

        countVintageStamps += processServiceManager.
                                    getInventoryTask()!!.
                                    taskRepository.
                                    getExciseStamps().
                                    findExciseStampsOfProduct(currentProductInfo!!).
                                    filter {
                                        ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V3.version
                                    }.size

        return countVintageStamps
    }

    fun delAllPartlyStamps(){
        currentProductInfo = currentProductInfo!!.copy(factCount = currentProductInfo!!.factCount - getCountPartlyStamps())

        currentCountExciseStamps.filter { goodsInfoCountExciseStamps ->
            goodsInfoCountExciseStamps.countType == GoodsInfoCountType.PARTLY.number
        }.let {
            currentCountExciseStamps.removeAll(it)
        }

        currentExciseStamps.filter { taskExciseStamp ->
            ExciseStamp.getEgaisVersion(taskExciseStamp.code) == EgaisStampVersion.V2.version
        }.let {
            currentExciseStamps.removeAll(it)
        }

        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getExciseStamps().
                findExciseStampsOfProduct(currentProductInfo!!).
                map {
                    if (ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V2.version){
                        processServiceManager.
                                getInventoryTask()!!.
                                taskRepository.
                                getExciseStamps().
                                deleteExciseStamp(it)
                    }
                }

        val countPartlyStamps =
                (processServiceManager.
                        getInventoryTask()!!.
                        taskRepository.
                        getProducts().
                        findProduct(currentProductInfo!!)!!.
                        factCount.toInt()
                        -
                        processServiceManager.
                                getInventoryTask()!!.
                                taskRepository.
                                getExciseStamps().
                                findExciseStampsOfProduct(currentProductInfo!!).
                                filter {
                                    ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V3.version
                                }.size)

        setFactCount(processServiceManager.
                            getInventoryTask()!!.
                            taskRepository.
                            getProducts().
                            findProduct(currentProductInfo!!)!!.
                            factCount - countPartlyStamps)
    }

    fun delAllVintageStamps(){
        currentProductInfo = currentProductInfo!!.copy(factCount = currentProductInfo!!.factCount - getCountVintageStamps())

        currentCountExciseStamps.filter { goodsInfoCountExciseStamps ->
            goodsInfoCountExciseStamps.countType == GoodsInfoCountType.VINTAGE.number
        }.let {
            currentCountExciseStamps.removeAll(it)
        }

        currentExciseStamps.filter { taskExciseStamp ->
            ExciseStamp.getEgaisVersion(taskExciseStamp.code) == EgaisStampVersion.V3.version
        }.let {
            currentExciseStamps.removeAll(it)
        }

        val countVintageStampsRepository = processServiceManager.
                                            getInventoryTask()!!.
                                            taskRepository.
                                            getExciseStamps().
                                            findExciseStampsOfProduct(currentProductInfo!!).
                                            filter {
                                                ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V3.version
                                            }.size

        processServiceManager.
                getInventoryTask()!!.
                taskRepository.
                getExciseStamps().
                findExciseStampsOfProduct(currentProductInfo!!).
                map {
                    if (ExciseStamp.getEgaisVersion(it.code) == EgaisStampVersion.V3.version){
                        processServiceManager.getInventoryTask()!!.taskRepository.getExciseStamps().deleteExciseStamp(it)
                    }
                }

        setFactCount(processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(currentProductInfo!!)!!.factCount - countVintageStampsRepository)

    }
}