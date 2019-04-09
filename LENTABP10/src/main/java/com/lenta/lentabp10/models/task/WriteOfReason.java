package com.lenta.lentabp10.models.task;

import lombok.Getter;

public class WriteOfReason {

    @Getter private String code;
    @Getter private String name;

    public WriteOfReason(String code, String name){
        this.code = code;
        this.name = name;
    }
}
