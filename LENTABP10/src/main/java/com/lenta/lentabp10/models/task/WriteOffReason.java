package com.lenta.lentabp10.models.task;

import lombok.Getter;

public class WriteOffReason {

    @Getter private String code;
    @Getter private String name;

    public WriteOffReason(String code, String name){
        this.code = code;
        this.name = name;
    }
}
