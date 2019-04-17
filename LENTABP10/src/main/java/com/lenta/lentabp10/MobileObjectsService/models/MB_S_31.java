package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника закрепления причин списания брака за секциями (WOB)
public class MB_S_31 {

    /// Тип задания на списание
    @JsonProperty("TASK_TYPE")
    @Getter private String taskType;

    /// Номер отдела
    @JsonProperty("SECTION_ID")
    @Getter private String sectionId;

    /// Причина движения
    @JsonProperty("REASON")
    @Getter private String reason;

    /// Текст к причине движения товара
    @JsonProperty("GRTXT")
    @Getter private String grtxt;

    public MB_S_31(String taskType, String sectionId, String reason, String grtxt) {
        this.taskType = taskType;
        this.sectionId = sectionId;
        this.reason = reason;
        this.grtxt = grtxt;
    }
}
