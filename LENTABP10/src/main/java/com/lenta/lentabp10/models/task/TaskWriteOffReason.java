package com.lenta.lentabp10.models.task;

import lombok.Getter;

public class TaskWriteOffReason {

    @Getter private WriteOffReason writeOffReason;
    @Getter private String materialNumber;
    @Getter private double count;

    public TaskWriteOffReason(WriteOffReason writeOffReason, String materialNumber, double count){
        this.writeOffReason = writeOffReason;
        this.materialNumber = materialNumber;
        this.count = count;
    }
}
