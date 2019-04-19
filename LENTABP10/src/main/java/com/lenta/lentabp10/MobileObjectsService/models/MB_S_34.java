package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника настроек заданий на списание WOB (справочник видов товара)
public class MB_S_34 {
    /// Тип задания на списание
    @JsonProperty("TASK_TYPE")
    @Getter private String taskType;

    /// Вид товара
    @JsonProperty("MTART")
    @Getter private String mtart;

    public MB_S_34(String taskType, String mtart) {
        this.taskType = taskType;
        this.mtart = mtart;
    }
}
