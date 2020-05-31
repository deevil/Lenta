package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.Task
import com.lenta.movement.models.TaskType
import com.lenta.movement.requests.network.models.DbTaskListItem
import com.lenta.movement.requests.network.models.toTask
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import java.util.*
import javax.inject.Inject

class SaveTaskNetRequest @Inject constructor(
    private val fmpRequestsHelper: FmpRequestsHelper
): UseCase<Task, SaveTaskParams> {

    override suspend fun run(params: SaveTaskParams): Either<Failure, Task> {
        val result = fmpRequestsHelper.restRequest(
            resourceName = "ZMP_UTZ_MVM_04_V001",
            data = params,
            clazz = SaveTaskStatus::class.java
        )

        return when (result) {
            is Either.Left -> result
            is Either.Right -> {
                val savedTask = result.b.tasks.firstOrNull()

                when {
                    savedTask == null -> result.left(Failure.ServerError)
                    savedTask.errorText.isNotEmpty() -> result.left(Failure.SapError(savedTask.errorText))
                    else -> result.right(savedTask.toTask())
                }
            }
        }
    }

}

data class SaveTaskParams(
    @SerializedName("IV_IP_PDA")
    val deviceIp: String,
    @SerializedName("IV_PERNR")
    val userNumber: String,
    @SerializedName("IV_WERKS")
    val tkNumber: String,
    @SerializedName("IV_TASK_NUM")
    val taskNumber: String,
    @SerializedName("IV_DESCR")
    val taskName: String,
    @SerializedName("IV_TYPE")
    val taskType: TaskType,
    @SerializedName("IV_TYPE_MVM")
    val movementType: MovementType,
    @SerializedName("IV_LGORT_SRC")
    val lgortSource: String,
    @SerializedName("LGORT_TGT")
    val lgortTarget: String,
    @SerializedName("DATE_SHIP")
    val shipmentDate: Date,
    @SerializedName("IV_NOT_FINISH")
    val isNotFinish: String,
    @SerializedName("WERKS_DSTNTN")
    val destination: String,
    @SerializedName("IT_TASK_POS")
    val materials: List<TaskMaterial>,
    @SerializedName("IT_TASK_BASKET")
    val baskets: List<TaskBasket>
) {

    data class TaskMaterial(
        @SerializedName("MATNR")
        val number: String,
        @SerializedName("FACT_QNT")
        val quantity: String,
        @SerializedName("XZAEL")
        val xzael: String,
        @SerializedName("IS_DEL")
        val isDeleted: String,
        @SerializedName("MEINS")
        val uom: String
    )

    data class TaskBasket(
        @SerializedName("BASKET_NUM")
        val basketNumber: String,
        @SerializedName("MATNR")
        val materialNumber: String,
        @SerializedName("FACT_QNT")
        val quantity: String,
        @SerializedName("MEINS")
        val uom: String,
        @SerializedName("MTART")
        val materialType: String,
        @SerializedName("LIFNR")
        val lifNr: String,
        @SerializedName("ZCHARG")
        val zcharg: String,
        @SerializedName("IS_MARK_STOCKS")
        val isExcise: String,
        @SerializedName("IS_PARTS_STOCKS")
        val isNotExcise: String,
        @SerializedName("IS_ALCO")
        val isAlco: String,
        @SerializedName("IS_USUAL")
        val isUsual: String,
        @SerializedName("IS_VET")
        val isVet: String,
        @SerializedName("IS_FOOD")
        val isFood: String
    )

}

class SaveTaskStatus: ObjectRawStatus<SaveTaskResult>()

data class SaveTaskResult(
    @SerializedName("ET_TASK_LIST")
    val tasks: List<DbTaskListItem>
)