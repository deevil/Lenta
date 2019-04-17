package com.lenta.lentabp10.MobileObjectsService.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class MB_S_29 {

    /// Аббревиатура типа задания на списание
    @JsonProperty("TASK_TYPE")
    @Getter private String taskType;

    /// Вид движения (управление запасами)
    @JsonProperty("BWART")
    @Getter private String bwart;

    /// Место возникновения затрат
    @JsonProperty("KOSTL")
    @Getter private String kostl;

    /// Склад
    @JsonProperty("LGORTTO")
    @Getter private String lgortto;

    /// Требуется отправка в ГИС
    @JsonProperty("SEND_GIS")
    @Getter private String sendGis;

    /// Признак, что для данного типа задания выбирать причину списания не нужно
    @JsonProperty("NO_GRUND")
    @Getter private String noGrund;

    /// Полное название типа задания на списание
    @JsonProperty("LONG_NAME")
    @Getter private String longName;

    /// Лимит количества товаров в задании
    @JsonProperty("LIMIT")
    @Getter private double limit;

    /// Используется в производстве
    @JsonProperty("CHK_OWNPR")
    @Getter private String chkOwnpr;

    public MB_S_29(String taskType, String bwart, String kostl, String lgortto, String sendGis, String noGrund, String longName, double limit, String chkOwnpr) {
        this.taskType = taskType;
        this.bwart = bwart;
        this.kostl = kostl;
        this.lgortto = lgortto;
        this.sendGis = sendGis;
        this.noGrund = noGrund;
        this.longName = longName;
        this.limit = limit;
        this.chkOwnpr = chkOwnpr;
    }
}
