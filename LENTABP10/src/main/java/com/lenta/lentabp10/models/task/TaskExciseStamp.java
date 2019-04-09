package com.lenta.lentabp10.models.task;

import com.lenta.shared.models.core.ExciseStamp;

import lombok.Getter;

public class TaskExciseStamp extends ExciseStamp {

    @Getter private String setMaterialNumber; //материал набора
    @Getter private String writeOffReason; //причина списания
    @Getter private boolean isBasStamp; // признак "плохой" марки

    public TaskExciseStamp(String materialNumber, String code, String setMaterialNumber, String writeOffReason, boolean isBasStamp) {
        super(materialNumber, code);
        this.setMaterialNumber = setMaterialNumber;
        this.writeOffReason = writeOffReason;
        this.isBasStamp = isBasStamp;
    }

}
