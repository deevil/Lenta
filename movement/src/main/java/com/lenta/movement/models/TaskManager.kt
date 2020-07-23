package com.lenta.movement.models

import com.lenta.movement.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

@Suppress("INACCESSIBLE_TYPE")
class TaskManager(
        hyperHive: HyperHive
) : ITaskManager {

    private val taskSettingsTable = ZmpUtz47V001(hyperHive).localHelper_ET_TASK_TPS
    private val pickingStorageTable = ZmpUtz48V001(hyperHive).localHelper_ET_LGORT_SRC
    private val allowProductsTable = ZmpUtz49V001(hyperHive).localHelper_ET_ALLOW_MATNR
    private val excludeProductsTable = ZmpUtz50V001(hyperHive).localHelper_ET_EXCLUDE_MATNR
    private val receiversTable = ZmpUtz79V001(hyperHive).localHelper_ET_PLANTS
    private val printerTable = ZmpUtz26V001(hyperHive).localHelper_ET_PRINTERS
    private val goodsTable = ZmpUtz30V001(hyperHive).localHelper_ET_MATERIALS
    private val taskTypeTable = ZmpUtz102V001(hyperHive).localHelper_ET_MVM_TXT

    private var task: Task? = null

    override fun getTask(): Task {
        return task ?: error("Task not defined!")
    }

    override fun getTaskOrNull(): Task? {
        return task
    }

    override fun setTask(task: Task) {
        this.task = task
    }

    override suspend fun getAvailableReceivers(): List<String> {
        return withContext(Dispatchers.IO) {
            receiversTable.all
                    .map { table ->
                        table.plant
                    }
                    .distinct()
        }
    }

    override suspend fun getAvailablePikingStorageList(
            taskType: TaskType,
            movementType: MovementType
    ): List<String> {
        return withContext(Dispatchers.IO) {
            pickingStorageTable.all.asSequence()
                    .filter {
                        it.taskType == taskType && it.mvmType == movementType
                    }
                    .map {
                        it.lgortSource
                    }
                    .toList()
                    .distinct()
        }
    }

    override suspend fun isAllowProduct(product: ProductInfo): Boolean {
        return withContext(Dispatchers.IO) {
            allowProductsTable.all.any {
                it.taskType == task?.taskType &&
                        it.taskCntrl == task?.movementType?.propertyName &&
                        it.ekgrp == product.ekGroup &&
                        it.matkl == product.matkl &&
                        it.mtart == product.materialType
            }
        }
    }

    override suspend fun isDisallowProduct(product: ProductInfo): Boolean {
        val productEkGroup = product.ekGroup
        val productMatkl = product.matkl
        val productMaterialType = product.materialType
        val goodCodeForPictogram =
                if (product.isAlco) ALCO_GOOD_CODE_FOR_PICTOGRAM
                else USUAL_GOOD_CODE_FOR_PICTOGRAM

        return withContext(Dispatchers.IO) {
            excludeProductsTable.all.asSequence()
                    .filter {
                        it.taskType == task?.taskType
                    }
                    .any { inputProduct ->
                        with(inputProduct) {
                            taskCntrl == goodCodeForPictogram &&
                                    ekgrp == productEkGroup &&
                                    matkl == productMatkl &&
                                    mtart == productMaterialType
                        }
                    }
        }
    }

    override fun clear() {
        task = null
    }

    override suspend fun getPrinterName(): String {
        return withContext(Dispatchers.IO) {
            printerTable.all.first().printerName
        }
    }

    override suspend fun getGoodName(goodNumber: String?): String {
        return withContext(Dispatchers.IO) {
            goodNumber?.let {
                goodsTable.getWhere(
                        GET_GOODNAME_BY_GOODNUMBER.format(goodNumber)
                )
                        .first()
                        .name
            }.orIfNull {
                Logg.e { "goodNumber null" }
                ""
            }
        }
    }

    override suspend fun getMovementType(movementType: MovementType): String {
        val propertyName = movementType.propertyName
        return withContext(Dispatchers.IO) {
            taskSettingsTable.getWhere(
                    GET_MVM_TYPE_BY_MVM_CODE.format(propertyName)
            )
                    .first()
                    .annotation
        }
    }

    override suspend fun getMovementTypeShort(movementType: MovementType): String {
        val propertyName = movementType.propertyName
        return withContext(Dispatchers.IO) {
            taskTypeTable.getWhere(
                    GET_MVM_SHORT_TYPE_BY_MVM_CODE.format(propertyName)
            )
                    .first()
                    .taskTypeTxt.orEmpty()
        }
    }

    override suspend fun getTaskSettings(taskType: TaskType, movementType: MovementType): TaskSettings {
        return withContext(Dispatchers.IO) {
            val gisControls = allowProductsTable.all.asSequence()
                    .filter {
                        it.taskType == taskType
                    }
                    .map {
                        when (it.taskCntrl) {
                            ALCO_GOOD_CODE_FOR_PICTOGRAM -> GisControl.Alcohol
                            else -> GisControl.GeneralProduct
                        }
                    }.toSet()

            val results = taskSettingsTable.all.filter {
                it.taskType == taskType && it.mvmType == movementType
            }

            val signOfDivision = mutableSetOf<GoodsSignOfDivision>()

            if (results.first().divMarkParts.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.MARK_PARTS)
            }

            if (results.first().divAlco.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.ALCO)
            }

            if (results.first().divUsual.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.USUAL)
            }

            if (results.first().divVet.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.VET)
            }

            if (results.first().divParts.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.PARTS)
            }

            if (results.first().divMtart.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.MTART)
            }

            if (results.first().divFood.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.FOOD)
            }

            if (results.first().divLifnr.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.LIF_NUMBER)
            }

            if (results.first().divMatnr.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.MATERIAL_NUMBER)
            }
            TaskSettings(
                    description = results.first().annotation,
                    shipmentStorageList = results.map { it.lgortTarget }.distinct(),
                    //signsOfDiv = signOfDivision.toSet()
                    signsOfDiv = setOf(
                            GoodsSignOfDivision.ALCO,
                            GoodsSignOfDivision.VET,
                            GoodsSignOfDivision.FOOD
                    ),
                    gisControls = gisControls
            )
        }
    }

    companion object {
        private const val GET_MVM_TYPE_BY_MVM_CODE = "TYPE_MVM = \"%s\""
        private const val GET_MVM_SHORT_TYPE_BY_MVM_CODE = "MVM_TYPE = \"%s\""
        private const val GET_GOODNAME_BY_GOODNUMBER = "MATERIAL = \"%s\""
        private const val ALCO_GOOD_CODE_FOR_PICTOGRAM = "A"
        private const val USUAL_GOOD_CODE_FOR_PICTOGRAM = "N"
    }
}