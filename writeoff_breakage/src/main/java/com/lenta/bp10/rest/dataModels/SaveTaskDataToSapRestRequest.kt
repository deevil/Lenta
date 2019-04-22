package com.lenta.bp10.rest.dataModels

import com.fasterxml.jackson.annotation.JsonProperty
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.shared.models.core.ProductInfo

class SaveTaskDataToSapRestRequest: BaseSapRequest {

    /// Табельный номер
    @JsonProperty("IV_PERNR")
    var perNo: String? = null
        private set

    /// Принтер
    @JsonProperty("IV_PRINTERNAME")
    var printer: String? = null
        private set

    /// Название задания
    @JsonProperty("IV_DESCR")
    var taskName: String? = null
        private set

    /// Тип задания на списание
    @JsonProperty("IV_TYPE")
    var taskType: String? = null
        private set

    /// Предп
    @JsonProperty("IV_WERKS")
    var tkNumber: String? = null
        private set

    /// Склад
    @JsonProperty("IV_LGORT")
    var storloc: String? = null
        private set

    /// IP адрес ТСД
    @JsonProperty("IV_IP")
    var ipAdress: String? = null
        private set

    // Вид движения
    @JsonProperty("IT_MOVE")
    var moveTypes: List<String>? = null
        private set

    // ГИС контроль
    @JsonProperty("IT_GIS")
    var gisControls: List<String>? = null
        private set

    // Тип продукта
    @JsonProperty("IT_MTYPES")
    var materialTypes: List<String>? = null
        private set

    /// Список товаров для сохранения задания из ТСД
    @JsonProperty("IT_MATERIALS")
    var materials: List<ProductInfo>? = null
        private set

    /// Список причин списания для сохранения задания из ТСД
    @JsonProperty("IT_WREASON")
    var writeOffReasons: List<TaskWriteOffReason>? = null
        private set

    /// Список марок для сохранения задания из ТСД
    @JsonProperty("IT_MARKS")
    var exciseStamps:List<TaskExciseStamp>? = null
        private set

    constructor() : super("json", null)

    constructor(perNo: String, printer: String, taskName: String, taskType: String, tkNumber: String, storloc: String, ipAdress: String, moveTypes: List<String>, gisControls: List<String>, materialTypes: List<String>, materials: List<ProductInfo>, writeOffReasons: List<TaskWriteOffReason>, exciseStamps: List<TaskExciseStamp>) : super("json", null)  {
        this.perNo = perNo
        this.printer = printer
        this.taskName = taskName
        this.taskType = taskType
        this.tkNumber = tkNumber
        this.storloc = storloc
        this.ipAdress = ipAdress
        this.moveTypes = moveTypes
        this.gisControls = gisControls
        this.materialTypes = materialTypes
        this.materials = materials
        this.writeOffReasons = writeOffReasons
        this.exciseStamps = exciseStamps
    }

    constructor(sapClient: String) : super("json", sapClient)

    fun setSapClient(sapClient: String): SaveTaskDataToSapRestRequest {
        var request: SaveTaskDataToSapRestRequest = SaveTaskDataToSapRestRequest(sapClient)
        request.perNo = perNo
        request.printer = printer
        request.taskName = taskName
        request.taskType = taskType
        request.tkNumber = tkNumber
        request.storloc = storloc
        request.ipAdress = ipAdress
        request.moveTypes = moveTypes
        request.gisControls = gisControls
        request.materialTypes = materialTypes
        request.materials = materials
        request.writeOffReasons = writeOffReasons
        request.exciseStamps = exciseStamps

        return request
    }
}