package com.lenta.lentabp10.models.task;

import lombok.Getter;

public class TaskWriteOfReason {

    @Getter private WriteOfReason writeOfReason;
    @Getter private String material;
    @Getter private String count;

    public TaskWriteOfReason(WriteOfReason writeOfReason, String material, String count){
        this.writeOfReason = writeOfReason;
        this.material = material;
        this.count = count;
    }
}
