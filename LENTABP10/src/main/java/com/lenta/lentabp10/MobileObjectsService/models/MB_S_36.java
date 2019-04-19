package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

// Модель справочника настроек заданий на списание WOB (справочник текстов ГИС-контроля)
public class MB_S_36 {
    /// Тип контроля задания на списание
    @JsonProperty("TASK_CNTRL")
    @Getter private String taskCntrl;

    /// Текст длиной 40 знаков
    @JsonProperty("CNTRL_TXT")
    @Getter private String cntrlTxt;

    public MB_S_36(String taskCntrl, String cntrlTxt) {
        this.taskCntrl = taskCntrl;
        this.cntrlTxt = cntrlTxt;
    }
}
