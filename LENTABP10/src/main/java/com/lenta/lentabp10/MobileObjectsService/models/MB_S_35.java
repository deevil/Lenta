package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника настроек заданий на списание WOB (справочник ГИС-контроль)
public class MB_S_35 {
    /// Тип задания на списание
    @JsonProperty("TASK_TYPE")
    @Getter private String taskType;

    /// Тип контроля задания на списание
    @JsonProperty("TASK_CNTRL")
    @Getter private String taskCntrl;

    public MB_S_35(String taskType, String taskCntrl) {
        this.taskType = taskType;
        this.taskCntrl = taskCntrl;
    }
}
