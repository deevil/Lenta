package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника настроек заданий на списание WOB (справочник складов)
public class MB_S_33 {

    /// Тип задания на списание
    @JsonProperty("TASK_TYPE")
    @Getter private String taskType;

    /// ТК
    @JsonProperty("WERKS")
    @Getter private String werks;

    /// Склад
    @JsonProperty("LGORT")
    @Getter private String lgort;

    public MB_S_33(String taskType, String werks, String lgort) {
        this.taskType = taskType;
        this.werks = werks;
        this.lgort = lgort;
    }
}
