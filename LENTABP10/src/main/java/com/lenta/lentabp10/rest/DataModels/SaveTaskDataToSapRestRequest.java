package com.lenta.lentabp10.rest.DataModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lenta.lentabp10.models.task.TaskExciseStamp;
import com.lenta.lentabp10.models.task.TaskWriteOffReason;
import com.lenta.shared.models.core.ProductInfo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class SaveTaskDataToSapRestRequest extends BaseSapRequest {

    /// Табельный номер
    @JsonProperty("IV_PERNR")
    @Getter private String perNo;

    /// Принтер
    @JsonProperty("IV_PRINTERNAME")
    @Getter private String printer;

    /// Название задания
    @JsonProperty("IV_DESCR")
    @Getter private String taskName;

    /// Тип задания на списание
    @JsonProperty("IV_TYPE")
    @Getter private String taskType;

    /// Предп
    @JsonProperty("IV_WERKS")
    @Getter private String tkNumber;

    /// Склад
    @JsonProperty("IV_LGORT")
    @Getter private String storloc;

    /// IP адрес ТСД
    @JsonProperty("IV_IP")
    @Getter private String ipAdress;

    // Вид движения
    @JsonProperty("IT_MOVE")
    @Getter private List<String> moveTypes;

    // ГИС контроль
    @JsonProperty("IT_GIS")
    @Getter private List<String> gisControls;

    // Тип продукта
    @JsonProperty("IT_MTYPES")
    @Getter private List<String> materialTypes;

    /// Список товаров для сохранения задания из ТСД
    @JsonProperty("IT_MATERIALS")
    @Getter @Setter private List<ProductInfo> materials;

    /// Список причин списания для сохранения задания из ТСД
    @JsonProperty("IT_WREASON")
    @Getter @Setter private List<TaskWriteOffReason> writeOffReasons;

    /// Список марок для сохранения задания из ТСД
    @JsonProperty("IT_MARKS")
    @Getter @Setter private List<TaskExciseStamp> exciseStamps;

    public SaveTaskDataToSapRestRequest()
    {
        super("json", null);
    }

    public SaveTaskDataToSapRestRequest(String sapClient)
    {
        super("json", sapClient);
    }

    public SaveTaskDataToSapRestRequest(String perNo, String printer, String taskName, String taskType, String tkNumber, String storloc, String ipAdress, List<String> moveTypes, List<String> gisControls, List<String> materialTypes, List<ProductInfo> materials, List<TaskWriteOffReason> writeOffReasons, List<TaskExciseStamp> exciseStamps) {
        super("json", null);
        this.perNo = perNo;
        this.printer = printer;
        this.taskName = taskName;
        this.taskType = taskType;
        this.tkNumber = tkNumber;
        this.storloc = storloc;
        this.ipAdress = ipAdress;
        this.moveTypes = moveTypes;
        this.gisControls = gisControls;
        this.materialTypes = materialTypes;
        this.materials = materials;
        this.writeOffReasons = writeOffReasons;
        this.exciseStamps = exciseStamps;
    }

    public SaveTaskDataToSapRestRequest setSapClient(String sapClient)
    {
        SaveTaskDataToSapRestRequest request = new SaveTaskDataToSapRestRequest(sapClient);
        request.perNo = perNo;
        request.printer = printer;
        request.taskName = taskName;
        request.taskType = taskType;
        request.tkNumber = tkNumber;
        request.storloc = storloc;
        request.ipAdress = ipAdress;
        request.moveTypes = moveTypes;
        request.gisControls = gisControls;
        request.materialTypes = materialTypes;
        request.materials = materials;
        request.writeOffReasons = writeOffReasons;
        request.exciseStamps = exciseStamps;

        return request;
    }
}
