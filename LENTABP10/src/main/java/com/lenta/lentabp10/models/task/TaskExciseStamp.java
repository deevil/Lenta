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

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass()!= obj.getClass())
        {
            return false;
        }

        TaskExciseStamp stamp =(TaskExciseStamp)obj;
        if (stamp == null)
        {
            return false;
        }
        return equals(stamp);
    }

    public boolean equals(TaskExciseStamp stamp)
    {
        if (stamp == null)
        {
            return false;
        }
        return stamp.getMaterialNumber() == this.getMaterialNumber()
                && stamp.getCode() == this.getCode()
                && stamp.getSetMaterialNumber() == setMaterialNumber
                && stamp.getWriteOffReason() == writeOffReason
                && stamp.isBasStamp() == isBasStamp;
    }

}
