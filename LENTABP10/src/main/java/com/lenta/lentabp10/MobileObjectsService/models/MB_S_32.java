package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника причин товарных движений для типов заданий (WOB)
public class MB_S_32 {

    /// Тип задания на списание
    @JsonProperty("TASK_TYPE")
    @Getter private String taskType;

    /// Тип контроля задания на списание
    @JsonProperty("TASK_CNTRL")
    @Getter private String taskCntrl;

    /// Причина движения
    @JsonProperty("REASON")
    @Getter private String reason;

    /// Текст к причине движения товара
    @JsonProperty("GRTXT")
    @Getter private String grtxt;

    public MB_S_32(String taskType, String taskCntrl, String reason, String grtxt) {
        this.taskType = taskType;
        this.taskCntrl = taskCntrl;
        this.reason = reason;
        this.grtxt = grtxt;
    }
}
